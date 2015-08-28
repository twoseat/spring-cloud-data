package org.springframework.cloud.data.module.deployer.cloudfoundry;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Steve Powell
 */
class CreateServiceBindingRequest {

	private String appId;

	private Map<String, String> parameters = new HashMap<>();

	private String serviceInstanceId;

	@JsonProperty("app_guid")
	public String getAppId() {
		return appId;
	}

	public CreateServiceBindingRequest withAppId(String appId) {
		this.appId = appId;
		return this;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public CreateServiceBindingRequest withParameters(Map<String, String> parameters) {
		this.parameters.putAll(parameters);
		return this;
	}

	public CreateServiceBindingRequest withParameter(String key, String value) {
		this.parameters.put(key, value);
		return this;
	}

	@JsonProperty("service_instance_guid")
	public String getServiceInstanceId() {
		return serviceInstanceId;
	}

	public CreateServiceBindingRequest withServiceInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
		return this;
	}

}
