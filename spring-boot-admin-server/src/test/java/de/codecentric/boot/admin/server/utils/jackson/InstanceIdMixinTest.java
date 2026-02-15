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

package de.codecentric.boot.admin.server.utils.jackson;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import de.codecentric.boot.admin.server.domain.values.InstanceId;

import static org.assertj.core.api.Assertions.assertThat;

class InstanceIdMixinTest {

	private final JsonMapper jsonMapper;

	protected InstanceIdMixinTest() {
		AdminServerModule adminServerModule = new AdminServerModule(new String[] { ".*password$" });
		jsonMapper = JsonMapper.builder()
			.addModule(adminServerModule)
			.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
			.build();
	}

	@Test
	void verifyDeserialize() throws JacksonException {
		InstanceId id = jsonMapper.readValue("\"abc\"", InstanceId.class);
		assertThat(id).isEqualTo(InstanceId.of("abc"));
	}

	@Test
	void verifySerialize() throws IOException {
		InstanceId id = InstanceId.of("abc");

		String result = jsonMapper.writeValueAsString(id);
		assertThat(result).isEqualTo("\"abc\"");
	}

}
