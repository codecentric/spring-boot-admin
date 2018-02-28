/*
 * Copyright 2014-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.client.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class CloudFoundryEnvironmentPostProcessor implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (CloudPlatform.CLOUD_FOUNDRY.isActive(environment)) {
            Map<String, Object> map = new HashMap<>();
            map.put("spring.boot.admin.client.auto-deregistration", "true");
            MapPropertySource propertySource = new MapPropertySource(
                "cloudConfigPropertySource", map);
            environment.getPropertySources().addFirst(propertySource);
        }
    }
}
