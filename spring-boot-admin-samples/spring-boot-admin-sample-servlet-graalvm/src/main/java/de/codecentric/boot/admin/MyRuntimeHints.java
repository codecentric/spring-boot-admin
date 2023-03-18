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

package de.codecentric.boot.admin;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ser.std.ClassSerializer;
import com.fasterxml.jackson.databind.ser.std.FileSerializer;
import com.fasterxml.jackson.databind.ser.std.StdJdkSerializers;
import com.fasterxml.jackson.databind.ser.std.TokenBufferSerializer;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeHint;
import org.springframework.aot.hint.TypeReference;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Configuration;

import de.codecentric.boot.admin.client.registration.Application;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.ui.config.AdminServerUiProperties;
import de.codecentric.boot.admin.server.ui.web.UiController;

import static org.springframework.util.ReflectionUtils.findMethod;

@Configuration
@RegisterReflectionForBinding({ de.codecentric.boot.admin.server.domain.entities.Application.class, Application.class })
public class MyRuntimeHints implements RuntimeHintsRegistrar {

	@lombok.SneakyThrows
	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		// Register method for reflection
		Set.of(new java.lang.reflect.Method[] { findMethod(UiController.Settings.class, "getTitle"),
				findMethod(UiController.Settings.class, "getBrand"),
				findMethod(UiController.Settings.class, "getLoginIcon"),
				findMethod(UiController.Settings.class, "getFavicon"),
				findMethod(UiController.Settings.class, "getFaviconDanger"),
				findMethod(UiController.Settings.class, "getPollTimer"),
				findMethod(UiController.Settings.class, "getTheme"),
				findMethod(UiController.Settings.class, "isNotificationFilterEnabled"),
				findMethod(UiController.Settings.class, "isRememberMeEnabled"),
				findMethod(UiController.Settings.class, "getAvailableLanguages"),
				findMethod(UiController.Settings.class, "getRoutes"),
				findMethod(UiController.Settings.class, "getExternalViews"),
				findMethod(UiController.Settings.class, "getViewSettings"), findMethod(UiController.class, "index"),
				findMethod(AdminServerUiProperties.UiTheme.class, "getPalette"),
				findMethod(AdminServerUiProperties.UiTheme.class, "getColor") })
			.forEach((method) -> hints.reflection().registerMethod(method, ExecutableMode.INVOKE));

		// Register resources
		hints.resources()
			.registerPattern("**/spring-boot-admin-server-ui/**.*")
			.registerPattern("**/sba-settings.js")
			.registerPattern("**/variables.css");

		hints.reflection()
			.registerType(Application.Builder.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(Application.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(Endpoint.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(Instance.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(InstanceId.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(AdminServerUiProperties.Palette.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerConstructor(Application.Builder.class.getDeclaredConstructor(), ExecutableMode.INVOKE)
			.registerMethod(Application.Builder.class.getMethod("build"), ExecutableMode.INVOKE)
			.registerMethod(Application.class.getMethod("builder"), ExecutableMode.INVOKE)
			.registerConstructor(Registration.class.getDeclaredConstructor(String.class, String.class, String.class,
					String.class, String.class, Map.class), ExecutableMode.INVOKE)
			.registerConstructor(Registration.Builder.class.getDeclaredConstructor(), ExecutableMode.INVOKE)
			.registerMethod(Registration.Builder.class.getMethod("build"), ExecutableMode.INVOKE)
			.registerMethod(Registration.class.getMethod("toBuilder"), ExecutableMode.INVOKE)
			.registerTypes(TypeReference.listOf(StdJdkSerializers.AtomicBooleanSerializer.class,
					StdJdkSerializers.AtomicIntegerSerializer.class, StdJdkSerializers.AtomicLongSerializer.class,
					FileSerializer.class, ClassSerializer.class, TokenBufferSerializer.class),
					TypeHint.builtWith(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS));

		hints.serialization().registerType(Registration.class).registerType(InstanceId.class);
	}

}
