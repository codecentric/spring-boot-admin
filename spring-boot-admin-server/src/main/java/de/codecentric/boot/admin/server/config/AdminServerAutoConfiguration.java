/*
 * Copyright 2014-2026 the original author or authors.
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

import java.time.Duration;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.client.InetAddressFilter;
import org.springframework.boot.webclient.autoconfigure.WebClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.SnapshottingInstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.eventstore.InstanceEventPublisher;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
import de.codecentric.boot.admin.server.services.ApiMediaTypeHandler;
import de.codecentric.boot.admin.server.services.ApplicationRegistry;
import de.codecentric.boot.admin.server.services.EndpointDetectionTrigger;
import de.codecentric.boot.admin.server.services.EndpointDetector;
import de.codecentric.boot.admin.server.services.HashingInstanceUrlIdGenerator;
import de.codecentric.boot.admin.server.services.HealthGroupsCache;
import de.codecentric.boot.admin.server.services.HealthGroupsCacheCleanupListener;
import de.codecentric.boot.admin.server.services.InMemoryHealthGroupsCache;
import de.codecentric.boot.admin.server.services.InfoUpdateTrigger;
import de.codecentric.boot.admin.server.services.InfoUpdater;
import de.codecentric.boot.admin.server.services.InstanceFilter;
import de.codecentric.boot.admin.server.services.InstanceIdGenerator;
import de.codecentric.boot.admin.server.services.InstanceRegistry;
import de.codecentric.boot.admin.server.services.StatusUpdateTrigger;
import de.codecentric.boot.admin.server.services.StatusUpdater;
import de.codecentric.boot.admin.server.services.endpoints.ChainingStrategy;
import de.codecentric.boot.admin.server.services.endpoints.ProbeEndpointsStrategy;
import de.codecentric.boot.admin.server.services.endpoints.QueryIndexEndpointStrategy;
import de.codecentric.boot.admin.server.utils.SsrfUrlValidator;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

@Configuration(proxyBeanMethods = false)
@Conditional(SpringBootAdminServerEnabledCondition.class)
@ConditionalOnBean(AdminServerMarkerConfiguration.Marker.class)
@EnableConfigurationProperties(AdminServerProperties.class)
@ImportAutoConfiguration({ AdminServerInstanceWebClientConfiguration.class, AdminServerWebConfiguration.class })
@AutoConfigureAfter({ WebClientAutoConfiguration.class })
@Slf4j
@Lazy(false)
public class AdminServerAutoConfiguration {

	public static final String SSRF_INET_ADDRESS_FILTER_BEAN_NAME = "ssrfInetAddressFilter";

	private final AdminServerProperties adminServerProperties;

	public AdminServerAutoConfiguration(AdminServerProperties adminServerProperties) {
		this.adminServerProperties = adminServerProperties;
	}

	@Bean
	@ConditionalOnMissingBean
	public InstanceFilter instanceFilter() {
		return (instance) -> true;
	}

	@Bean(name = SSRF_INET_ADDRESS_FILTER_BEAN_NAME)
	@ConditionalOnMissingBean(name = SSRF_INET_ADDRESS_FILTER_BEAN_NAME)
	@ConditionalOnProperty(prefix = "spring.boot.admin.ssrf-protection", name = "enabled", havingValue = "true")
	public InetAddressFilter ssrfInetAddressFilter() {
		InetAddressFilter filter = InetAddressFilter.externalAddresses();
		List<String> allowedCidrs = this.adminServerProperties.getSsrfProtection().getAllowedCidrs();
		if (!allowedCidrs.isEmpty()) {
			filter = filter.or(allowedCidrs.toArray(String[]::new));
		}
		return filter;
	}

	@Bean
	@ConditionalOnMissingBean
	public SsrfUrlValidator ssrfUrlValidator(
			@Qualifier(SSRF_INET_ADDRESS_FILTER_BEAN_NAME) ObjectProvider<InetAddressFilter> ssrfInetAddressFilter) {
		// The InetAddressFilter bean is only registered when SSRF protection is enabled
		// (see ssrfInetAddressFilter()). When absent, fall back to the default external
		// filter; SsrfUrlValidator only consults it while protection is enabled, so this
		// fallback is never exercised in the default (disabled) configuration.
		InetAddressFilter filter = ssrfInetAddressFilter.getIfAvailable(InetAddressFilter::externalAddresses);
		return new SsrfUrlValidator(this.adminServerProperties.getSsrfProtection(), filter);
	}

	@Bean
	@ConditionalOnMissingBean
	public InstanceRegistry instanceRegistry(InstanceRepository instanceRepository,
			InstanceIdGenerator instanceIdGenerator, InstanceFilter instanceFilter, SsrfUrlValidator ssrfUrlValidator) {
		return new InstanceRegistry(instanceRepository, instanceIdGenerator, instanceFilter, ssrfUrlValidator);
	}

	@Bean
	@ConditionalOnMissingBean
	public ApplicationRegistry applicationRegistry(InstanceRegistry instanceRegistry,
			InstanceEventPublisher instanceEventPublisher) {
		return new ApplicationRegistry(instanceRegistry, instanceEventPublisher);
	}

	@Bean
	@ConditionalOnMissingBean
	public InstanceIdGenerator instanceIdGenerator() {
		return new HashingInstanceUrlIdGenerator();
	}

	@Bean
	@ConditionalOnMissingBean
	public HealthGroupsCache healthGroupsCache() {
		return new InMemoryHealthGroupsCache();
	}

	@Bean
	@ConditionalOnMissingBean
	public StatusUpdater statusUpdater(InstanceRepository instanceRepository,
			InstanceWebClient.Builder instanceWebClientBuilder, HealthGroupsCache healthGroupsCache) {

		AdminServerProperties.MonitorProperties monitorProperties = this.adminServerProperties.getMonitor();

		StatusUpdater updater = new StatusUpdater(instanceRepository, instanceWebClientBuilder.build(),
				new ApiMediaTypeHandler(), monitorProperties.getStatusChangeDetectionStrategy().asPredicate(),
				healthGroupsCache);

		Duration timeout = monitorProperties.getDefaultTimeout();
		Duration interval = monitorProperties.getStatusInterval();

		if (timeout.compareTo(interval) > 0) {
			timeout = interval;
		}

		updater.timeout(timeout);

		return updater;
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	@ConditionalOnMissingBean
	public StatusUpdateTrigger statusUpdateTrigger(StatusUpdater statusUpdater, Publisher<InstanceEvent> events) {
		AdminServerProperties.MonitorProperties monitorProperties = this.adminServerProperties.getMonitor();

		Duration defaultTimeout = monitorProperties.getDefaultTimeout();
		Duration statusInterval = monitorProperties.getStatusInterval();

		if (defaultTimeout.compareTo(statusInterval) > 0) {
			log.warn(
					"Default timeout ({}) is larger than status interval ({}), hence status interval will be used as timeout.",
					defaultTimeout, statusInterval);
		}

		return new StatusUpdateTrigger(statusUpdater, events, statusInterval, monitorProperties.getStatusLifetime(),
				monitorProperties.getStatusMaxBackoff());
	}

	@Bean
	@ConditionalOnMissingBean
	public HealthGroupsCacheCleanupListener healthGroupsCacheCleanupListener(Publisher<InstanceEvent> events,
			HealthGroupsCache healthGroupsCache) {
		return new HealthGroupsCacheCleanupListener(events, healthGroupsCache);
	}

	@Bean
	@ConditionalOnMissingBean
	public EndpointDetector endpointDetector(InstanceRepository instanceRepository,
			InstanceWebClient.Builder instanceWebClientBuilder) {
		InstanceWebClient instanceWebClient = instanceWebClientBuilder.build();
		ChainingStrategy strategy = new ChainingStrategy(
				new QueryIndexEndpointStrategy(instanceWebClient, new ApiMediaTypeHandler()),
				new ProbeEndpointsStrategy(instanceWebClient, this.adminServerProperties.getProbedEndpoints()));
		return new EndpointDetector(instanceRepository, strategy);
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	@ConditionalOnMissingBean
	public EndpointDetectionTrigger endpointDetectionTrigger(EndpointDetector endpointDetector,
			Publisher<InstanceEvent> events) {
		return new EndpointDetectionTrigger(endpointDetector, events);
	}

	@Bean
	@ConditionalOnMissingBean
	public InfoUpdater infoUpdater(InstanceRepository instanceRepository,
			InstanceWebClient.Builder instanceWebClientBuilder) {
		return new InfoUpdater(instanceRepository, instanceWebClientBuilder.build(), new ApiMediaTypeHandler());
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	@ConditionalOnMissingBean
	public InfoUpdateTrigger infoUpdateTrigger(InfoUpdater infoUpdater, Publisher<InstanceEvent> events) {
		return new InfoUpdateTrigger(infoUpdater, events, this.adminServerProperties.getMonitor().getInfoInterval(),
				this.adminServerProperties.getMonitor().getInfoLifetime(),
				this.adminServerProperties.getMonitor().getInfoMaxBackoff());
	}

	@Bean
	@ConditionalOnMissingBean(InstanceEventStore.class)
	public InMemoryEventStore eventStore() {
		return new InMemoryEventStore();
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	@ConditionalOnMissingBean(InstanceRepository.class)
	public SnapshottingInstanceRepository instanceRepository(InstanceEventStore eventStore) {
		return new SnapshottingInstanceRepository(eventStore);
	}

}
