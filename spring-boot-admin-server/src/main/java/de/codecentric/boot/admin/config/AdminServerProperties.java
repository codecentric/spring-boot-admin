/*
 * Copyright 2014-2017 the original author or authors.
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

    public void setContextPath(String pathPrefix) {
        if (!pathPrefix.startsWith("/") || pathPrefix.endsWith("/")) {
            throw new IllegalArgumentException("ContextPath must start with '/' and not end with '/'");
        }
        this.contextPath = pathPrefix;
    }

    public String getContextPath() {
        return contextPath;
    }

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

        /**
         * Connect timeout when querying the applications' status and info.
         */
        private int connectTimeout = 2_000;

        /**
         * read timeout when querying the applications' status and info.
         */
        private int readTimeout = 5_000;

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

        public int getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public int getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
        }
    }

}
