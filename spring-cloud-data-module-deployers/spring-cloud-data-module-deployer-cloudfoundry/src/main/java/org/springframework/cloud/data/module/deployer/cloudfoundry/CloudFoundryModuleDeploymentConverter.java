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

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.data.core.ModuleDefinition;
import org.springframework.cloud.data.core.ModuleDeploymentId;
import org.springframework.cloud.data.core.ModuleDeploymentRequest;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Converts between Module information and Application information.
 * Sole owner of correspondence between module ids and application names; also builds environment for the module launcher application.
 *
 * @author Steve Powell
 */
@Component
class CloudFoundryModuleDeploymentConverter {

	//TODO: validate the prefixing strategy
	private static final String PREFIX = "spring_cloud_data_";

	private Resource moduleLauncherResource;

	@Autowired
	CloudFoundryModuleDeploymentConverter(@Value("${cloudfoundry.moduleLauncherLocation}") Resource moduleLauncherResource) {
		this.moduleLauncherResource = moduleLauncherResource;
	}

	String toApplicationName(ModuleDeploymentId moduleDeploymentId) {
		return PREFIX + moduleDeploymentId.toString();
	}

	ModuleDeploymentId toModuleDeploymentId(String applicationName) {
		String moduleIdString = moduleIdString(applicationName);
		if (moduleIdString == null) {
			return null;
		}
		try {
			return ModuleDeploymentId.parse(moduleIdString);
		}
		catch (IllegalArgumentException e) {
			return null; // We ignore invalid format cases
		}
	}

	Resource toModuleLauncherResource(ModuleDefinition moduleDefinition) {
		// We use a fixed launcher application
		return this.moduleLauncherResource;
	}

	Map<String, String> toModuleLauncherEnvironment(ModuleDeploymentRequest moduleDeploymentRequest) {
		HashMap<String, String> environment = new HashMap<>();
		environment.put("modules", moduleDeploymentRequest.getCoordinates().toString());
		for (Map.Entry<String, String> entry : moduleDeploymentRequest.getDefinition().getParameters().entrySet()) {
			environment.put(String.format("arguments.%d.%s", 0, entry.getKey()), entry.getValue());
		}
		return environment;
	}

	private static String moduleIdString(String applicationName) {
		if (applicationName.startsWith(PREFIX)) {
			return applicationName.substring(PREFIX.length());
		}
		return null;
	}
}
