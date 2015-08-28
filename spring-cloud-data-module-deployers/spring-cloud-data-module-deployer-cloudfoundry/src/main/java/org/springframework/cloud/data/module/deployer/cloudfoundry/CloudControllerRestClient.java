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

/**
 * Interface to cloud controller functions which wrap the REST interface required by the {@link CloudFoundryModuleDeployer}.
 *
 * @author Steve Powell
 */
interface CloudControllerRestClient {

	/**
	 * Creates an application definition and returns its id.
	 * @param request the structure carrying all necessary parameters
	 * @return a Response instance carrying all the response values expected
	 * @throws org.springframework.web.client.RestClientException in the event of failure
	 */
	CreateApplicationResponse createApplication(CreateApplicationRequest request);

	/**
	 * Deletes an application given its id. The application may be in any state.
	 * @param request the structure carrying all necessary parameters
	 * @return a Response instance carrying all the response values expected
	 * @throws org.springframework.web.client.RestClientException in the event of failure
	 */
	DeleteApplicationResponse deleteApplication(DeleteApplicationRequest request);

	/**
	 * Obtains application statistics for every instance of an application given its id.
	 * @param request the structure carrying all necessary parameters
	 * @return a Response instance carrying all the response values expected
	 * @throws org.springframework.web.client.RestClientException in the event of failure
	 */
	GetApplicationStatisticsResponse getApplicationStatistics(GetApplicationStatisticsRequest request);

	/**
	 * Lists applications (with their ids) in a given space(id) and optionally matching a name.
	 * @param request the structure carrying all necessary parameters
	 * @return a Response instance carrying all the response values expected
	 * @throws org.springframework.web.client.RestClientException in the event of failure
	 */
	ListApplicationsResponse listApplications(ListApplicationsRequest request);

	/**
	 * Lists all the known organizations (with their ids).
	 * @param request the structure carrying all necessary parameters
	 * @return a Response instance carrying all the response values expected
	 * @throws org.springframework.web.client.RestClientException in the event of failure
	 */
	ListOrganizationsResponse listOrganizations(ListOrganizationsRequest request);

	/**
	 * Lists all the known spaces (with their ids) in a given organization (given by id).
	 * @param request the structure carrying all necessary parameters
	 * @return a Response instance carrying all the response values expected
	 * @throws org.springframework.web.client.RestClientException in the event of failure
	 */
	ListSpacesResponse listSpaces(ListSpacesRequest request);

	/**
	 * Uploads the bits required for an application (identified by its id) to run.
	 * @param request the structure carrying all necessary parameters
	 * @return a Response instance carrying all the response values expected
	 * @throws org.springframework.web.client.RestClientException in the event of failure
	 */
	UploadBitsResponse uploadBits(UploadBitsRequest request);

	/**
	 * Updates the state of an application (given by its id), for example, to start it.
	 * @param request the structure carrying all necessary parameters
	 * @return a Response instance carrying all the response values expected
	 * @throws org.springframework.web.client.RestClientException in the event of failure
	 */
	UpdateApplicationResponse updateApplication(UpdateApplicationRequest request);

}
