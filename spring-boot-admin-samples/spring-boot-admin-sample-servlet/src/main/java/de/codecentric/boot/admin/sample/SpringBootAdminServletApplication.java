/*
 * Copyright 2014-2023 the original author or authors.
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

package de.codecentric.boot.admin.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.audit.InMemoryAuditEventRepository;
import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.web.client.HttpHeadersProvider;
import de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunction;

@SpringBootApplication
@EnableAdminServer
@Lazy(false)
@EnableCaching
public class SpringBootAdminServletApplication {

	private static final Logger log = LoggerFactory.getLogger(SpringBootAdminServletApplication.class);

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SpringBootAdminServletApplication.class);
		app.setApplicationStartup(new BufferingApplicationStartup(1500));
		app.run(args);
	}

	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager("books");
	}

	// tag::customization-instance-exchange-filter-function[]
	@Bean
	public InstanceExchangeFilterFunction auditLog() {
		return (instance, request, next) -> next.exchange(request).doOnSubscribe((s) -> {
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
		return (instance) -> {
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add("X-CUSTOM", "My Custom Value");
			return httpHeaders;
		};
	}
	// end::customization-http-headers-providers[]

	@Bean
	public HttpExchangeRepository httpTraceRepository() {
		return new InMemoryHttpExchangeRepository();
	}

	@Bean
	public AuditEventRepository auditEventRepository() {
		return new InMemoryAuditEventRepository();
	}

	@Bean
	public EmbeddedDatabase dataSource() {
		return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.HSQL)
			.addScript("org/springframework/session/jdbc/schema-hsqldb.sql")
			.build();
	}

}
