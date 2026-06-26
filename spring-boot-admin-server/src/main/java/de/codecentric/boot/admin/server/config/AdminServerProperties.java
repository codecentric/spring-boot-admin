/*
 * Copyright 2014-2026 the original author or authors.
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

package de.codecentric.boot.admin.server.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.web.PathUtils;
import de.codecentric.boot.admin.server.web.client.BasicAuthHttpHeaderProvider.InstanceCredentials;

import static java.util.Arrays.asList;

@lombok.Data
@ConfigurationProperties("spring.boot.admin")
public class AdminServerProperties {

	/**
	 * The context-path prefixes the path where the Admin Servers static assets and api
	 * should be served, relative to the Dispatcher-Servlet.
	 */
	private String contextPath = "";

	private ServerProperties server = new ServerProperties();

	private MonitorProperties monitor = new MonitorProperties();

	private InstanceAuthProperties instanceAuth = new InstanceAuthProperties();

	private InstanceProxyProperties instanceProxy = new InstanceProxyProperties();

	private SsrfProtectionProperties ssrfProtection = new SsrfProtectionProperties();

	/**
	 * The metadata keys which should be sanitized when serializing to JSON
	 */
	private String[] metadataKeysToSanitize = new String[] { ".*password$", ".*secret$", ".*key$", ".*token$",
			".*credentials.*", ".*vcap_services$" };

	/**
	 * For Spring Boot 2.x applications the endpoints should be discovered automatically
	 * using the actuator links. For Spring Boot 1.x applications SBA probes for the
	 * specified endpoints using an OPTIONS request. If the path differs from the id you
	 * can specify this as id:path (e.g. health:ping).
	 * <p>
	 * All the available default endpoints are listed in the <a href=
	 * "https://docs.spring.io/spring-boot/reference/actuator/endpoints.html">documentation</a>.
	 */
	private String[] probedEndpoints = { "health", "env", "metrics", "httptrace:trace", "httptrace", "threaddump:dump",
			"threaddump", "jolokia", "info", "logfile", "refresh", "flyway", "liquibase", "heapdump", "loggers",
			"auditevents", "mappings", "scheduledtasks", "configprops", "caches", "beans", "conditions",
			"httpexchanges", "integrationgraph", "quartz", "sessions", "shutdown", "startup", "prometheus", "sbom" };

	public void setContextPath(String contextPath) {
		this.contextPath = PathUtils.normalizePath(contextPath);
	}

	/**
	 * @param path the partial path within the admin context-path
	 * @return the full path within the admin context-path
	 */
	public String path(String path) {
		return this.contextPath + path;
	}

	@lombok.Data
	public static class ServerProperties {

		/**
		 * Enable Spring Boot Admin Server Default: true
		 */
		private boolean enabled = true;

	}

	@lombok.Data
	public static class MonitorProperties {

		/**
		 * Default {@link StatusChangeDetectionStrategy} applied if nothing different is
		 * configured for {@link MonitorProperties#statusChangeDetectionStrategy}.
		 */
		public static final StatusChangeDetectionStrategy DEFAULT_STATUS_CHANGE_DETECTION_STRATEGY = StatusChangeDetectionStrategy.STATUS_ONLY;

		/**
		 * Time interval to check the status of instances, must be greater than 1 second.
		 */
		@DurationUnit(ChronoUnit.MILLIS)
		private Duration statusInterval = Duration.ofMillis(10_000L);

		/**
		 * Lifetime of status. The status won't be updated as long the last status isn't
		 * expired.
		 */
		@DurationUnit(ChronoUnit.MILLIS)
		private Duration statusLifetime = Duration.ofMillis(10_000L);

		/**
		 * Strategy to use to determine if, given two {@link StatusInfo} instances,
		 * they're different or not in order to decide if a {@code STATUS_UPDATED} event
		 * should be published or not.
		 * <p>
		 * Defaults to
		 * {@link AdminServerProperties.MonitorProperties#DEFAULT_STATUS_CHANGE_DETECTION_STRATEGY}
		 * unless a different value is specified.
		 */
		private StatusChangeDetectionStrategy statusChangeDetectionStrategy = DEFAULT_STATUS_CHANGE_DETECTION_STRATEGY;

		/**
		 * The maximal backoff for status check retries (retry after error has exponential
		 * backoff, minimum backoff is 1 second).
		 */
		@DurationUnit(ChronoUnit.MILLIS)
		private Duration statusMaxBackoff = Duration.ofMillis(60_000L);

		/**
		 * Time interval to check the info of instances,
		 */
		@DurationUnit(ChronoUnit.MILLIS)
		private Duration infoInterval = Duration.ofMinutes(1L);

		/**
		 * The maximal backoff for info check retries (retry after error has exponential
		 * backoff, minimum backoff is 1 second).
		 */
		@DurationUnit(ChronoUnit.MILLIS)
		private Duration infoMaxBackoff = Duration.ofMinutes(10);

		/**
		 * Lifetime of info. The info won't be updated as long the last info isn't
		 * expired.
		 */
		@DurationUnit(ChronoUnit.MILLIS)
		private Duration infoLifetime = Duration.ofMinutes(1L);

		/**
		 * Default number of retries for failed requests. Individual values for specific
		 * endpoints can be overridden using `spring.boot.admin.monitor.retries.*`.
		 */
		private int defaultRetries = 0;

		/**
		 * Number of retries per endpointId. Defaults to default-retry.
		 */
		private Map<String, Integer> retries = new HashMap<>();

		/**
		 * Default timeout when making requests. Individual values for specific endpoints
		 * can be overridden using `spring.boot.admin.monitor.timeout.*`.
		 */
		@DurationUnit(ChronoUnit.MILLIS)
		private Duration defaultTimeout = Duration.ofMillis(10_000L);

		/**
		 * timeout per endpointId. Defaults to default-timeout.
		 */
		@DurationUnit(ChronoUnit.MILLIS)
		private Map<String, Duration> timeout = new HashMap<>();

		/**
		 * Strategy to determine if two {@link StatusInfo} instances are different or not.
		 */
		public enum StatusChangeDetectionStrategy {

			/**
			 * It considers two {@link StatusInfo} instances different if they're not
			 * equal.
			 */
			FULL {
				@Override
				public boolean mismatch(StatusInfo statusInfo, StatusInfo newStatusInfo) {
					return !Objects.equals(statusInfo, newStatusInfo);
				}
			},
			/**
			 * It considers two {@link StatusInfo} instances different if only their
			 * {@code status} is not equal.
			 */
			STATUS_ONLY {
				@Override
				public boolean mismatch(StatusInfo statusInfo, StatusInfo newStatusInfo) {
					return !Objects.equals(statusInfo.getStatus(), newStatusInfo.getStatus());
				}
			};

			public abstract boolean mismatch(StatusInfo statusInfo, StatusInfo newStatusInfo);

			public BiPredicate<StatusInfo, StatusInfo> asPredicate() {
				return this::mismatch;
			}

		}

	}

	@lombok.Data
	public static class InstanceAuthProperties {

		/**
		 * Whether or not to use configuration properties as a source for instance
		 * credentials <br/>
		 * Default: true
		 */
		private boolean enabled = true;

		/**
		 * Default username used for authentication to each instance. Individual values
		 * for specific instances can be overriden using
		 * `spring.boot.admin.instance-auth.service-map.*.user-name`. <br/>
		 * Default: null
		 */
		private String defaultUserName = null;

		/**
		 * Default userpassword used for authentication to each instance. Individual
		 * values for specific instances can be overriden using
		 * `spring.boot.admin.instance-auth.service-map.*.user-password`. <br/>
		 * Default: null
		 */
		private String defaultPassword = null;

		/**
		 * Map of instance credentials per registered service name
		 */
		private Map<String, InstanceCredentials> serviceMap = new HashMap<>();

	}

	@lombok.Data
	public static class InstanceProxyProperties {

		/**
		 * Headers not to be forwarded when making requests to clients.
		 */
		private Set<String> ignoredHeaders = new HashSet<>(asList("Cookie", "Set-Cookie", "Authorization"));

	}

	@lombok.Data
	public static class SsrfProtectionProperties {

		/**
		 * Whether SSRF protection is enabled. When enabled, registration URLs are
		 * validated against blocked schemes and non-external IP addresses. Default: false
		 * (opt-in).
		 */
		private boolean enabled = false;

		/**
		 * URL schemes that are permitted. Any scheme not in this list is blocked.
		 * Default: http, https.
		 */
		private Set<String> allowedSchemes = new HashSet<>(asList("http", "https"));

		/**
		 * Additional IP addresses or CIDR ranges (IPv4 and IPv6) that are permitted even
		 * if they would otherwise be blocked by the default
		 * {@code InetAddressFilter.externalAddresses()} filter. Useful for intranet
		 * deployments where the Admin Server must reach services on private IP ranges.
		 * <p>
		 * Examples: {@code 192.168.1.100}, {@code 192.168.1.0/24}, {@code 10.0.0.0/8},
		 * {@code fd00::/8}
		 */
		private List<String> allowedCidrs = new ArrayList<>();

	}

}
