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

package de.codecentric.boot.admin.server.web.client;

import org.junit.jupiter.api.Test;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;

import static org.assertj.core.api.Assertions.assertThat;

public class CloudFoundryHttpHeaderProviderTest {

	private CloudFoundryHttpHeaderProvider headersProvider = new CloudFoundryHttpHeaderProvider();

	@Test
	public void test_cloud_foundry_header() {
		Registration registration = Registration.create("foo", "http://health")
			.metadata("applicationId", "549e64cf-a478-423d-9d6d-02d803a028a8")
			.metadata("instanceId", "0")
			.build();
		Instance instance = Instance.create(InstanceId.of("id")).register(registration);
		assertThat(headersProvider.getHeaders(instance).get("X-CF-APP-INSTANCE"))
			.containsOnly("549e64cf-a478-423d-9d6d-02d803a028a8:0");
	}

	@Test
	public void test_no_header() {
		Registration registration = Registration.create("foo", "http://health").build();
		Instance instance = Instance.create(InstanceId.of("id")).register(registration);
		assertThat(headersProvider.getHeaders(instance)).isEmpty();
	}

}
