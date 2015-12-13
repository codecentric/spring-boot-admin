package de.codecentric.boot.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.boot.admin")
public class AdminServerProperties {

	private MonitorProperties monitor = new MonitorProperties();

	public MonitorProperties getMonitor() {
		return monitor;
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
}
