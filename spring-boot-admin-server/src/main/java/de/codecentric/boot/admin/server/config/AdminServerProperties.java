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

package de.codecentric.boot.admin.server.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@lombok.Data
@ConfigurationProperties("spring.boot.admin")
public class AdminServerProperties {
    /**
     * The context-path prefixes the path where the Admin Servers statics assets and api should be
     * served. Relative to the Dispatcher-Servlet.
     */
    private String contextPath = "";

    private MonitorProperties monitor = new MonitorProperties();

    /**
     * For Spring Boot 2.x applications the endpoints should be discovered automatically using the actuator links.
     * For Spring Boot 1.x applications SBA probes for the specified endpoints using an OPTIONS request.
     * If the path differs from the id you can specify this as id:path (e.g. health:ping).
     */
    private String[] probedEndpoints = {"health", "env", "metrics", "trace", "threaddump:dump", "jolokia", "info",
            "logfile", "refresh", "flyway", "liquibase", "heapdump", "loggers", "auditevents"};

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

    public String[] getProbedEndpoints() {
        return probedEndpoints;
    }

    public void setProbedEndpoints(String[] probedEndpoints) {
        this.probedEndpoints = probedEndpoints;
    }

    @lombok.Data
    public static class MonitorProperties {
        /**
         * Time interval to update the status of instances with expired statusInfo
         */
        private Duration period = Duration.ofSeconds(10);

        /**
         * Lifetime of status. The status won't be updated as long the last status isn't
         * expired.
         */
        private Duration statusLifetime = Duration.ofSeconds(10);

        /**
         * Connect timeout when querying the instances' status and info.
         */
        private Duration connectTimeout = Duration.ofSeconds(2);

        /**
         * read timeout when querying the instances' status and info.
         */
        private Duration readTimeout = Duration.ofSeconds(5);

    }
}
