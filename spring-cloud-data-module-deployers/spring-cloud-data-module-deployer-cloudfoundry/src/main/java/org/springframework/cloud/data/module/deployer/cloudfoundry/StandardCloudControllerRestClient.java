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

package org.springframework.cloud.data.module.deployer.cloudfoundry;

import java.net.URI;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * REST client specialised for module deployment requirements.
 *
 * @author Steve Powell
 */
@Component
final class StandardCloudControllerRestClient implements CloudControllerRestClient {

	private final URI endpoint;

	private final ExtendedOAuth2RestOperations restOperations;

	@Autowired
	StandardCloudControllerRestClient(@Value("${cloudfoundry.api.endpoint}") URI endpoint,
			ExtendedOAuth2RestOperations restOperations) {
		this.endpoint = endpoint;
		this.restOperations = restOperations;
	}

	@Override
	public CreateApplicationResponse createApplication(CreateApplicationRequest request) {
		URI uri = UriComponentsBuilder.fromUri(this.endpoint)
				.pathSegment("v2", "apps")
				.build().toUri();
		try {
			return this.restOperations.postForObject(uri, request, CreateApplicationResponse.class);
		}
		catch (RestClientException _) {
			return null;
		}
	}

	@Override
	public DeleteApplicationResponse deleteApplication(DeleteApplicationRequest request) {
		URI uri = UriComponentsBuilder.fromUri(this.endpoint)
				.pathSegment("v2", "apps", request.getId())
				.build().toUri();

		this.restOperations.delete(uri);

		return new DeleteApplicationResponse().withDeleted(true);
	}

	@Override
	public GetApplicationStatisticsResponse getApplicationStatistics(GetApplicationStatisticsRequest request) {
		URI uri = UriComponentsBuilder.fromUri(this.endpoint)
				.pathSegment("v2", "apps", request.getId(), "stats")
				.build().toUri();

		return this.restOperations.getForObject(uri, GetApplicationStatisticsResponse.class);
	}

	@Override
	public ListApplicationsResponse listApplications(ListApplicationsRequest request) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUri(this.endpoint)
				.pathSegment("v2", "apps")
				.queryParam("q", "space_guid:" + request.getSpaceId());
		if (!StringUtils.isEmpty(request.getName())) {
			builder.queryParam("q", "name:" + request.getName());
		}
		URI uri = builder.build().toUri();

		return this.restOperations.getForObject(uri, ListApplicationsResponse.class);
	}

	@Override
	public ListOrganizationsResponse listOrganizations(ListOrganizationsRequest request) {
		URI uri = UriComponentsBuilder.fromUri(this.endpoint)
				.pathSegment("v2", "organizations")
				.queryParam("q", "name:" + request.getName())
				.build().toUri();

		return this.restOperations.getForObject(uri, ListOrganizationsResponse.class);
	}

	@Override
	public ListSpacesResponse listSpaces(ListSpacesRequest request) {
		URI uri = UriComponentsBuilder.fromUri(this.endpoint)
				.pathSegment("v2", "spaces")
				.queryParam("q", "name:" + request.getName())
				.queryParam("q", "organization_guid:" + request.getOrgId())
				.build().toUri();

		return this.restOperations.getForObject(uri, ListSpacesResponse.class);
	}

	@Override
	public UpdateApplicationResponse updateApplication(UpdateApplicationRequest request) {
		URI uri = UriComponentsBuilder.fromUri(this.endpoint)
				.pathSegment("v2", "apps", request.getId())
				.build().toUri();

		try {
			return this.restOperations.putForObject(uri, request, UpdateApplicationResponse.class);
		}
		catch (RestClientException _) {
			return null;
		}
	}

	@Override
	public UploadBitsResponse uploadBits(UploadBitsRequest request) {
		URI uri = UriComponentsBuilder.fromUri(this.endpoint)
				.pathSegment("v2", "apps", request.getId(), "bits")
				.queryParam("async", false)
				.build().toUri();

		MultiValueMap<String, Object> payload = new LinkedMultiValueMap<>();

		payload.add("application", request.getResource());
		payload.add("resources", new ArrayList());

		try {
			this.restOperations.put(uri, payload);
			return new UploadBitsResponse();
		}
		catch (RestClientException _) {
			return null;
		}
	}

}
