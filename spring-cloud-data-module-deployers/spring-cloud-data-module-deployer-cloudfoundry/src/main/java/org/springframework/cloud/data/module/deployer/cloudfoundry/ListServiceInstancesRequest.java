package org.springframework.cloud.data.module.deployer.cloudfoundry;

/**
 * @author Steve Powell
 */
final class ListServiceInstancesRequest {
	private String name;

	private String spaceId;

	public String getName() {
		return name;
	}

	public ListServiceInstancesRequest withName(String name) {
		this.name = name;
		return this;
	}

	public String getSpaceId() {
		return spaceId;
	}

	public ListServiceInstancesRequest withSpaceId(String spaceId) {
		this.spaceId = spaceId;
		return this;
	}
}
