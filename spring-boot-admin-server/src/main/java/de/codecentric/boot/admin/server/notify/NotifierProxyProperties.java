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

package de.codecentric.boot.admin.server.notify;

import org.springframework.boot.context.properties.ConfigurationProperties;

@lombok.Data
@ConfigurationProperties("spring.boot.admin.notify.proxy")
public class NotifierProxyProperties {

	/**
	 * Proxy-Host for sending notifications
	 */
	private String host;

	/**
	 * Proxy-Port for sending notifications
	 */
	private int port;

	/**
	 * Proxy-User for sending notifications (if proxy requires authentication).
	 */
	private String username;

	/**
	 * Proxy-Password for sending notifications (if proxy requires authentication).
	 */
	private String password;

}
