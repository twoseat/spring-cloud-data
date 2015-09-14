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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.core.io.Resource;

/**
 * @author Steve Powell
 * @author Paul Harris
 */
class Parameters {
	/**
	 * Parameters for {@link CloudFoundryApplicationOperations#deleteApplication(DeleteApplication) deleteApplication()} operation.
	 */
	static class DeleteApplication {

		private volatile String name;

		public String getName() {
			return name;
		}

		public DeleteApplication withName(String name) {
			this.name = name;
			return this;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			DeleteApplication that = (DeleteApplication) o;

			return !(name != null ? !name.equals(that.name) : that.name != null);
		}

		@Override
		public int hashCode() {
			return name != null ? name.hashCode() : 0;
		}
	}

	/**
	 * Parameters for {@link CloudFoundryApplicationOperations#getApplicationsStatus(GetApplicationsStatus) getApplicationsStatus()} operation.
	 * Parameter {@code name} is optional; if {@code name} is {@code null} all applications statuses are requested.
	 */
	static class GetApplicationsStatus {

		private volatile String name;

		public String getName() {
			return name;
		}

		public GetApplicationsStatus withName(String name) {
			this.name = name;
			return this;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			GetApplicationsStatus that = (GetApplicationsStatus) o;

			return !(name != null ? !name.equals(that.name) : that.name != null);
		}

		@Override
		public int hashCode() {
			return name != null ? name.hashCode() : 0;
		}
	}

	/**
	 * Parameters for {@link CloudFoundryApplicationOperations#pushBindAndStartApplication(PushBindAndStartApplication) pushBindAndStartApplication()} operation.
	 */
	static class PushBindAndStartApplication {

		private Map<String, String> environment;

		private int instances = 1;

		private String name;

		private Resource resource;

		private Set<String> serviceInstanceNames = new HashSet<>();

		public Map<String, String> getEnvironment() {
			return environment;
		}

		public PushBindAndStartApplication withEnvironment(Map<String, String> environment) {
			this.environment = environment;
			return this;
		}

		public int getInstances() {
			return instances;
		}

		public PushBindAndStartApplication withInstances(int instances) {
			this.instances = instances;
			return this;
		}

		public String getName() {
			return name;
		}

		public PushBindAndStartApplication withName(String name) {
			this.name = name;
			return this;
		}

		public Resource getResource() {
			return resource;
		}

		public PushBindAndStartApplication withResource(Resource resource) {
			this.resource = resource;
			return this;
		}

		public Set<String> getServiceInstanceNames() {
			return serviceInstanceNames;
		}

		public PushBindAndStartApplication withServiceInstanceNames(Set<String> serviceInstanceNames) {
			this.serviceInstanceNames.addAll(serviceInstanceNames);
			return this;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			PushBindAndStartApplication that = (PushBindAndStartApplication) o;

			if (instances != that.instances) {
				return false;
			}
			if (!mapEquals(environment, that.environment)) {
				return false;
			}
			if (name != null ? !name.equals(that.name) : that.name != null) {
				return false;
			}
			if (resource != null ? !resource.equals(that.resource) : that.resource != null) {
				return false;
			}
			return setEquals(serviceInstanceNames, that.serviceInstanceNames);
		}

		@Override
		public int hashCode() {
			int result = environment != null ? environment.hashCode() : 0;
			result = 31 * result + instances;
			result = 31 * result + (name != null ? name.hashCode() : 0);
			result = 31 * result + (resource != null ? resource.hashCode() : 0);
			result = 31 * result + (serviceInstanceNames != null ? serviceInstanceNames.hashCode() : 0);
			return result;
		}

		private static boolean mapEquals(Map<String, String> map1, Map<String, String> map2) {
			if (map1 == map2) {
				return true;
			}
			if (map1 == null && map2 == null) {
				return true;
			}
			if (map1 == null || map2 == null) {
				return false;
			}
			if (map1.size() != map2.size()) {
				return false;
			}
			for (Map.Entry<String, String> e : map1.entrySet()) {
				if (e.getValue() == null) {
					if (map2.get(e.getKey()) != null) {
						return false;
					}
				}
				else if (!e.getValue().equals(map2.get(e.getKey()))) {
					return false;
				}
			}
			return true;
		}

		private static boolean setEquals(Set<String> set1, Set<String> set2) {
			if (set1 == set2) {
				return true;
			}
			if (set1 == null && set2 == null) {
				return true;
			}
			if (set1 == null || set2 == null) {
				return false;
			}
			if (set1.size() != set2.size()) {
				return false;
			}
			for (String s : set1) {
				if (!set2.contains(s)) {
					return false;
				}
			}
			return true;
		}
	}
}
