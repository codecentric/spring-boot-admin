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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ser.std.ClassSerializer;
import com.fasterxml.jackson.databind.ser.std.FileSerializer;
import com.fasterxml.jackson.databind.ser.std.StdJdkSerializers;
import com.fasterxml.jackson.databind.ser.std.TokenBufferSerializer;
import lombok.SneakyThrows;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeHint;
import org.springframework.aot.hint.TypeReference;
import org.springframework.context.annotation.Configuration;

import de.codecentric.boot.admin.client.registration.Application;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEndpointsDetectedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceInfoChangedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegistrationUpdatedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.BuildVersion;
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.domain.values.Tags;
import de.codecentric.boot.admin.server.ui.config.AdminServerUiProperties;
import de.codecentric.boot.admin.server.ui.config.CssColorUtils;
import de.codecentric.boot.admin.server.ui.web.UiController;
import de.codecentric.boot.admin.server.utils.jackson.BuildVersionMixin;
import de.codecentric.boot.admin.server.utils.jackson.EndpointMixin;
import de.codecentric.boot.admin.server.utils.jackson.EndpointsMixin;
import de.codecentric.boot.admin.server.utils.jackson.InfoMixin;
import de.codecentric.boot.admin.server.utils.jackson.InstanceDeregisteredEventMixin;
import de.codecentric.boot.admin.server.utils.jackson.InstanceEndpointsDetectedEventMixin;
import de.codecentric.boot.admin.server.utils.jackson.InstanceEventMixin;
import de.codecentric.boot.admin.server.utils.jackson.InstanceIdMixin;
import de.codecentric.boot.admin.server.utils.jackson.InstanceInfoChangedEventMixin;
import de.codecentric.boot.admin.server.utils.jackson.InstanceRegisteredEventMixin;
import de.codecentric.boot.admin.server.utils.jackson.InstanceRegistrationUpdatedEventMixin;
import de.codecentric.boot.admin.server.utils.jackson.InstanceStatusChangedEventMixin;
import de.codecentric.boot.admin.server.utils.jackson.StatusInfoMixin;
import de.codecentric.boot.admin.server.utils.jackson.TagsMixin;
import de.codecentric.boot.admin.server.web.InstanceWebProxy;

import static org.springframework.util.ReflectionUtils.findMethod;

@Configuration
public class RuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(org.springframework.aot.hint.RuntimeHints hints, ClassLoader classLoader) {
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

	@SneakyThrows
	private static void registerReflectionHints(org.springframework.aot.hint.RuntimeHints hints) {
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

		Class<?> queryEndpointStrategyResponse = Class
			.forName("de.codecentric.boot.admin.server.services.endpoints.QueryIndexEndpointStrategy$Response");
		Class<?> queryEndpointStrategyResponseEndpointRef = Class.forName(
				"de.codecentric.boot.admin.server.services.endpoints.QueryIndexEndpointStrategy$Response$EndpointRef");

		hints.reflection()
			.registerType(queryEndpointStrategyResponse, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(queryEndpointStrategyResponseEndpointRef, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(Application.Builder.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(InstanceDeregisteredEvent.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(InstanceEndpointsDetectedEvent.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(InstanceEvent.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(InstanceInfoChangedEvent.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(InstanceRegisteredEvent.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(InstanceRegistrationUpdatedEvent.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(InstanceStatusChangedEvent.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(InstanceId.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(Application.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(Endpoint.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(Instance.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(InstanceId.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(InstanceWebProxy.InstanceResponse.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(InstanceWebProxy.ForwardRequest.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(AdminServerUiProperties.Palette.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(CssColorUtils.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)

			.registerType(BuildVersionMixin.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(EndpointMixin.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(EndpointsMixin.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(InfoMixin.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(InstanceDeregisteredEventMixin.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(InstanceEndpointsDetectedEventMixin.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(InstanceEventMixin.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(InstanceIdMixin.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(InstanceInfoChangedEventMixin.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(InstanceRegisteredEventMixin.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(InstanceRegistrationUpdatedEventMixin.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(InstanceStatusChangedEventMixin.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(StatusInfoMixin.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
			.registerType(TagsMixin.class, MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)

			.registerConstructor(queryEndpointStrategyResponse.getConstructors()[0], ExecutableMode.INVOKE)
			.registerConstructor(
					queryEndpointStrategyResponseEndpointRef.getDeclaredConstructor(String.class, boolean.class),
					ExecutableMode.INVOKE)
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
	}

}
