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

import lombok.SneakyThrows;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.annotation.Configuration;

import de.codecentric.boot.admin.client.registration.Application;
import de.codecentric.boot.admin.client.registration.DefaultApplicationFactory;

@Configuration
public class ClientRuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		registerReflectionHints(hints);
	}

	@SneakyThrows
	private static void registerReflectionHints(RuntimeHints hints) {
		hints.reflection()
			.registerType(Application.Builder.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(Application.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerConstructor(Application.Builder.class.getDeclaredConstructor(), ExecutableMode.INVOKE)
			.registerMethod(Application.Builder.class.getMethod("build"), ExecutableMode.INVOKE)
			.registerMethod(Application.class.getMethod("builder"), ExecutableMode.INVOKE)
			.registerMethod(DefaultApplicationFactory.class.getMethod("onWebServerInitialized",
					WebServerInitializedEvent.class), ExecutableMode.INVOKE);
	}

}
