package org.springframework.cloud.data.module.deployer.cloudfoundry;

/**
 * Request for REST operation {@link CloudControllerRestClient#updateApplication(UpdateApplicationRequest) updateApplication()}.
 *
 * @author Steve Powell
 */
class UpdateApplicationRequest extends CreateApplicationRequest<UpdateApplicationRequest> {

	private String id;

	public String getId() {
		return id;
	}

	public UpdateApplicationRequest withId(String id) {
		this.id = id;
		return this;
	}
}
