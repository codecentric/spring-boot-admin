/*
 * Copyright 2014-2019 the original author or authors.
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

package de.codecentric.boot.admin.server.ui.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.http.CacheControl;

import de.codecentric.boot.admin.server.ui.web.UiController;

@lombok.Data
@ConfigurationProperties("spring.boot.admin.ui")
public class AdminServerUiProperties {

	private static final String[] CLASSPATH_RESOURCE_LOCATIONS = { "classpath:/META-INF/spring-boot-admin-server-ui/" };

	private static final String[] CLASSPATH_EXTENSION_RESOURCE_LOCATIONS = {
			"classpath:/META-INF/spring-boot-admin-server-ui/extensions/" };

	/**
	 * Locations of SBA ui resources.
	 */
	private String[] resourceLocations = CLASSPATH_RESOURCE_LOCATIONS;

	/**
	 * Locations of SBA ui exentsion resources.
	 */
	private String[] extensionResourceLocations = CLASSPATH_EXTENSION_RESOURCE_LOCATIONS;

	/**
	 * Locations of SBA ui template.
	 */
	private String templateLocation = CLASSPATH_RESOURCE_LOCATIONS[0];

	/**
	 * Icon used as image on login page
	 */
	private String loginIcon = "assets/img/icon-spring-boot-admin.svg";

	/**
	 * Icon used as default favicon and icon for desktop notifications.
	 */
	private String favicon = "assets/img/favicon.png";

	/**
	 * Icon used as default favicon and icon for desktop notifications.
	 */
	private String faviconDanger = "assets/img/favicon-danger.png";

	/**
	 * Page-Title to be shown.
	 */
	private String title = "Spring Boot Admin";

	/**
	 * Brand to be shown in then navbar.
	 */
	private String brand = "<img src=\"assets/img/icon-spring-boot-admin.svg\"><span>Spring Boot Admin</span>";

	/**
	 * If running behind a reverse proxy (using path rewriting) this can be used to output
	 * correct self references. If the host/port is omitted it will be inferred from the
	 * request.
	 */
	@Nullable
	private String publicUrl = null;

	/**
	 * Wether the thymeleaf templates should be cached.
	 */
	private boolean cacheTemplates = true;

	/**
	 * Cache-Http-Header settings.
	 */
	private Cache cache = new Cache();

	/**
	 * External views shown in the navbar.
	 */
	private List<UiController.ExternalView> externalViews = new ArrayList<>();

	/**
	 * External views shown in the navbar.
	 */
	private List<UiController.ViewSettings> viewSettings = new ArrayList<>();

	/**
	 * Whether the option to remember a user should be available.
	 */
	private boolean rememberMeEnabled = true;

	/**
	 * Limit languages to this list. Intersection of all supported languages and this list
	 * will be used.
	 */
	private List<String> availableLanguages = new ArrayList<>();

	private PollTimer pollTimer = new PollTimer();

	@lombok.Data
	public static class PollTimer {

		/**
		 * Time in milliseconds to refresh data in caches view.
		 */
		private int cache = 2500;

		/**
		 * Time in milliseconds to refresh data in datasource view.
		 */
		private int datasource = 2500;

		/**
		 * Time in milliseconds to refresh data in gc view.
		 */
		private int gc = 2500;

		/**
		 * Time in milliseconds to refresh data in process view.
		 */
		private int process = 2500;

		/**
		 * Time in milliseconds to refresh data in memory view.
		 */
		private int memory = 2500;

		/**
		 * Time in milliseconds to refresh data in threads view.
		 */
		private int threads = 2500;

	}

	@lombok.Data
	public static class Cache {

		/**
		 * include "max-age" directive in Cache-Control http header.
		 */
		@Nullable
		@DurationUnit(ChronoUnit.SECONDS)
		private Duration maxAge = Duration.ofSeconds(3600);

		/**
		 * include "no-cache" directive in Cache-Control http header.
		 */
		private Boolean noCache = false;

		/**
		 * include "no-store" directive in Cache-Control http header.
		 */
		private Boolean noStore = false;

		public CacheControl toCacheControl() {
			if (Boolean.TRUE.equals(this.noStore)) {
				return CacheControl.noStore();
			}
			if (Boolean.TRUE.equals(this.noCache)) {
				return CacheControl.noCache();
			}
			if (this.maxAge != null) {
				return CacheControl.maxAge(this.maxAge.getSeconds(), TimeUnit.SECONDS);
			}
			return CacheControl.empty();
		}

	}

}
