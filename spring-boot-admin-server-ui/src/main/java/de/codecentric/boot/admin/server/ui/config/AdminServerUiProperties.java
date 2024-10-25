/*
 * Copyright 2014-2024 the original author or authors.
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

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.http.CacheControl;
import org.springframework.lang.Nullable;

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

	/**
	 * Additional routes to exclude from home page redirecting filter. Requests to these
	 * routes will not redirected to home page
	 */
	private List<String> additionalRouteExcludes = new ArrayList<>();

	/**
	 * Allows to enable toast notifications in SBA.
	 */
	private Boolean enableToasts = false;

	/**
	 * Show or hide URL of instances.
	 */
	private Boolean hideInstanceUrl = false;

	private UiTheme theme = new UiTheme();

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

		/**
		 * Time in milliseconds to refresh data in logfile view.
		 */
		private int logfile = 1000;

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

	@Data
	public static class UiTheme {

		private Boolean backgroundEnabled = true;

		private Palette palette = new Palette();

		private String color = "#14615A";

	}

	/**
	 * Color shades are based on Tailwind's color palettes:
	 * https://tailwindcss.com/docs/customizing-colors
	 *
	 * name shade number mainColorLighter 50 mainColorLight 300 mainColor 500
	 * mainColorDark 700 mainColorDarker 800
	 */
	@Getter
	public static class Palette {

		private String shade50 = "#EEFCFA";

		private String shade100 = "#D9F7F4";

		private String shade200 = "#B7F0EA";

		private String shade300 = "#91E8E0";

		private String shade400 = "#6BE0D5";

		private String shade500 = "#47D9CB";

		private String shade600 = "#27BEAF";

		private String shade700 = "#1E9084";

		private String shade800 = "#14615A";

		private String shade900 = "#0A2F2B";

		public void set50(String shade50) {
			this.shade50 = shade50;
		}

		public void set100(String shade100) {
			this.shade100 = shade100;
		}

		public void set200(String shade200) {
			this.shade200 = shade200;
		}

		public void set300(String shade300) {
			this.shade300 = shade300;
		}

		public void set400(String shade400) {
			this.shade400 = shade400;
		}

		public void set500(String shade500) {
			this.shade500 = shade500;
		}

		public void set600(String shade600) {
			this.shade600 = shade600;
		}

		public void set700(String shade700) {
			this.shade700 = shade700;
		}

		public void set800(String shade800) {
			this.shade800 = shade800;
		}

		public void set900(String shade900) {
			this.shade900 = shade900;
		}

	}

}
