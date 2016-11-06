package de.codecentric.boot.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.boot.admin")
public class AdminServerProperties {

	/**
	 * The context-path prefixes the path where the Admin Servers statics assets and api should be
	 * served. Relative to the Dispatcher-Servlet.
	 */
	private String contextPath = "";

	private MonitorProperties monitor = new MonitorProperties();

	private RoutesProperties routes = new RoutesProperties();

	public void setContextPath(String pathPrefix) {
		if (!pathPrefix.startsWith("/") || pathPrefix.endsWith("/")) {
			throw new IllegalArgumentException(
					"ContextPath must start with '/' and not end with '/'");
		}
		this.contextPath = pathPrefix;
	}

	public String getContextPath() {
		return contextPath;
	}

	public MonitorProperties getMonitor() {
		return monitor;
	}

	public RoutesProperties getRoutes() {
		return routes;
	}

	public static class MonitorProperties {
		/**
		 * Time interval in ms to update the status of applications with expired statusInfo
		 */
		private long period = 10_000L;

		/**
		 * Lifetime of status in ms. The status won't be updated as long the last status isn't
		 * expired.
		 */
		private long statusLifetime = 10_000L;

		public void setPeriod(long period) {
			this.period = period;
		}

		public long getPeriod() {
			return period;
		}

		public void setStatusLifetime(long statusLifetime) {
			this.statusLifetime = statusLifetime;
		}

		public long getStatusLifetime() {
			return statusLifetime;
		}
	}

	public static class RoutesProperties {
		/**
		 * Endpoints to be proxified by spring boot admin.
		 */
		private String[] endpoints = { "env", "metrics", "trace", "dump", "jolokia", "info",
				"trace", "logfile", "refresh", "flyway", "liquibase", "heapdump" };

		public String[] getEndpoints() {
			return endpoints;
		}

		public void setEndpoints(String[] endpoints) {
			this.endpoints = endpoints;
		}
	}
}
