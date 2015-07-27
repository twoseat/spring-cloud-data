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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Part or the whole of a number of responses returned from list operations, get operations,
 * and create and update operations.
 *
 * @author Steve Powell
 */
class ResourceResponse {

	private volatile Metadata metadata;

	private volatile Entity entity;

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	static final class Metadata {

		private volatile String id;

		private volatile String url;

		private volatile String createdAt;

		private volatile String updatedAt;

		public String getId() {
			return id;
		}

		@JsonProperty("guid")
		public void setId(String id) {
			this.id = id;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getCreatedAt() {
			return createdAt;
		}

		@JsonProperty("created_at")
		public void setCreatedAt(String createdAt) {
			this.createdAt = createdAt;
		}

		public String getUpdatedAt() {
			return updatedAt;
		}

		@JsonProperty("updated_at")
		public void setUpdatedAt(String updatedAt) {
			this.updatedAt = updatedAt;
		}
	}

	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	static final class Entity {

		private volatile String name;

		private volatile String state;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getState() {
			return state;
		}

		public void setState(String state) {
			this.state = state;
		}
	}
}
