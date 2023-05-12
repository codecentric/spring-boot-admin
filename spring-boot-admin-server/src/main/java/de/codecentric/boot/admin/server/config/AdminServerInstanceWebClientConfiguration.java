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

package de.codecentric.boot.admin.server.config;

import java.net.CookiePolicy;
import java.util.List;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.web.reactive.function.client.WebClient;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.web.client.BasicAuthHttpHeaderProvider;
import de.codecentric.boot.admin.server.web.client.CompositeHttpHeadersProvider;
import de.codecentric.boot.admin.server.web.client.HttpHeadersProvider;
import de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunction;
import de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunctions;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import de.codecentric.boot.admin.server.web.client.InstanceWebClientCustomizer;
import de.codecentric.boot.admin.server.web.client.LegacyEndpointConverter;
import de.codecentric.boot.admin.server.web.client.LegacyEndpointConverters;
import de.codecentric.boot.admin.server.web.client.cookies.CookieStoreCleanupTrigger;
import de.codecentric.boot.admin.server.web.client.cookies.JdkPerInstanceCookieStore;
import de.codecentric.boot.admin.server.web.client.cookies.PerInstanceCookieStore;
import de.codecentric.boot.admin.server.web.client.reactive.CompositeReactiveHttpHeadersProvider;
import de.codecentric.boot.admin.server.web.client.reactive.ReactiveHttpHeadersProvider;

@Configuration(proxyBeanMethods = false)
@Lazy(false)
public class AdminServerInstanceWebClientConfiguration {

	private final InstanceWebClient.Builder instanceWebClientBuilder;

	public AdminServerInstanceWebClientConfiguration(ObjectProvider<InstanceWebClientCustomizer> customizers,
			WebClient.Builder webClient) {
		this.instanceWebClientBuilder = InstanceWebClient.builder(webClient);
		customizers.orderedStream().forEach((customizer) -> customizer.customize(this.instanceWebClientBuilder));
	}

	@Bean
	@ConditionalOnMissingBean
	@Scope("prototype")
	public InstanceWebClient.Builder instanceWebClientBuilder() {
		return this.instanceWebClientBuilder.clone();
	}

	@Configuration(proxyBeanMethods = false)
	protected static class InstanceExchangeFiltersConfiguration {

		@Bean
		@ConditionalOnBean(InstanceExchangeFilterFunction.class)
		@ConditionalOnMissingBean(name = "filterInstanceWebClientCustomizer")
		public InstanceWebClientCustomizer filterInstanceWebClientCustomizer(
				List<InstanceExchangeFilterFunction> filters) {
			return (builder) -> builder.filters((f) -> f.addAll(filters));
		}

		@Configuration(proxyBeanMethods = false)
		protected static class DefaultInstanceExchangeFiltersConfiguration {

			@Bean
			@Order(0)
			@ConditionalOnBean(HttpHeadersProvider.class)
			@ConditionalOnMissingBean(name = "addHeadersInstanceExchangeFilter")
			public InstanceExchangeFilterFunction addHeadersInstanceExchangeFilter(
					List<HttpHeadersProvider> headersProviders) {
				return InstanceExchangeFilterFunctions.addHeaders(new CompositeHttpHeadersProvider(headersProviders));
			}

			@Bean
			@Order(0)
			@ConditionalOnBean(ReactiveHttpHeadersProvider.class)
			@ConditionalOnMissingBean(name = "addReactiveHeadersInstanceExchangeFilter")
			public InstanceExchangeFilterFunction addReactiveHeadersInstanceExchangeFilter(
					List<ReactiveHttpHeadersProvider> reactiveHeadersProviders) {
				return InstanceExchangeFilterFunctions
					.addHeadersReactive(new CompositeReactiveHttpHeadersProvider(reactiveHeadersProviders));
			}

			@Bean
			@Order(10)
			@ConditionalOnMissingBean(name = "rewriteEndpointUrlInstanceExchangeFilter")
			public InstanceExchangeFilterFunction rewriteEndpointUrlInstanceExchangeFilter() {
				return InstanceExchangeFilterFunctions.rewriteEndpointUrl();
			}

			@Bean
			@Order(20)
			@ConditionalOnMissingBean(name = "setDefaultAcceptHeaderInstanceExchangeFilter")
			public InstanceExchangeFilterFunction setDefaultAcceptHeaderInstanceExchangeFilter() {
				return InstanceExchangeFilterFunctions.setDefaultAcceptHeader();
			}

			@Bean
			@Order(30)
			@ConditionalOnBean(LegacyEndpointConverter.class)
			@ConditionalOnMissingBean(name = "legacyEndpointConverterInstanceExchangeFilter")
			public InstanceExchangeFilterFunction legacyEndpointConverterInstanceExchangeFilter(
					List<LegacyEndpointConverter> converters) {
				return InstanceExchangeFilterFunctions.convertLegacyEndpoints(converters);
			}

			@Bean
			@Order(40)
			@ConditionalOnMissingBean(name = "logfileAcceptWorkaround")
			public InstanceExchangeFilterFunction logfileAcceptWorkaround() {
				return InstanceExchangeFilterFunctions.logfileAcceptWorkaround();
			}

			@Bean
			@Order(50)
			@ConditionalOnMissingBean(name = "cookieHandlingInstanceExchangeFilter")
			public InstanceExchangeFilterFunction cookieHandlingInstanceExchangeFilter(
					final PerInstanceCookieStore store) {
				return InstanceExchangeFilterFunctions.handleCookies(store);
			}

