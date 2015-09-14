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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.springframework.cloud.dataflow.core.ModuleCoordinates;
import org.springframework.cloud.dataflow.core.ModuleDefinition;
import org.springframework.cloud.dataflow.core.ModuleDeploymentId;
import org.springframework.cloud.dataflow.core.ModuleDeploymentRequest;
import org.springframework.cloud.dataflow.module.ModuleStatus;
import org.springframework.cloud.dataflow.module.deployer.ModuleDeployer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * @author Steve Powell
 */
public class ApplicationModuleDeployerTest {

	private CloudFoundryApplicationOperations ops = mock(CloudFoundryApplicationOperations.class);

	private ModuleDeployer moduleDeployer;

	private Resource testResource = new FileSystemResource("");

	private Set<String> testServices = new HashSet<>(Arrays.asList("test-service"));

	@Before
	public void setUpDeployer() throws Exception {
		CloudFoundryModuleDeployerProperties props = new CloudFoundryModuleDeployerProperties();
		props.setModuleLauncherLocation(testResource);
		props.setServices(testServices);
		this.moduleDeployer = new ApplicationModuleDeployer(
				props,
				new CloudFoundryModuleDeploymentConverter(),
				this.ops);
	}

	@Test
	public void testDeploy() throws Exception {
		Map<String, String> testEnv = new HashMap<>();
		testEnv.put("JBP_CONFIG_JAVA_MAIN", "{arguments: '--args.0.test-param=test-param-value --modules=groupId:artifactId:jar:1.2.3 '}\n");
		testEnv.put("SPRING_CLOUD_DATAFLOW_MODULE", "test-group.test-label");

		when(this.ops.pushBindAndStartApplication(new Parameters.PushBindAndStartApplication()
				.withName("test-group" + "-" + "test-label")
				.withServiceInstanceNames(testServices)
				.withResource(testResource)
				.withInstances(1)
				.withEnvironment(testEnv)))
				.thenReturn(new Results.PushBindAndStartApplication().withCreateSucceeded(true));

		ModuleDefinition.Builder definitionBuilder = new ModuleDefinition.Builder();
		definitionBuilder.setGroup("test-group");
		definitionBuilder.setLabel("test-label");
		definitionBuilder.setName("test-name");
		definitionBuilder.setParameter("test-param", "test-param-value");
		ModuleDefinition definition = definitionBuilder.build();

		ModuleCoordinates.Builder coordsBuilder = new ModuleCoordinates.Builder();
		ModuleCoordinates coords = ModuleCoordinates.parse("groupId:artifactId:1.2.3");

		ModuleDeploymentRequest request = new ModuleDeploymentRequest(definition, coords);

		ModuleDeploymentId modId = this.moduleDeployer.deploy(request);

		assertEquals(new ModuleDeploymentId("test-group", "test-label"), modId);
	}

	@Test(expected = IllegalStateException.class)
	public void testDeployFails() throws Exception {
		Map<String, String> testEnv = new HashMap<>();
		testEnv.put("JBP_CONFIG_JAVA_MAIN", "{arguments: '--args.0.test-param=test-param-value --modules=groupId:artifactId:jar:1.2.3 '}\n");
		testEnv.put("SPRING_CLOUD_DATAFLOW_MODULE", "test-group.test-label");

		when(this.ops.pushBindAndStartApplication(new Parameters.PushBindAndStartApplication()
				.withName("test-group" + "-" + "test-label")
				.withServiceInstanceNames(testServices)
				.withResource(testResource)
				.withInstances(1)
				.withEnvironment(testEnv)))
				.thenReturn(new Results.PushBindAndStartApplication().withCreateSucceeded(false));

		ModuleDefinition.Builder definitionBuilder = new ModuleDefinition.Builder();
		definitionBuilder.setGroup("test-group");
		definitionBuilder.setLabel("test-label");
		definitionBuilder.setName("test-name");
		definitionBuilder.setParameter("test-param", "test-param-value");
		ModuleDefinition definition = definitionBuilder.build();

		ModuleCoordinates.Builder coordsBuilder = new ModuleCoordinates.Builder();
		ModuleCoordinates coords = ModuleCoordinates.parse("groupId:artifactId:1.2.3");

		ModuleDeploymentRequest request = new ModuleDeploymentRequest(definition, coords);

		this.moduleDeployer.deploy(request);
	}

	@Test
	public void testStatusNone() throws Exception {

		when(this.ops.getApplicationsStatus(new Parameters.GetApplicationsStatus()))
				.thenReturn(new Results.GetApplicationsStatus());

		Map<ModuleDeploymentId, ModuleStatus> moduleStatus = this.moduleDeployer.status();

		assertEquals(0, moduleStatus.size());
	}

