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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.cloudfoundry.client.lib.domain.InstanceInfo;
import org.cloudfoundry.client.lib.domain.InstanceStats;
import org.junit.Test;

import org.springframework.cloud.dataflow.module.ModuleInstanceStatus;
import org.springframework.cloud.dataflow.module.ModuleStatus;

/**
 * @author Steve Powell
 */
public class CloudFoundryModuleInstanceStatusTest {

	private static final String TEST_TIME = "1953-06-04 06:00:00 +0000";

	private static final double TEST_CPU = 1.1e10D;

	private static final int TEST_DISK = 2345;

	private static final int TEST_MEMORY = 1234;

	private static final long TEST_DISK_QUOTA = 9999l;

	private static final int TEST_DEBUG_PORT = 8888;

	private static final int TEST_PORT = 7777;

	private static final long TEST_MEM_QUOTA = 6666l;

	private static final String TEST_URI = "test.uri";

	private static final int TEST_FDS_QUOTA = 5555;

	private static final String TEST_HOST = "test.host";

	private static final double TEST_UPTIME = 2.2e20D;

	private InstanceInfo ii1 = new InstanceInfo(generateInfoMap(1));

	private InstanceStats is1 = new InstanceStats("1", generateStatsMap(1));

	private ModuleInstanceStatus mis1 = new CloudFoundryModuleInstanceStatus("test-app-name1", 11);

	private ModuleInstanceStatus mis2 = new CloudFoundryModuleInstanceStatus("test-app-name2", ii1, is1);

	@Test
	public void testGetId() throws Exception {
		assertEquals("Id is unexpected", "test-app-name1:11", mis1.getId());
		assertEquals("Id is unexpected", "test-app-name2:1", mis2.getId());
	}

	@Test
	public void testGetState() throws Exception {
		assertEquals("Unexpected state", ModuleStatus.State.failed, mis1.getState());
		assertEquals("Unexpected state", ModuleStatus.State.deployed, mis2.getState());
	}

	@Test
	public void testGetAttributes() throws Exception {
		assertEquals("Attributes present when they shouldn't be", Collections.<String, String>emptyMap(), mis1.getAttributes());

		Map<String, String> expectedAttributes = new HashMap<>();
		expectedAttributes.put("disk_quota", "9999");
		expectedAttributes.put("fds_quota", "5555");
		expectedAttributes.put("host", "test.host");
		expectedAttributes.put("mem_quota", "6666");
		expectedAttributes.put("name", "test-stats-name-1");
		expectedAttributes.put("port", "7777");
		expectedAttributes.put("uptime", "2.2E20");
		expectedAttributes.put("uris", "test.uri,test.uri.two");
		expectedAttributes.put("usage.cpu", "1.1E10");
		expectedAttributes.put("usage.disk", "2345");
		expectedAttributes.put("usage.memory", "1234");
		expectedAttributes.put("usage.time", "1953-06-04 06:00:00");

		assertEquals("Attributes incorrect", expectedAttributes, mis2.getAttributes());
	}

	private static Map<String, Object> generateInfoMap(int index) {
		Map<String, Object> infoMap = new HashMap<>();
		infoMap.put("since", new Long(index * 123));
		infoMap.put("index", index);
		infoMap.put("state", "RUNNING");
		infoMap.put("debug_ip", "test.debug.ip");
		infoMap.put("debug_port", 8888);
		return infoMap;
	}

	private static Map<String, Object> generateStatsMap(int index) {
		Map<String, Object> statsMap = new HashMap<>();
		statsMap.put("state", "irrelevant");
		statsMap.put("stats", generateInnerStatsMap(index));
		statsMap.put("index", index);
		statsMap.put("debug_ip", "test.debug.ip");
		statsMap.put("debug_port", TEST_DEBUG_PORT);
		return statsMap;
	}

	private static Map<String, Object> generateInnerStatsMap(int index) {
		Map<String, Object> iStatsMap = new HashMap<>();
		iStatsMap.put("cores", 2);
		iStatsMap.put("name", "test-stats-name-" + index);
		iStatsMap.put("usage", generateUsageMap(index));
		iStatsMap.put("disk_quota", TEST_DISK_QUOTA);
		iStatsMap.put("port", TEST_PORT);
		iStatsMap.put("mem_quota", TEST_MEM_QUOTA);
		iStatsMap.put("uris", Arrays.asList(new String[] {TEST_URI, TEST_URI + ".two"}));
		iStatsMap.put("fds_quota", TEST_FDS_QUOTA);
		iStatsMap.put("host", TEST_HOST);
		iStatsMap.put("uptime", TEST_UPTIME);
		return iStatsMap;
	}

	private static Map<String, Object> generateUsageMap(int index) {
		Map<String, Object> usageMap = new HashMap<>();
		usageMap.put("time", TEST_TIME);
		usageMap.put("cpu", TEST_CPU);
		usageMap.put("disk", TEST_DISK);
		usageMap.put("mem", TEST_MEMORY);
		return usageMap;
	}
}
