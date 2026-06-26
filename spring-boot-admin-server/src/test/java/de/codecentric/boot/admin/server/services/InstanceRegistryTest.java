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

package de.codecentric.boot.admin.server.services;

import java.util.ArrayList;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.http.client.InetAddressFilter;
import reactor.test.StepVerifier;

import de.codecentric.boot.admin.server.config.AdminServerProperties.SsrfProtectionProperties;
import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.utils.SsrfUrlValidator;
import de.codecentric.boot.admin.server.web.client.exception.SsrfProtectionException;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InstanceRegistryTest {

	private InstanceRepository repository;

	private InstanceIdGenerator idGenerator;

	private InstanceRegistry registry;

	@BeforeEach
	void setUp() {
		repository = new EventsourcingInstanceRepository(new InMemoryEventStore());
		idGenerator = new HashingInstanceUrlIdGenerator();
		registry = new InstanceRegistry(repository, idGenerator, (instance) -> {
			Map<String, String> metadata = instance.getRegistration().getMetadata();
			return !metadata.containsKey("displayed") || !metadata.get("displayed").equals("false");
		});
	}

	@Test
	void registerFailed_null() {
		assertThatThrownBy(() -> registry.register(null)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void register() {
		Registration registration = Registration.create("abc", "http://localhost:8080/health").build();
		InstanceId id = registry.register(registration).block();

		StepVerifier.create(registry.getInstance(id)).assertNext((app) -> {
			assertThat(app.getRegistration()).isEqualTo(registration);
			assertThat(app.getId()).isNotNull();
		}).verifyComplete();

		StepVerifier.create(registry.getInstances()).assertNext((app) -> {
			assertThat(app.getRegistration()).isEqualTo(registration);
			assertThat(app.getId()).isNotNull();
		}).verifyComplete();
	}

	@Test
	void deregister() {
		InstanceId id = registry.register(Registration.create("abc", "http://localhost:8080/health").build()).block();
		registry.deregister(id).block();

		StepVerifier.create(registry.getInstance(id))
			.assertNext((app) -> assertThat(app.isRegistered()).isFalse())
			.verifyComplete();
	}

	@Test
	void refresh() {
		// Given instance is already registered and has status and info.
		StatusInfo status = StatusInfo.ofUp();
		Info info = Info.from(singletonMap("foo", "bar"));
		Registration registration = Registration.create("abc", "http://localhost:8080/health").build();
		InstanceId id = idGenerator.generateId(registration);
		Instance app = Instance.create(id).register(registration).withStatusInfo(status).withInfo(info);
		StepVerifier.create(repository.save(app)).expectNextCount(1).verifyComplete();

		// When instance registers second time
		InstanceId refreshId = registry.register(Registration.create("abc", "http://localhost:8080/health").build())
			.block();

		assertThat(refreshId).isEqualTo(id);
		StepVerifier.create(registry.getInstance(id)).assertNext((registered) -> {
			// Then info and status are retained
			assertThat(registered.getInfo()).isEqualTo(info);
			assertThat(registered.getStatusInfo()).isEqualTo(status);
		}).verifyComplete();
	}

	@Test
	void findByName() {
		InstanceId id1 = registry.register(Registration.create("abc", "http://localhost:8080/health").build()).block();
		InstanceId id2 = registry.register(Registration.create("abc", "http://localhost:8081/health").build()).block();
		InstanceId id3 = registry.register(Registration.create("zzz", "http://localhost:9999/health").build()).block();

		StepVerifier.create(registry.getInstances("abc"))
			.recordWith(ArrayList::new)
			.thenConsumeWhile((a) -> true)
			.consumeRecordedWith(
					(applications) -> assertThat(applications.stream().map(Instance::getId)).doesNotContain(id3)
						.containsExactlyInAnyOrder(id1, id2))
			.verifyComplete();
	}

	@Test
	void findByNameAndFilter() {
		InstanceId id1 = registry.register(Registration.create("abc", "http://localhost:8080/health").build()).block();
		registry
			.register(Registration.create("abc", "http://localhost:8081/health").metadata("displayed", "false").build())
			.block();

		StepVerifier.create(registry.getInstances("abc"))
			.recordWith(ArrayList::new)
			.thenConsumeWhile((a) -> true)
			.consumeRecordedWith(
					(applications) -> assertThat(applications.stream().map(Instance::getId)).containsExactly(id1))
			.verifyComplete();
	}

	@Nested
	class SsrfProtection {

		private InstanceRegistry ssrfRegistry;

		@BeforeEach
		void setUp() {
			SsrfProtectionProperties ssrfProps = new SsrfProtectionProperties();
			ssrfProps.setEnabled(true);
			SsrfUrlValidator ssrfValidator = new SsrfUrlValidator(ssrfProps, InetAddressFilter.externalAddresses());
			ssrfRegistry = new InstanceRegistry(repository, idGenerator, (instance) -> true, ssrfValidator);
		}

		@Test
		void register_rejects_awsMetadataHealthUrl() {
			Registration reg = Registration.create("evil", "http://169.254.169.254/latest/meta-data/").build();
			assertThatThrownBy(() -> ssrfRegistry.register(reg).block()).isInstanceOf(SsrfProtectionException.class)
				.hasMessageContaining("not permitted by the configured InetAddressFilter");
		}

		@Test
		void register_rejects_loopbackManagementUrl() {
			Registration reg = Registration.create("evil", "http://example.com/health")
				.managementUrl("http://127.0.0.1:8080/actuator")
				.build();
			assertThatThrownBy(() -> ssrfRegistry.register(reg).block()).isInstanceOf(SsrfProtectionException.class)
				.hasMessageContaining("not permitted by the configured InetAddressFilter");
		}

		@Test
		void register_rejects_privateRangeServiceUrl() {
			Registration reg = Registration.create("evil", "http://example.com/health")
				.serviceUrl("http://192.168.1.1/")
				.build();
			assertThatThrownBy(() -> ssrfRegistry.register(reg).block()).isInstanceOf(SsrfProtectionException.class)
				.hasMessageContaining("not permitted by the configured InetAddressFilter");
		}

		@Test
		void register_allows_externalUrl_whenSsrfEnabled() {
			Registration reg = Registration.create("legit", "http://example.com/actuator/health").build();
			InstanceId id = ssrfRegistry.register(reg).block();
			assertThat(id).isNotNull();
		}

		@Test
		void register_allows_privateHost_whenCustomFilterPermitsIt() {
			SsrfProtectionProperties ssrfProps = new SsrfProtectionProperties();
			ssrfProps.setEnabled(true);
			// Extend the default external filter to also allow 192.168.1.0/24
			InetAddressFilter customFilter = InetAddressFilter.externalAddresses().or("192.168.1.0/24");
			ssrfRegistry = new InstanceRegistry(repository, idGenerator, (instance) -> true,
					new SsrfUrlValidator(ssrfProps, customFilter));

			Registration reg = Registration.create("intranet-svc", "http://192.168.1.100/actuator/health").build();
			InstanceId id = ssrfRegistry.register(reg).block();
			assertThat(id).isNotNull();
		}

		@Test
		void register_allows_cidrRange_whenConfiguredViaProperty() {
			SsrfProtectionProperties ssrfProps = new SsrfProtectionProperties();
			ssrfProps.setEnabled(true);
			ssrfProps.setAllowedCidrs(java.util.List.of("10.0.0.0/8"));
			InetAddressFilter filter = InetAddressFilter.externalAddresses()
				.or(ssrfProps.getAllowedCidrs().toArray(String[]::new));
			ssrfRegistry = new InstanceRegistry(repository, idGenerator, (instance) -> true,
					new SsrfUrlValidator(ssrfProps, filter));

			Registration reg = Registration.create("pod", "http://10.42.0.5/actuator/health").build();
			InstanceId id = ssrfRegistry.register(reg).block();
			assertThat(id).isNotNull();
		}

		@Test
		void register_rejects_addressOutsideAllowedCidr() {
			SsrfProtectionProperties ssrfProps = new SsrfProtectionProperties();
			ssrfProps.setEnabled(true);
			ssrfProps.setAllowedCidrs(java.util.List.of("192.168.1.0/24"));
			InetAddressFilter filter = InetAddressFilter.externalAddresses()
				.or(ssrfProps.getAllowedCidrs().toArray(String[]::new));
			ssrfRegistry = new InstanceRegistry(repository, idGenerator, (instance) -> true,
					new SsrfUrlValidator(ssrfProps, filter));

			// 192.168.2.1 is outside the allowed 192.168.1.0/24
			Registration reg = Registration.create("evil", "http://192.168.2.1/actuator/health").build();
			assertThatThrownBy(() -> ssrfRegistry.register(reg).block()).isInstanceOf(SsrfProtectionException.class)
				.hasMessageContaining("not permitted by the configured InetAddressFilter");
		}

	}

}
