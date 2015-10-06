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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import org.springframework.cloud.dataflow.core.ModuleDefinition;
import org.springframework.cloud.dataflow.core.ModuleDeploymentId;
import org.springframework.cloud.dataflow.core.ModuleDeploymentRequest;
import org.springframework.cloud.dataflow.module.deployer.ModuleArgumentQualifier;

/**
 * Converts between Module information and Application information.
 * Sole owner of correspondence between module ids and application names;
 * also builds environment for the module launcher application.
 *
 * @author Steve Powell
 */
class CloudFoundryModuleDeploymentConverter {

	// No prefixing is necessary.
	private static final String APPLICATION_PREFIX = "";

	private static final String MODULE_ID_ENVIRONMENT_VARNAME = "SPRING_CLOUD_DATAFLOW_MODULE";

	ModuleDeploymentId getModuleId(ApplicationStatus applicationStatus) {
		Map<String, String> environment = applicationStatus.getEnvironment();
		if (environment != null) {
			String moduleIdString = environment.get(MODULE_ID_ENVIRONMENT_VARNAME);
			if (moduleIdString != null) {
				try {
					return ModuleDeploymentId.parse(moduleIdString);
				}
				catch (IllegalArgumentException e) {
					// We ignore invalid format cases
				}
			}
		}
		return null; // we ignore applications that don't have the right environment setting
	}

	String toApplicationName(ModuleDeploymentId moduleDeploymentId) {
		return APPLICATION_PREFIX + moduleDeploymentId.getGroup() + "-" + moduleDeploymentId.getLabel();
	}

	Map<String, String> generateModuleLauncherEnvironment(ModuleDeploymentRequest request) {
		Map<String, String> env = new HashMap<>();
		env.put(MODULE_ID_ENVIRONMENT_VARNAME, getModuleIdString(request.getDefinition()));
		Map<String, String> argEnvironment = argsToEnvironmentVariables(generateArguments(request));
		env.putAll(argEnvironment);
		return env;
	}

	private static String getModuleIdString(ModuleDefinition definition) {
		return ModuleDeploymentId.fromModuleDefinition(definition).toString();
	}

	private static Map<String, String> generateArguments(ModuleDeploymentRequest moduleDeploymentRequest) {
		Map<String, String> arguments = new HashMap<>();
		arguments.put("modules", moduleDeploymentRequest.getCoordinates().toString());
		arguments.putAll(ModuleArgumentQualifier.qualifyArgs(0, moduleDeploymentRequest.getDefinition().getParameters()));
		arguments.putAll(ModuleArgumentQualifier.qualifyArgs(0, moduleDeploymentRequest.getDeploymentProperties()));
		return arguments;
	}

	/*
	 * WORKAROUND for spring binding not working well when dealing with props that bind to a Map and using underscores.
	 * What we'd like is the commented out code, with following javadoc. What we currently do though, is to pass the
	 * args as (dotted) "command line args", all in a special CF Buildpack ENV var.
	 * see https://github.com/cloudfoundry/java-buildpack/blob/81f993c5bdcdaca6a28ad6970a6d1144236f3f6d/docs/container-java_main.md#configuration
	 *
	 * Produce a collection of environment variables from arguments intended for the application.
	 * The keys are converted into environment variable names where dots are replaced by underscores,
	 * and the alphabetic characters are upper-cased. Any resulting name clashes will result in entry losses.
	 */
	private static Map<String, String> argsToEnvironmentVariables(Map<String, String> args) {
		//      Map<String, String> env = new HashMap<>(args.size());
		//      for (Map.Entry<String, String> entry : args.entrySet()) {
		//          env.put(entry.getKey().toUpperCase().replace('.', '_'), entry.getValue());
		//      }
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : args.entrySet()) {
			sb.append("--").append(entry.getKey()).append('=').append(entry.getValue()).append(' ');
		}
		String asYaml = new Yaml().dump(Collections.singletonMap("arguments", sb.toString()));
		return Collections.singletonMap("JBP_CONFIG_JAVA_MAIN", asYaml);
	}
}
