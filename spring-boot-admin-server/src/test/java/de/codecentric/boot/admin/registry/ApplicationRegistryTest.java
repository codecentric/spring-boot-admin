/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin.registry;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.Info;
import de.codecentric.boot.admin.model.StatusInfo;
import de.codecentric.boot.admin.registry.store.ApplicationStore;
import de.codecentric.boot.admin.registry.store.SimpleApplicationStore;

public class ApplicationRegistryTest {
	private ApplicationStore store = new SimpleApplicationStore();
	private ApplicationIdGenerator idGenerator = new HashingApplicationUrlIdGenerator();
	private ApplicationRegistry registry = new ApplicationRegistry(store, idGenerator);

	public ApplicationRegistryTest() {
		registry.setApplicationEventPublisher(Mockito.mock(ApplicationEventPublisher.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void registerFailed_null() throws Exception {
		registry.register(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void registerFailed_no_name() throws Exception {
		registry.register(Application.create("").withHealthUrl("http://localhost/health").build());
	}

	@Test(expected = IllegalArgumentException.class)
	public void registerFailed_no_healthUrl() throws Exception {
		registry.register(Application.create("name").build());
	}

	@Test(expected = IllegalArgumentException.class)
	public void registerFailed_invalid_healthUrl() throws Exception {
		registry.register(Application.create("name").withHealthUrl("not-a-url").build());
	}

	@Test(expected = IllegalArgumentException.class)
	public void registerFailed_invalid_mgmtUrl() throws Exception {
		registry.register(Application.create("").withHealthUrl("http://localhost/health")
				.withManagementUrl("not-a-url").build());
	}

	@Test(expected = IllegalArgumentException.class)
	public void registerFailed_invalid_svcUrl() throws Exception {
		registry.register(Application.create("").withHealthUrl("http://localhost/health")
				.withServiceUrl("not-a-url").build());
	}

	@Test
	public void register() throws Exception {
		Application app = registry.register(
				Application.create("abc").withHealthUrl("http://localhost:8080/health").build());

		assertThat(app.getHealthUrl()).isEqualTo("http://localhost:8080/health");
		assertThat(app.getName()).isEqualTo("abc");
		assertThat(app.getId()).isNotNull();
	}

	@Test
	public void refresh() throws Exception {
		// Given application is already reegistered and has status and info.
		StatusInfo status = StatusInfo.ofUp();
		Info info = Info.from(singletonMap("foo", "bar"));
		Application app = Application.create("abc").withHealthUrl("http://localhost:8080/health")
				.withStatusInfo(status).withInfo(info).build();
		String id = idGenerator.generateId(app);
		store.save(Application.copyOf(app).withId(id).build());

		// When application registers second time
		Application registered = registry.register(
				Application.create("abc").withHealthUrl("http://localhost:8080/health").build());

		// Then info and status are retained
		assertThat(registered.getInfo()).isSameAs(info);
		assertThat(registered.getStatusInfo()).isSameAs(status);
	}

	@Test
	public void getApplication() throws Exception {
		Application app = registry
				.register(Application.create("abc").withHealthUrl("http://localhost/health")
						.withManagementUrl("http://localhost:8080/").build());
		assertThat(registry.getApplication(app.getId())).isEqualTo(app);
		assertThat(app.getManagementUrl()).isEqualTo("http://localhost:8080/");
	}

	@Test
	public void getApplications() throws Exception {
		Application app = registry.register(
				Application.create("abc").withHealthUrl("http://localhost/health").build());

		Collection<Application> applications = registry.getApplications();
		assertThat(applications).containsOnly(app);
	}

	@Test
	public void getApplicationsByName() throws Exception {
		Application app = registry.register(
				Application.create("abc").withHealthUrl("http://localhost/health").build());
		Application app2 = registry.register(
				Application.create("abc").withHealthUrl("http://localhost:8081/health").build());
		registry.register(
				Application.create("zzz").withHealthUrl("http://localhost:8082/health").build());

		Collection<Application> applications = registry.getApplicationsByName("abc");
		assertThat(applications).containsOnly(app, app2);
	}
}