			@Bean
			@Order(100)
			@ConditionalOnMissingBean(name = "retryInstanceExchangeFilter")
			public InstanceExchangeFilterFunction retryInstanceExchangeFilter(
					AdminServerProperties adminServerProperties) {
				AdminServerProperties.MonitorProperties monitor = adminServerProperties.getMonitor();
				return InstanceExchangeFilterFunctions.retry(monitor.getDefaultRetries(), monitor.getRetries());
			}

			@Bean
			@Order(200)
			@ConditionalOnMissingBean(name = "timeoutInstanceExchangeFilter")
			public InstanceExchangeFilterFunction timeoutInstanceExchangeFilter(
					AdminServerProperties adminServerProperties) {
				AdminServerProperties.MonitorProperties monitor = adminServerProperties.getMonitor();
				return InstanceExchangeFilterFunctions.timeout(monitor.getDefaultTimeout(), monitor.getTimeout());
			}

		}

	}

	@Configuration(proxyBeanMethods = false)
	protected static class HttpHeadersProviderConfiguration {

		@Bean
		@ConditionalOnMissingBean
		public BasicAuthHttpHeaderProvider basicAuthHttpHeadersProvider(AdminServerProperties adminServerProperties) {
			AdminServerProperties.InstanceAuthProperties instanceAuth = adminServerProperties.getInstanceAuth();

			if (instanceAuth.isEnabled()) {
				return new BasicAuthHttpHeaderProvider(instanceAuth.getDefaultUserName(),
						instanceAuth.getDefaultPassword(), instanceAuth.getServiceMap());
			}
			else {
				return new BasicAuthHttpHeaderProvider();
			}
		}

	}

	@Configuration(proxyBeanMethods = false)
	protected static class LegaycEndpointConvertersConfiguration {

		@Bean
		@ConditionalOnMissingBean(name = "healthLegacyEndpointConverter")
		public LegacyEndpointConverter healthLegacyEndpointConverter() {
			return LegacyEndpointConverters.health();
		}

		@Bean
		@ConditionalOnMissingBean(name = "infoLegacyEndpointConverter")
		public LegacyEndpointConverter infoLegacyEndpointConverter() {
			return LegacyEndpointConverters.info();
		}

		@Bean
		@ConditionalOnMissingBean(name = "envLegacyEndpointConverter")
		public LegacyEndpointConverter envLegacyEndpointConverter() {
			return LegacyEndpointConverters.env();
		}

		@Bean
		@ConditionalOnMissingBean(name = "httptraceLegacyEndpointConverter")
		public LegacyEndpointConverter httptraceLegacyEndpointConverter() {
			return LegacyEndpointConverters.httptrace();
		}

		@Bean
		@ConditionalOnMissingBean(name = "threaddumpLegacyEndpointConverter")
		public LegacyEndpointConverter threaddumpLegacyEndpointConverter() {
			return LegacyEndpointConverters.threaddump();
		}

		@Bean
		@ConditionalOnMissingBean(name = "liquibaseLegacyEndpointConverter")
		public LegacyEndpointConverter liquibaseLegacyEndpointConverter() {
			return LegacyEndpointConverters.liquibase();
		}

		@Bean
		@ConditionalOnMissingBean(name = "flywayLegacyEndpointConverter")
		public LegacyEndpointConverter flywayLegacyEndpointConverter() {
			return LegacyEndpointConverters.flyway();
		}

		@Bean
		@ConditionalOnMissingBean(name = "beansLegacyEndpointConverter")
		public LegacyEndpointConverter beansLegacyEndpointConverter() {
			return LegacyEndpointConverters.beans();
		}

		@Bean
		@ConditionalOnMissingBean(name = "configpropsLegacyEndpointConverter")
		public LegacyEndpointConverter configpropsLegacyEndpointConverter() {
			return LegacyEndpointConverters.configprops();
		}

		@Bean
		@ConditionalOnMissingBean(name = "mappingsLegacyEndpointConverter")
		public LegacyEndpointConverter mappingsLegacyEndpointConverter() {
			return LegacyEndpointConverters.mappings();
		}

		@Bean
		@ConditionalOnMissingBean(name = "startupLegacyEndpointConverter")
		public LegacyEndpointConverter startupLegacyEndpointConverter() {
			return LegacyEndpointConverters.startup();
		}

	}

	@Configuration(proxyBeanMethods = false)
	protected static class CookieStoreConfiguration {

		/**
		 * Creates a default {@link PerInstanceCookieStore} that should be used.
		 * @return the cookie store
		 */
		@Bean
		@ConditionalOnMissingBean
		public PerInstanceCookieStore cookieStore() {
			return new JdkPerInstanceCookieStore(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
		}

		/**
		 * Creates a default trigger to cleanup the cookie store on deregistering of an
		 * {@link de.codecentric.boot.admin.server.domain.entities.Instance}.
		 * @param publisher publisher of {@link InstanceEvent}s events
		 * @param cookieStore the store to inform about deregistration of an
		 * {@link de.codecentric.boot.admin.server.domain.entities.Instance}
		 * @return a new trigger
		 */
		@Bean(initMethod = "start", destroyMethod = "stop")
		@ConditionalOnMissingBean
		public CookieStoreCleanupTrigger cookieStoreCleanupTrigger(final Publisher<InstanceEvent> publisher,
				final PerInstanceCookieStore cookieStore) {
			return new CookieStoreCleanupTrigger(publisher, cookieStore);
		}

	}

}
