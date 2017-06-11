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
package de.codecentric.boot.admin.server.registry;

import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.ApplicationId;
import de.codecentric.boot.admin.server.model.Info;
import de.codecentric.boot.admin.server.model.Registration;
import de.codecentric.boot.admin.server.model.StatusInfo;
import de.codecentric.boot.admin.server.registry.store.ApplicationStore;
import de.codecentric.boot.admin.server.registry.store.SimpleApplicationStore;

import java.util.Collection;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    public void register() throws Exception {
        Application app = registry.register(Registration.create("abc", "http://localhost:8080/health").build());

        assertThat(app.getRegistration().getHealthUrl()).isEqualTo("http://localhost:8080/health");
        assertThat(app.getRegistration().getName()).isEqualTo("abc");
        assertThat(app.getId()).isNotNull();
    }

    @Test
    public void refresh() throws Exception {
        // Given application is already reegistered and has status and info.
        StatusInfo status = StatusInfo.ofUp();
        Info info = Info.from(singletonMap("foo", "bar"));
        Registration registration = Registration.create("abc", "http://localhost:8080/health").build();
        ApplicationId id = idGenerator.generateId(registration);
        Application app = Application.create(id, registration).statusInfo(status).info(info).build();
        store.save(app);

        // When application registers second time
        Application registered = registry.register(Registration.create("abc", "http://localhost:8080/health").build());

        // Then info and status are retained
        assertThat(registered.getInfo()).isSameAs(info);
        assertThat(registered.getStatusInfo()).isSameAs(status);
    }

    @Test
    public void getApplication() throws Exception {
        Application app = registry.register(
                Registration.create("abc", "http://localhost/health").managementUrl("http://localhost:8080/").build());
        assertThat(registry.getApplication(app.getId())).isEqualTo(app);
        assertThat(app.getRegistration().getManagementUrl()).isEqualTo("http://localhost:8080/");
    }

    @Test
    public void getApplications() throws Exception {
        Application app = registry.register(Registration.create("abc", "http://localhost/health").build());

        Collection<Application> applications = registry.getApplications();
        assertThat(applications).containsOnly(app);
    }

    @Test
    public void getApplicationsByName() throws Exception {
        Application app = registry.register(Registration.create("abc", "http://localhost/health").build());
        Application app2 = registry.register(Registration.create("abc", "http://localhost:8081/health").build());
        registry.register(Registration.create("zzz", "http://localhost:8082/health").build());

        Collection<Application> applications = registry.getApplicationsByName("abc");
        assertThat(applications).containsOnly(app, app2);
    }
}
