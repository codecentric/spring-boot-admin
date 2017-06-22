/*
 * Copyright 2014-2017 the original author or authors.
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

package de.codecentric.boot.admin.server.domain.entities;

import de.codecentric.boot.admin.server.domain.values.ApplicationId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EventSourcingApplicationRepositoryTest {
    private EventSourcingApplicationRepository repository = new EventSourcingApplicationRepository(
            new InMemoryEventStore());

    @Before
    public void setUp() throws Exception {
        repository.start();
    }

    @After
    public void tearDown() throws Exception {
        repository.stop();
    }

    @Test
    public void save() throws Exception {
        Application application = Application.create(ApplicationId.of("foo"))
                                             .register(Registration.create("name", "http://health").build());

        StepVerifier.create(repository.save(application)).verifyComplete();

        StepVerifier.create(repository.find(application.getId())).assertNext(loaded -> {
            assertThat(loaded.getId()).isEqualTo(application.getId());
            assertThat(loaded.getVersion()).isEqualTo(application.getVersion());
            assertThat(loaded.getRegistration()).isEqualTo(application.getRegistration());
            assertThat(loaded.getInfo()).isEqualTo(application.getInfo());
            assertThat(loaded.getStatusInfo()).isEqualTo(application.getStatusInfo());
        }).verifyComplete();
    }

    @Test
    public void find_methods() throws Exception {
        Application application1 = Application.create(ApplicationId.of("foo.1"))
                                              .register(Registration.create("foo", "http://health").build());
        Application application2 = Application.create(ApplicationId.of("foo.2"))
                                              .register(Registration.create("foo", "http://health").build());
        Application application3 = Application.create(ApplicationId.of("bar"))
                                              .register(Registration.create("bar", "http://health").build());

        StepVerifier.create(repository.save(application1)).verifyComplete();
        StepVerifier.create(repository.save(application2)).verifyComplete();
        StepVerifier.create(repository.save(application3)).verifyComplete();

        StepVerifier.create(repository.find(application2.getId()))
                    .assertNext(loaded2 -> assertThat(loaded2.getId()).isEqualTo(loaded2.getId()))
                    .verifyComplete();

        StepVerifier.create(repository.findByName("foo"))
                    .recordWith(ArrayList::new)
                    .thenConsumeWhile(a -> true)
                    .consumeRecordedWith(appsByName -> {
                        assertThat(appsByName.stream()
                                             .map(Application::getId)
                                             .collect(Collectors.toList())).containsExactlyInAnyOrder(
                                application1.getId(), application2.getId());
                    })
                    .verifyComplete();

        StepVerifier.create(repository.findAll())
                    .recordWith(ArrayList::new)
                    .thenConsumeWhile(a -> true)
                    .consumeRecordedWith(allApps -> {
                        assertThat(allApps.stream()
                                          .map(Application::getId)
                                          .collect(Collectors.toList())).containsExactlyInAnyOrder(application1.getId(),
                                application2.getId(), application3.getId());
                    })
                    .verifyComplete();
    }
}