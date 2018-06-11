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

package de.codecentric.boot.admin.server.ui.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.http.CacheControl;

@lombok.Data
@ConfigurationProperties("spring.boot.admin.ui")
public class AdminServerUiProperties {
    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {"classpath:/META-INF/spring-boot-admin-server-ui/"};

    /**
     * Locations of SBA ui resources.
     */
    private String[] resourceLocations = CLASSPATH_RESOURCE_LOCATIONS;

    /**
     * Locations of SBA ui template.
     */
    private String templateLocation = CLASSPATH_RESOURCE_LOCATIONS[0];

    private String customLabel;

    private String customImageSrc;

    private boolean cacheTemplates = true;

    private final Cache cache = new Cache();

    @lombok.Data
    public static class Cache {

        /**
         * include "max-age" directive in Cache-Control http header.
         */
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
