/*
 * Copyright 2014-2020 the original author or authors.
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

package de.codecentric.boot.admin.server.config;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.BuildVersion;
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.domain.values.Tags;

@Configuration
@Conditional(SpringBootAdminServerEnabledCondition.class)
// TODO find the right condition (e.g. property? profile? ???)
public class SpringNativeAutoConfiguration implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		registerReflectionHints(hints);

		registerResourcesHints(hints);

		registerSerializationHints(hints);
	}

	private static void registerSerializationHints(org.springframework.aot.hint.RuntimeHints hints) {
		hints.serialization()
			.registerType(HashMap.class)
			.registerType(ArrayList.class)
			.registerType(Registration.class)
			.registerType(InstanceId.class)
			.registerType(Instance.class)
			.registerType(BuildVersion.class)
			.registerType(Endpoint.class)
			.registerType(Endpoints.class)
			.registerType(Info.class)
			.registerType(StatusInfo.class)
			.registerType(Tags.class);
	}

	private static void registerResourcesHints(org.springframework.aot.hint.RuntimeHints hints) {
		hints.resources()
			.registerPattern("**/spring-boot-admin-server-ui/**.*")
			.registerPattern("**/sba-settings.js")
			.registerPattern("**/variables.css");
	}

	private static void registerReflectionHints(org.springframework.aot.hint.RuntimeHints hints) {
		// TODO: add RegisterReflectionForBinding Annotations etc.
	}

}
