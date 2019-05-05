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

package de.codecentric.boot.admin;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.web.client.HttpHeadersProvider;
import de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

@Configuration
@EnableAutoConfiguration
@EnableAdminServer
@Import({SecurityPermitAllConfig.class, SecuritySecureConfig.class, NotifierConfig.class})
public class SpringBootAdminServletApplication {
    private static final Logger log = LoggerFactory.getLogger(SpringBootAdminServletApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBootAdminServletApplication.class, args);
    }

    // tag::customization-instance-exchange-filter-function[]
    @Bean
    public InstanceExchangeFilterFunction auditLog() {
        return (instance, request, next) -> next.exchange(request).doOnSubscribe(s -> {
            if (HttpMethod.DELETE.equals(request.method()) || HttpMethod.POST.equals(request.method())) {
                log.info("{} for {} on {}", request.method(), instance.getId(), request.url());
            }
        });
    }
    // end::customization-instance-exchange-filter-function[]

    @Bean
    public CustomNotifier customNotifier(InstanceRepository repository) {
        return new CustomNotifier(repository);
    }

    @Bean
    public CustomEndpoint customEndpoint() {
        return new CustomEndpoint();
    }

    // tag::customization-http-headers-providers[]
    @Bean
    public HttpHeadersProvider customHttpHeadersProvider() {
        return instance -> {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-CUSTOM", "My Custom Value");
            return httpHeaders;
        };
    }
    // end::customization-http-headers-providers[]

}
