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

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;

@lombok.Data
@ConfigurationProperties(prefix = "spring.boot.admin.client")
public class ClientProperties {

	/**
	 * The admin server urls to register at
	 */
	private String[] url = new String[] {};

	/**
	 * The admin rest-apis path.
	 */
	private String apiPath = "instances";

	/**
	 * Time interval the registration is repeated
	 */
	@DurationUnit(ChronoUnit.MILLIS)
	private Duration period = Duration.ofMillis(10_000L);

	/**
	 * Connect timeout for the registration.
	 */
	@DurationUnit(ChronoUnit.MILLIS)
	private Duration connectTimeout = Duration.ofMillis(5_000L);

	/**
	 * Read timeout (in ms) for the registration.
	 */
	@DurationUnit(ChronoUnit.MILLIS)
	private Duration readTimeout = Duration.ofMillis(5_000L);

	/**
	 * Username for basic authentication on admin server
	 */
	@Nullable
	private String username;

	/**
	 * Password for basic authentication on admin server
	 */
	@Nullable
	private String password;

	/**
	 * Enable automatic deregistration on shutdown If not set it defaults to true if a
	 * active {@link CloudPlatform} is present;
	 */
	@Nullable
	private Boolean autoDeregistration = null;

	/**
	 * Enable automatic registration when the application is ready.
	 */
	private boolean autoRegistration = true;

	/**
	 * Enable registration against one or all admin servers
	 */
	private boolean registerOnce = true;

	/**
	 * Enable Spring Boot Admin Client.
	 */
	private boolean enabled = true;

	public String[] getAdminUrl() {
		String[] adminUrls = this.url.clone();
		for (int i = 0; i < adminUrls.length; i++) {
			adminUrls[i] += "/" + this.apiPath;
		}
		return adminUrls;
	}

	public boolean isAutoDeregistration(Environment environment) {
		return (this.autoDeregistration != null) ? this.autoDeregistration
				: (CloudPlatform.getActive(environment) != null);
	}

}
