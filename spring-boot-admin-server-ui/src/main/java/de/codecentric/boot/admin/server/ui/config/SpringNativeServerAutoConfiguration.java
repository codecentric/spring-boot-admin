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

package de.codecentric.boot.admin.server.ui.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

import de.codecentric.boot.admin.server.config.AdminServerMarkerConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerWebConfiguration;
import de.codecentric.boot.admin.server.config.SpringBootAdminServerEnabledCondition;

@Configuration(proxyBeanMethods = false)
@Conditional(SpringBootAdminServerEnabledCondition.class)
@ConditionalOnBean(AdminServerMarkerConfiguration.Marker.class)
@AutoConfigureAfter(AdminServerWebConfiguration.class)
@ImportRuntimeHints({ ServerRuntimeHints.class })
public class SpringNativeServerAutoConfiguration {

}
