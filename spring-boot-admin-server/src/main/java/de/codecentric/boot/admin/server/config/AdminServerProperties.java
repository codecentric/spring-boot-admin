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

package de.codecentric.boot.admin.server.config;

import de.codecentric.boot.admin.server.web.PathUtils;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.convert.DefaultDurationUnit;

import static java.util.Arrays.asList;

@lombok.Data
@ConfigurationProperties("spring.boot.admin")
public class AdminServerProperties {
    /**
     * The context-path prefixes the path where the Admin Servers statics assets and api should be
     * served. Relative to the Dispatcher-Servlet.
     */
    private String contextPath = "";

    private MonitorProperties monitor = new MonitorProperties();

    private InstanceProxyProperties instanceProxy = new InstanceProxyProperties();

    /**
     * The metadata keys which should be sanitized when serializing to json
     */
    private String[] metadataKeysToSanitize = new String[]{".*password$", ".*secret$", ".*key$", ".*$token$",
            ".*credentials.*", ".*vcap_services$"};

    /**
     * For Spring Boot 2.x applications the endpoints should be discovered automatically using the actuator links.
     * For Spring Boot 1.x applications SBA probes for the specified endpoints using an OPTIONS request.
     * If the path differs from the id you can specify this as id:path (e.g. health:ping).
     */
    private String[] probedEndpoints = {"health", "env", "metrics", "httptrace:trace", "threaddump:dump", "jolokia",
            "info", "logfile", "refresh", "flyway", "liquibase", "heapdump", "loggers", "auditevents"};

    public void setContextPath(String contextPath) {
        this.contextPath = PathUtils.normalizePath(contextPath);
    }

    public String getContextPath() {
        return contextPath;
    }

    public MonitorProperties getMonitor() {
        return monitor;
    }

    public String[] getMetadataKeysToSanitize() {
        return metadataKeysToSanitize;
    }

    public void setMetadataKeysToSanitize(String[] metadataKeysToSanitize) {
        this.metadataKeysToSanitize = metadataKeysToSanitize;
    }

    public String[] getProbedEndpoints() {
        return probedEndpoints;
    }

    public void setProbedEndpoints(String[] probedEndpoints) {
        this.probedEndpoints = probedEndpoints;
    }

    public InstanceProxyProperties getInstanceProxy() {
        return instanceProxy;
    }

    @lombok.Data
    public static class MonitorProperties {
        /**
         * Time interval to update the status of instances with expired statusInfo
         */
        @DefaultDurationUnit(ChronoUnit.MILLIS)
        private Duration period = Duration.ofMillis(10_000L);

        /**
         * Lifetime of status. The status won't be updated as long the last status isn't
         * expired.
         */
        @DefaultDurationUnit(ChronoUnit.MILLIS)
        private Duration statusLifetime = Duration.ofMillis(10_000L);

        /**
         * Connect timeout when querying the instances' status and info.
         */
        @DefaultDurationUnit(ChronoUnit.MILLIS)
        private Duration connectTimeout = Duration.ofMillis(2_000);

        /**
         * read timeout when querying the instances' status and info.
         */
        @DefaultDurationUnit(ChronoUnit.MILLIS)
        private Duration readTimeout = Duration.ofMillis(5_000);

    }

    @lombok.Data
    public static class InstanceProxyProperties {
        /**
         * Headers not to be forwarded when making requests to clients.
         */
        private Set<String> ignoredHeaders = new HashSet<>(asList("Cookie", "Set-Cookie", "Authorization"));

        public Set<String> getIgnoredHeaders() {
            return ignoredHeaders;
        }

        public void setIgnoredHeaders(Set<String> ignoredHeaders) {
            this.ignoredHeaders = ignoredHeaders;
        }
    }
}
