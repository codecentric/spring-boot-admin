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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import de.codecentric.boot.admin.server.domain.values.BuildVersion;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

class BuildVersionMixinTest {

	private final JsonMapper objectMapper;

	protected BuildVersionMixinTest() {
		AdminServerModule adminServerModule = new AdminServerModule(new String[]{".*password$"});
		objectMapper = JsonMapper.builder()
			.addModule(adminServerModule)
			.build();
	}

	@Test
	void verifyDeserialize() throws JsonProcessingException {
		BuildVersion buildVersion = objectMapper.readValue("\"1.0.0\"", BuildVersion.class);
		assertThat(buildVersion).isEqualTo(BuildVersion.valueOf("1.0.0"));
	}

	@Test
	void verifySerialize() throws JsonProcessingException {
		BuildVersion buildVersion = BuildVersion.valueOf("1.0.0");

		String result = objectMapper.writeValueAsString(buildVersion);
		assertThat(result).isEqualTo("\"1.0.0\"");
	}

	@Test
	void verifySerializeWithMapEntryVersion() throws JsonProcessingException {
		BuildVersion buildVersion = BuildVersion.from(singletonMap("version", "1.0.0"));

		String result = objectMapper.writeValueAsString(buildVersion);
		assertThat(result).isEqualTo("\"1.0.0\"");
	}

	@Test
	void verifySerializeWithNestedMapEntryVersion() throws JsonProcessingException {
		BuildVersion buildVersion = BuildVersion.from(singletonMap("build", singletonMap("version", "1.0.0")));

		String result = objectMapper.writeValueAsString(buildVersion);
		assertThat(result).isEqualTo("\"1.0.0\"");
	}

}
