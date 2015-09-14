/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.dataflow.module.deployer.cloudfoundry;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.dataflow.module.deployer.ModuleDeployer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * This class is present to make sure that Spring Authentication protocols for Cloud Foundry
 * controller API access are correctly wired.
 *
 * @author Ben Hale
 * @author Steve Powell
 * @author Eric Bottard
 */
@Configuration
@EnableConfigurationProperties(CloudFoundryModuleDeployerProperties.class)
public class CloudFoundryModuleDeployerConfiguration {

	private String clientId = "cf";

	private String clientSecret = "";

	private RestOperations initialRestOperations = getInitialRestOperations();

	@Autowired
	private CloudFoundryModuleDeployerProperties properties;

	@Bean
	public ModuleDeployer processModuleDeployer(
			CloudFoundryModuleDeploymentConverter converter,
			CloudFoundryApplicationOperations applicationOperations) {
		return new ApplicationModuleDeployer(properties, converter, applicationOperations);
	}

	@Bean
	public ModuleDeployer taskModuleDeployer(
			CloudFoundryModuleDeploymentConverter converter,
			CloudFoundryApplicationOperations applicationOperations) {
		// TODO: return appropriate task deployer
		return new ApplicationModuleDeployer(properties, converter, applicationOperations);
	}

	@Bean
	CloudControllerOperations cloudControllerOperations(ExtendedOAuth2RestOperations restOperations) {
		return new CloudControllerTemplate(properties.getApiEndpoint(), restOperations);
	}

	@Bean
	CloudFoundryApplicationOperations cloudFoundryApplicationOperations(
			CloudControllerOperations client) {
		return new CloudFoundryApplicationTemplate(client,
				properties.getOrganization(),
				properties.getSpace(),
				properties.getDomain());
	}

	@Bean
	CloudFoundryModuleDeploymentConverter cloudFoundryModuleDeploymentConverter() {
		return new CloudFoundryModuleDeploymentConverter();
	}

	@Bean
	ExtendedOAuth2RestOperations oAuth2RestTemplate(
			OAuth2ClientContext clientContext,
			OAuth2ProtectedResourceDetails details) {
		return new ExtendedOAuth2RestTemplate(details, clientContext);
	}

	@Bean
	@ConfigurationProperties("security.oauth2.client")
	OAuth2ProtectedResourceDetails oAuth2ProtectedResourceDetails() {
		ResourceOwnerPasswordResourceDetails resource = new ResourceOwnerPasswordResourceDetails();
		resource.setClientId(this.clientId);
		resource.setClientSecret(this.clientSecret);
		resource.setAccessTokenUri(getAccessTokenUri(this.initialRestOperations, this.properties.getApiEndpoint()));
		resource.setUsername(this.properties.getUsername());
		resource.setPassword(this.properties.getPassword());
		return resource;
	}

	@Bean
	OAuth2ClientContext oAuth2ClientContext() {
		return new DefaultOAuth2ClientContext(new DefaultAccessTokenRequest());
	}

	private static String getAccessTokenUri(RestOperations restOperations, URI apiEndpoint) {
		URI infoUri = UriComponentsBuilder.fromUri(apiEndpoint)
				.scheme("https").pathSegment("info")
				.build().toUri();

		@SuppressWarnings("unchecked") Map<String, String> results = (Map<String, String>) restOperations.getForObject(infoUri, Map.class);

		return UriComponentsBuilder.fromUriString(results.get("token_endpoint"))
				.pathSegment("oauth", "token")
				.build().toUriString();
	}

	private static RestOperations getInitialRestOperations() {
		RestTemplate restTemplate = new RestTemplate();

		for (HttpMessageConverter hmc : restTemplate.getMessageConverters()) {
			if (hmc instanceof MappingJackson2HttpMessageConverter) {
				MappingJackson2HttpMessageConverter j2hmc = (MappingJackson2HttpMessageConverter) hmc;
				j2hmc.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
				break;
			}
		}

		restTemplate.getMessageConverters().add(new AbstractHttpMessageConverter<Object>() {

			@Override
			protected boolean supports(Class<?> clazz) {
				return true;
			}

			@Override
			protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
				throw new UnsupportedOperationException();
			}

			@Override
			protected void writeInternal(Object o, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
			}
		});
		return restTemplate;
	}

	private static class ExtendedOAuth2RestTemplate extends OAuth2RestTemplate implements ExtendedOAuth2RestOperations {

		public ExtendedOAuth2RestTemplate(OAuth2ProtectedResourceDetails resource, OAuth2ClientContext context) {
			super(resource, context);
		}

		@Override
		public <T> T putForObject(URI uri, Object request, Class<T> responseType) throws RestClientException {
			RequestCallback requestCallback = httpEntityCallback(request, responseType);
			HttpMessageConverterExtractor<T> responseExtractor =
					new HttpMessageConverterExtractor<>(responseType, getMessageConverters());
			return this.execute(uri, HttpMethod.PUT, requestCallback, responseExtractor);
		}
	}
}
