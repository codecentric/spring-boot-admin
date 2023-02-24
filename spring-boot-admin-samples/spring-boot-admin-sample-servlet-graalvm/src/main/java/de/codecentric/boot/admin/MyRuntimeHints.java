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

import java.util.Set;

import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import de.codecentric.boot.admin.server.ui.config.AdminServerUiProperties;
import de.codecentric.boot.admin.server.ui.web.UiController;

import static org.springframework.util.ReflectionUtils.findMethod;

public class MyRuntimeHints implements RuntimeHintsRegistrar {

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
				findMethod(UiController.Settings.class, "getViewSettings"),
				findMethod(UiController.Settings.class, "getEnableToasts"),
				findMethod(AdminServerUiProperties.UiTheme.class, "getPalette"),
				findMethod(AdminServerUiProperties.UiTheme.class, "getColor") })
				.forEach((method) -> hints.reflection().registerMethod(method, ExecutableMode.INVOKE));

		// Register resources
		hints.resources().registerPattern("META-INF/spring-boot-admin-server-ui/**.*");

		/*
		 * // Register serialization
		 * hints.serialization().registerType(MySerializableClass.class);
		 *
		 * // Register proxy hints.proxies().registerJdkProxy(MyInterface.class);
		 */
	}

}