	@Test
	public void testStatusMany() throws Exception {

		whenGetApplicationsStatusTwo("test-group", "test-label", "test-app-id", "test-instance-id", "FLAPPING", "RUNNING");

		Map<ModuleDeploymentId, ModuleStatus> moduleStatus = this.moduleDeployer.status();

		ModuleDeploymentId modId1 = new ModuleDeploymentId("test-group1", "test-label1");
		ModuleDeploymentId modId2 = new ModuleDeploymentId("test-group2", "test-label2");

		assertEquals(2, moduleStatus.size());

		assertTrue(moduleStatus.containsKey(modId1));
		assertEquals(modId1, moduleStatus.get(modId1).getModuleDeploymentId());
		assertTrue(moduleStatus.get(modId1).getInstances().containsKey("test-app-id1:test-instance-id1"));
		assertEquals(ModuleStatus.State.deploying, moduleStatus.get(modId1).getState());

		assertTrue(moduleStatus.containsKey(modId2));
		assertEquals(modId2, moduleStatus.get(modId2).getModuleDeploymentId());
		assertTrue(moduleStatus.get(modId2).getInstances().containsKey("test-app-id2:test-instance-id2"));
		assertEquals(ModuleStatus.State.deployed, moduleStatus.get(modId2).getState());
	}

	@Test
	public void testStatus1NotFound() throws Exception {
		when(this.ops.getApplicationsStatus(new Parameters.GetApplicationsStatus().withName("test-group" + "-" + "test-label")))
				.thenReturn(new Results.GetApplicationsStatus());

		ModuleStatus moduleStatus = this.moduleDeployer.status(new ModuleDeploymentId("test-group", "test-label"));

		ModuleDeploymentId modId = new ModuleDeploymentId("test-group", "test-label");
		assertEquals(modId, moduleStatus.getModuleDeploymentId());
		assertEquals(ModuleStatus.State.unknown, moduleStatus.getState());
		assertEquals(Collections.emptyMap(), moduleStatus.getInstances());
	}

	@Test
	public void testStatus1() throws Exception {

		whenGetApplicationsStatus("test-group", "test-label", "test-app-id", "test-instance-id", "RUNNING");

		ModuleStatus moduleStatus = this.moduleDeployer.status(new ModuleDeploymentId("test-group", "test-label"));

		ModuleDeploymentId modId = new ModuleDeploymentId("test-group", "test-label");

		assertEquals(modId, moduleStatus.getModuleDeploymentId());
		assertTrue(moduleStatus.getInstances().containsKey("test-app-id:test-instance-id"));
		assertEquals(ModuleStatus.State.deployed, moduleStatus.getState());
	}

	@Test
	public void testUndeployFoundDeleted() throws Exception {

		when(this.ops.deleteApplication(new Parameters.DeleteApplication()
				.withName("test-group-test-label")))
				.thenReturn(new Results.DeleteApplication().withDeleted(true).withFound(true));

		this.moduleDeployer.undeploy(new ModuleDeploymentId("test-group", "test-label"));
	}

	@Test(expected = IllegalStateException.class)
	public void testUndeployNotFound() throws Exception {

		when(this.ops.deleteApplication(new Parameters.DeleteApplication()
				.withName("test-group-test-label")))
				.thenReturn(new Results.DeleteApplication().withDeleted(true).withFound(false));

		this.moduleDeployer.undeploy(new ModuleDeploymentId("test-group", "test-label"));
	}

	@Test(expected = IllegalStateException.class)
	public void testUndeployFoundNotDeleted() throws Exception {

		when(this.ops.deleteApplication(new Parameters.DeleteApplication()
				.withName("test-group-test-label")))
				.thenReturn(new Results.DeleteApplication().withDeleted(false).withFound(true));

		this.moduleDeployer.undeploy(new ModuleDeploymentId("test-group", "test-label"));
	}

	private void whenGetApplicationsStatus(String group, String label, String appId, String instanceId, String state) {
		when(this.ops.getApplicationsStatus(new Parameters.GetApplicationsStatus().withName(group + "-" + label)))
				.thenReturn(new Results.GetApplicationsStatus()
						.withApplication(group + "-" + label, new ApplicationStatus()
								.withId(appId)
								.withEnvironment(Collections.singletonMap("SPRING_CLOUD_DATAFLOW_MODULE", group + "." + label))
								.withInstances(Collections.singletonMap(instanceId, new Responses.ApplicationInstanceStatus()
										.withState(state)))));
	}

	private void whenGetApplicationsStatusTwo(String group, String label, String appId, String instanceId, String state1, String state2) {
		when(this.ops.getApplicationsStatus(new Parameters.GetApplicationsStatus()))
				.thenReturn(new Results.GetApplicationsStatus()
								.withApplication(group + 1 + "-" + label + 1, new ApplicationStatus()
										.withId(appId + 1)
										.withEnvironment(Collections.singletonMap("SPRING_CLOUD_DATAFLOW_MODULE", group + 1 + "." + label + 1))
										.withInstances(Collections.singletonMap(instanceId + 1, new Responses.ApplicationInstanceStatus()
												.withState(state1))))
								.withApplication(group + 2 + "-" + label + 2, new ApplicationStatus()
										.withId(appId + 2)
										.withEnvironment(Collections.singletonMap("SPRING_CLOUD_DATAFLOW_MODULE", group + 2 + "." + label + 2))
										.withInstances(Collections.singletonMap(instanceId + 2, new Responses.ApplicationInstanceStatus()
												.withState(state2))))
				);
	}
}
