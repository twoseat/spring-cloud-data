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

import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.data.core.ModuleCoordinates;
import org.springframework.cloud.data.core.ModuleDefinition;
import org.springframework.cloud.data.core.ModuleDeploymentId;
import org.springframework.cloud.data.core.ModuleDeploymentRequest;
import org.springframework.cloud.data.module.ModuleInstanceStatus;
import org.springframework.cloud.data.module.ModuleStatus;
import org.springframework.cloud.data.module.deployer.ModuleDeployer;
import org.springframework.context.ApplicationContext;

/**
 * This is a convenience class used to try out this implementation of the deployer until it's correctly wired in
 * to the overall app. It must be discarded at that time.
 *
 * @author Steve Powell
 */
final class TestApplication {

	public static void main(String[] args) {
		ApplicationContext applicationContext = SpringApplication.run(CloudFoundryModuleDeployerConfiguration.class, args);

		ModuleDeployer moduleDeployer = applicationContext.getBean(ModuleDeployer.class);

		// Get current state
		Map<ModuleDeploymentId, ModuleStatus> mds = moduleDeployer.status();
		System.out.println("\nmoduleDeployer.status() ==> " + prettyPrint(mds));

		// Get one of the current ModuleDeploymentIds
		ModuleDeploymentId[] moduleDeploymentIds = mds.keySet().toArray(new ModuleDeploymentId[mds.size()]);
		for (ModuleDeploymentId mdid : moduleDeploymentIds) {
			moduleDeployer.undeploy(mdid);
		}

		System.out.println("\nmoduleDeployer.status() ==> " + prettyPrint(moduleDeployer.status()));

		ModuleDefinition md = new ModuleDefinition.Builder().setName("time").setLabel("time")
				.setGroup("ticktock")
				.setParameter("fixedDelay", "1")
				.build();
		ModuleDeploymentRequest mdr = new ModuleDeploymentRequest(md, ModuleCoordinates.parse("org.springframework.cloud.stream.module:time-source:1.0.0.BUILD-SNAPSHOT"));

		ModuleDeploymentId moduleDeploymentId = moduleDeployer.deploy(mdr);

		ModuleDefinition md2 = new ModuleDefinition.Builder().setName("log").setLabel("log")
				.setGroup("ticktock")
				.setParameter("fixedDelay", "1")
				.build();
		ModuleDeploymentRequest mdr2 = new ModuleDeploymentRequest(md2, ModuleCoordinates.parse("org.springframework.cloud.stream.module:log-sink:1.0.0.BUILD-SNAPSHOT"));

		moduleDeployer.deploy(mdr2);


		System.out.println("\nmoduleDeployer.status() ==> " + prettyPrint(moduleDeployer.status()));

//        ModuleStatus ms = moduleDeployer.status(new ModuleDeploymentId("a", "Steve"));
//        System.out.println("\nmoduleDeployer.status(MDI(\"a\",\"Steve\")) ==> " + ms.getState());
//        System.out.println("\ninstance(s) ==> " + iPrettyPrint(ms.getInstances()));
//
//        ms = moduleDeployer.status(new ModuleDeploymentId("a", "Steve2"));
//        System.out.println("\nmoduleDeployer.status(MDI(\"a\",\"Steve2\")) ==> " + ms.getState());
//        System.out.println("\ninstance(s) ==> " + iPrettyPrint(ms.getInstances()));
//
//        ms = moduleDeployer.status(new ModuleDeploymentId("b", "Steve"));
//        System.out.println("\nmoduleDeployer.status(MDI(\"b\",\"Steve\")) ==> " + ms.getState());
//        System.out.println("\ninstance(s) ==> " + iPrettyPrint(ms.getInstances()));
//
//		CloudFoundryApplicationOperations resourceClient = applicationContext.getBean(CloudFoundryApplicationOperations.class);
//
//		resourceClient.getApplicationsStatus(new GetApplicationsStatusParameters().withName())
//
//		CloudControllerRestClient client = applicationContext.getBean(CloudControllerRestClient.class);
//
//
//		GetApplicationStatisticsResponse gasr = client.getApplicationStatistics(new GetApplicationStatisticsRequest().withId("8a4ac8c5-ebff-4bb0-8e47-972e47ee2674"));


//		client.uploadBits(new UploadBitsRequest()
//				.withId("b2fe9e68-7124-4489-98d0-6ea47be10a14")
//				.withResource(new FileSystemResource("/Users/spowell/Downloads/java-main-application-1.0.0.BUILD-SNAPSHOT.jar")));

//		CloudFoundryApplicationOperations resourceClient = applicationContext.getBean(CloudFoundryApplicationOperations.class);
//		PushApplicationParameters pushApplicationRequest = new PushApplicationParameters().withName("hello")
//				.withResource(new FileSystemResource("/Users/spowell/Downloads/java-main-application-1.0.0.BUILD-SNAPSHOT.jar"))
//				.withEnvironment(Collections.singletonMap("foo", "bar"));
//		resourceClient.pushApplication(pushApplicationRequest);

//        moduleDeployer.undeploy(??);

		System.out.println();

	}

	private static String iPrettyPrint(Map<String, ModuleInstanceStatus> mis) {
		StringBuilder sb = new StringBuilder("\n   {\n");
		for (Map.Entry<String, ModuleInstanceStatus> e : mis.entrySet()) {
			sb.append("    ").append(e.getKey()).append(" : ").append(e.getValue().toString()).append('\n');
		}
		return sb.append("   }").toString();
	}

	private static String prettyPrint(Map<ModuleDeploymentId, ModuleStatus> mds) {
		StringBuilder sb = new StringBuilder("\n {");
		for (Map.Entry<ModuleDeploymentId, ModuleStatus> e : mds.entrySet()) {
			ModuleStatus ms = e.getValue();
			sb.append("\n  ").append(e.getKey()).append(" : ").append(ms.getState());
			sb.append(iPrettyPrint(ms.getInstances()));
		}
		return sb.append("\n }").toString();
	}

}
