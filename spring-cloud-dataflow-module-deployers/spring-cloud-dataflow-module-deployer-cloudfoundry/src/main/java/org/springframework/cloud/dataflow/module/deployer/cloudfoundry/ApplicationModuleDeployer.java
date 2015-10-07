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

import java.util.HashMap;
import java.util.Map;

import org.springframework.cloud.dataflow.core.ModuleDefinition;
import org.springframework.cloud.dataflow.core.ModuleDeploymentId;
import org.springframework.cloud.dataflow.core.ModuleDeploymentRequest;
import org.springframework.cloud.dataflow.module.ModuleStatus;
import org.springframework.cloud.dataflow.module.deployer.ModuleDeployer;

/**
 * A {@link ModuleDeployer} which deploys modules as applications running in a space in CloudFoundry.
 *
 * @author Paul Harris
 * @author Steve Powell
 * @author Eric Bottard
 */
class ApplicationModuleDeployer implements ModuleDeployer {

	private final CloudFoundryModuleDeploymentConverter cloudFoundryModuleDeploymentConverter;

	private final CloudFoundryApplicationOperations applicationOperations;

	private final CloudFoundryModuleDeployerProperties properties;

	ApplicationModuleDeployer(
			CloudFoundryModuleDeployerProperties properties,
			CloudFoundryModuleDeploymentConverter converter,
			CloudFoundryApplicationOperations applicationOperations) {
		this.properties = properties;
		this.cloudFoundryModuleDeploymentConverter = converter;
		this.applicationOperations = applicationOperations;
	}

	@Override
	public ModuleDeploymentId deploy(ModuleDeploymentRequest request) {
		ModuleDeploymentId moduleDeploymentId = ModuleDeploymentId.fromModuleDefinition(request.getDefinition());
		String applicationName = this.cloudFoundryModuleDeploymentConverter.toApplicationName(moduleDeploymentId);

		Results.PushBindAndStartApplication response = this.applicationOperations.pushBindAndStartApplication(new Parameters.PushBindAndStartApplication()
						.withEnvironment(this.cloudFoundryModuleDeploymentConverter.generateModuleLauncherEnvironment(request))
						.withInstances(request.getCount())
						.withName(applicationName)
						.withResource(properties.getModuleLauncherLocation())
						.withServiceInstanceNames(this.properties.getServices())
		);
		if (!response.isCreateSucceeded()) {
			throw new IllegalStateException("Module " + moduleDeploymentId + " could not be deployed");
		}
		return moduleDeploymentId;
	}

	@Override
	public Map<ModuleDeploymentId, ModuleStatus> status() {
		Results.GetApplicationsStatus response = this.applicationOperations.getApplicationsStatus(
				new Parameters.GetApplicationsStatus());

		Map<ModuleDeploymentId, ModuleStatus> result = new HashMap<>();
		for (Map.Entry<String, ApplicationStatus> e : response.getApplications().entrySet()) {
			ApplicationStatus applicationStatus = e.getValue();
			ModuleDeploymentId moduleDeploymentId = this.cloudFoundryModuleDeploymentConverter.getModuleId(applicationStatus);
			if (null != moduleDeploymentId) { // filter out non-modules
				result.put(moduleDeploymentId,
						new ModuleStatusBuilder().withId(moduleDeploymentId).withApplicationStatus(applicationStatus).build());
			}
		}
		return result;
	}

	@Override
	public ModuleStatus status(ModuleDeploymentId moduleDeploymentId) {
		String applicationName = this.cloudFoundryModuleDeploymentConverter.toApplicationName(moduleDeploymentId);

		Results.GetApplicationsStatus response = this.applicationOperations.getApplicationsStatus(
				new Parameters.GetApplicationsStatus().withName(applicationName));

		return new ModuleStatusBuilder().withId(moduleDeploymentId).withApplicationStatus(response.getApplications().get(applicationName)).build();
	}

	@Override
	public void undeploy(ModuleDeploymentId moduleDeploymentId) {
		Results.DeleteApplication response = this.applicationOperations.deleteApplication(
				new Parameters.DeleteApplication()
						.withName(this.cloudFoundryModuleDeploymentConverter.toApplicationName(moduleDeploymentId)));
		if (!response.isFound() || !response.isDeleted()) {
			throw new IllegalStateException("Module " + moduleDeploymentId + " is not deployed");
		}
	}
}
