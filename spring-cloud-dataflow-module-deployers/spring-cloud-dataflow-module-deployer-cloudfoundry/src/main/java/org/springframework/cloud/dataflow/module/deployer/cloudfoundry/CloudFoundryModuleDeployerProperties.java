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

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * @author Eric Bottard
 */
@ConfigurationProperties("cloudfoundry")
class CloudFoundryModuleDeployerProperties {

	/**
	 * Location of the CloudFoundry REST API endpoint to use.
	 */
	private URI apiEndpoint;

	/**
	 * The domain to use when mapping routes for applications.
	 */
	private String domain;

	/**
	 * Location of the ModuleLauncher uber-jar to be uploaded.
	 */
	private Resource moduleLauncherLocation = new ClassPathResource("spring-cloud-stream-module-launcher.jar");

	/**
	 * The organization to use when registering new applications.
	 */
	private String organization;

	/**
	 * Password to use when accessing CloudController.
	 */
	private String password;

	/**
	 * The names of services to bind to each application deployed as a module.
	 * This should typically contain a service capable of playing the role of a binding transport.
	 */
	private Set<String> services = new HashSet<>(Arrays.asList("redis"));

	/**
	 * The space to use when registering new applications.
	 */
	private String space;

	/**
	 * Username to use when accessing CloudController.
	 */
	private String username;

	public URI getApiEndpoint() {
		return apiEndpoint;
	}

	public void setApiEndpoint(URI apiEndpoint) {
		this.apiEndpoint = apiEndpoint;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public Resource getModuleLauncherLocation() {
		return moduleLauncherLocation;
	}

	public void setModuleLauncherLocation(Resource moduleLauncherLocation) {
		this.moduleLauncherLocation = moduleLauncherLocation;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<String> getServices() {
		return services;
	}

	public void setServices(Set<String> services) {
		this.services = services;
	}

	public String getSpace() {
		return space;
	}

	public void setSpace(String space) {
		this.space = space;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
