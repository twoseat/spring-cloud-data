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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Implementation of high-level operations on applications, limited to those
 * operations required by the {@link CloudFoundryModuleDeployer}.
 *
 * @author Steve Powell
 */
@Component
final class StandardCloudFoundryApplicationOperations implements CloudFoundryApplicationOperations {

	private static final String DEFAULT_BUILDPACK = "";

	private static final int DEFAULT_MEMORY = 1024; // megabytes

	private final CloudControllerRestClient client;

	private final String spaceId;

	@Autowired
	StandardCloudFoundryApplicationOperations(CloudControllerRestClient client,
			@Value("${cloudfoundry.organization}") String organizationName,
			@Value("${cloudfoundry.space}") String spaceName) {
		this.client = client;
		this.spaceId = getSpaceId(client, organizationName, spaceName);
	}

	@Override
	public DeleteApplicationResults deleteApplication(DeleteApplicationParameters parameters) {
		// Check that application actually exists
		ListApplicationsRequest listRequest = new ListApplicationsRequest()
				.withSpaceId(this.spaceId)
				.withName(parameters.getName());

		List<ResourceResponse<ApplicationEntity>> applications = this.client.listApplications(listRequest).getResources();

		if (applications.isEmpty()) {
			return new DeleteApplicationResults().withFound(false);
		}
		String appId = applications.get(0).getMetadata().getId();

		// Then, unbind any services
		ListServiceBindingsRequest listServiceBindingsRequest = new ListServiceBindingsRequest()
				.withAppId(appId);
		List<ResourceResponse<ServiceBindingEntity>> serviceBindings = this.client.listServiceBindings(listServiceBindingsRequest).getResources();
		for (ResourceResponse<ServiceBindingEntity> serviceBinding : serviceBindings) {
			this.client.removeServiceBinding(new RemoveServiceBindingRequest()
					.withAppId(appId)
					.withBindingId(serviceBinding.getMetadata().getId())
			);
		}

		// Then, perform the actual deletion
		DeleteApplicationRequest deleteRequest = new DeleteApplicationRequest()
				.withId(appId);

		DeleteApplicationResponse response = this.client.deleteApplication(deleteRequest);

		return new DeleteApplicationResults().withFound(true).withDeleted(response.isDeleted());
	}

	@Override
	public GetApplicationsStatusResults getApplicationsStatus(
			GetApplicationsStatusParameters parameters) {
		ListApplicationsRequest listRequest = new ListApplicationsRequest()
				.withSpaceId(this.spaceId);

		if (parameters.getName() != null) {
			listRequest.withName(parameters.getName());
		}

		List<ResourceResponse<ApplicationEntity>> applications = this.client.listApplications(listRequest).getResources();
		if (applications.isEmpty()) {
			return new GetApplicationsStatusResults();
		}

		GetApplicationsStatusResults response = new GetApplicationsStatusResults();
		for (ResourceResponse<ApplicationEntity> application : applications) {
			String applicationId = application.getMetadata().getId();
			String applicationName = application.getEntity().getName();
			String applicationState = application.getEntity().getState();

			// TODO: decide what to do here
			if (!"STARTED".equals(applicationState)) {
				response.withApplication(applicationName, new ApplicationStatus());
			}
			else {
				GetApplicationStatisticsRequest statsRequest = new GetApplicationStatisticsRequest()
						.withId(applicationId);

				GetApplicationStatisticsResponse statsResponse = this.client.getApplicationStatistics(statsRequest);
				response.withApplication(applicationName,
						new ApplicationStatus()
								.withId(applicationId)
								.withInstances(statsResponse));
			}
		}

		return response;
	}

	@Override
	public PushApplicationResults pushApplication(PushApplicationParameters parameters) {
		PushApplicationResults pushResponse = new PushApplicationResults();

		CreateApplicationRequest createRequest = new CreateApplicationRequest()
				.withSpaceId(this.spaceId)
				.withName(parameters.getName())
				.withInstances(1)
				.withBuildpack(DEFAULT_BUILDPACK)
				.withMemory(DEFAULT_MEMORY)
				.withState("STOPPED")
				.withEnvironment(parameters.getEnvironment());

		CreateApplicationResponse createResponse = this.client.createApplication(createRequest);
		if (createResponse == null) {
			return pushResponse.withError(PushApplicationResults.Error.CREATE_FAILED);
		}

		UploadBitsRequest uploadBitsRequest = new UploadBitsRequest()
				.withId(createResponse.getMetadata().getId())
				.withResource(parameters.getResource());

		UploadBitsResponse uploadBitsResponse = this.client.uploadBits(uploadBitsRequest);
		if (uploadBitsResponse == null) {
			return pushResponse.withError(PushApplicationResults.Error.UPLOAD_FAILED);
		}

		UpdateApplicationRequest updateRequest = new UpdateApplicationRequest()
				.withId(createResponse.getMetadata().getId())
				.withState("STARTED");
		UpdateApplicationResponse updateResponse = this.client.updateApplication(updateRequest);
		if (updateResponse == null) {
			return pushResponse.withError(PushApplicationResults.Error.START_FAILED);
		}

		return pushResponse.withError(PushApplicationResults.Error.NONE);
	}

	private static String getSpaceId(CloudControllerRestClient client, String organizationName, String spaceName) {
		ListOrganizationsRequest organizationsRequest = new ListOrganizationsRequest()
				.withName(organizationName);

		String orgId = client.listOrganizations(organizationsRequest).getResources().get(0).getMetadata().getId();

		ListSpacesRequest spacesRequest = new ListSpacesRequest()
				.withOrgId(orgId)
				.withName(spaceName);

		return client.listSpaces(spacesRequest).getResources().get(0).getMetadata().getId();
	}
}
