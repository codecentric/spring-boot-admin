/*
 * Copyright 2014-2020 the original author or authors.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.audit.InMemoryAuditEventRepository;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
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

@Configuration(proxyBeanMethods = false)
@EnableAutoConfiguration
@EnableAdminServer
@Import({ SecurityPermitAllConfig.class, SecuritySecureConfig.class, NotifierConfig.class })
@Lazy(false)
public class SpringBootAdminServletApplication {

	private static final Logger log = LoggerFactory.getLogger(SpringBootAdminServletApplication.class);

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SpringBootAdminServletApplication.class);
		app.setApplicationStartup(new BufferingApplicationStartup(1500));
		app.run(args);
	}

	private final java.util.concurrent.atomic.AtomicInteger a = new java.util.concurrent.atomic.AtomicInteger();

	@Bean
	@org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
	public de.codecentric.boot.admin.server.web.client.InstanceWebClient webClient(
			de.codecentric.boot.admin.server.web.client.InstanceWebClient.Builder builder) {
		return builder.build();
	}

	@Bean
	@org.springframework.core.annotation.Order(300)
	public InstanceExchangeFilterFunction loggingInstanceExchangeFilter() {
		return (instance, request, next) -> {
			return next.exchange(request).doOnSuccess((resp) -> {
				a.set(0);
				log.info("exchange filter function: onsuccess [instance={}]", instance.getId());
			});
		};
	}

	// tag::customization-instance-exchange-filter-function[]
	@Bean
	public InstanceExchangeFilterFunction auditLog() {
		return (instance, request, next) -> {
			log.info("auditLog: call [instance={}]", instance.getId());

			return next.exchange(request).doOnSubscribe((s) -> {
				if (org.springframework.http.HttpMethod.DELETE.equals(request.method())
						|| org.springframework.http.HttpMethod.POST.equals(request.method())) {
					log.info("{} for {} on {}", request.method(), instance.getId(), request.url());
				}
			});
		};
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
	public HttpTraceRepository httpTraceRepository() {
		return new InMemoryHttpTraceRepository();
	}

	@Bean
	public AuditEventRepository auditEventRepository() {
		return new InMemoryAuditEventRepository();
	}

	@Bean
	public EmbeddedDatabase dataSource() {
		return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.HSQL)
				.addScript("org/springframework/session/jdbc/schema-hsqldb.sql").build();
	}

}
