/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.client.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.cloud.CloudFoundryVcapEnvironmentPostProcessor;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.logging.DeferredLogs;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

public class CloudFoundryApplicationPropertiesTest {

	@Test
	public void bind() {
		String vcap = "{\"application_users\":[]," + "\"application_id\":\"9958288f-9842-4ddc-93dd-1ea3c90634cd\","
				+ "\"instance_id\":\"bb7935245adf3e650dfb7c58a06e9ece\","
				+ "\"instance_index\":0,\"version\":\"3464e092-1c13-462e-a47c-807c30318a50\","
				+ "\"name\":\"foo\",\"uris\":[\"foo.cfapps.io\"]," + "\"started_at\":\"2013-05-29 02:37:59 +0000\","
				+ "\"started_at_timestamp\":1369795079," + "\"host\":\"0.0.0.0\",\"port\":61034,"
				+ "\"limits\":{\"mem\":128,\"disk\":1024,\"fds\":16384},"
				+ "\"version\":\"3464e092-1c13-462e-a47c-807c30318a50\","
				+ "\"name\":\"dsyerenv\",\"uris\":[\"dsyerenv.cfapps.io\"],"
				+ "\"users\":[],\"start\":\"2013-05-29 02:37:59 +0000\"," + "\"state_timestamp\":1369795079}";

		MockEnvironment env = new MockEnvironment();
		env.setProperty("VCAP_APPLICATION", vcap);
		new CloudFoundryVcapEnvironmentPostProcessor(new DeferredLogs()).postProcessEnvironment(env, null);

		CloudFoundryApplicationProperties cfProperties = Binder.get(env)
			.bind("vcap.application", Bindable.of(CloudFoundryApplicationProperties.class))
			.get();
		assertThat(cfProperties.getApplicationId()).isEqualTo("9958288f-9842-4ddc-93dd-1ea3c90634cd");
		assertThat(cfProperties.getInstanceIndex()).isEqualTo("0");
	}

}
