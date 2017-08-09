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
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import junit.framework.AssertionFailedError;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
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
        //given
        Application application = Application.create(ApplicationId.of("foo"))
                                             .register(Registration.create("name", "http://health").build());

        StepVerifier.create(repository.save(application)).verifyComplete();

        //when/then
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
        //given
        Application application1 = Application.create(ApplicationId.of("foo.1"))
                                              .register(Registration.create("foo", "http://health").build());
        Application application2 = Application.create(ApplicationId.of("foo.2"))
                                              .register(Registration.create("foo", "http://health").build());
        Application application3 = Application.create(ApplicationId.of("bar"))
                                              .register(Registration.create("bar", "http://health").build());

        StepVerifier.create(repository.save(application1)).verifyComplete();
        StepVerifier.create(repository.save(application2)).verifyComplete();
        StepVerifier.create(repository.save(application3)).verifyComplete();

        //when/then
        StepVerifier.create(repository.find(application2.getId()))
                    .assertNext(loaded2 -> assertThat(loaded2.getId()).isEqualTo(application2.getId()))
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

    @Test
    public void should_retry_computeIfPresent() {
        AtomicLong counter = new AtomicLong(3L);
        //given
        Application application1 = Application.create(ApplicationId.of("foo.1"))
                                              .register(Registration.create("foo", "http://health").build());
        StepVerifier.create(repository.save(application1)).verifyComplete();

        //when
        StepVerifier.create(repository.computeIfPresent(application1.getId(),
                (key, application) -> counter.getAndDecrement() > 0L ?
                        Mono.just(application1) :
                        Mono.just(application.withEndpoints(Endpoints.single("info", "info"))))).verifyComplete();

        //then
        StepVerifier.create(repository.find(application1.getId()))
                    .assertNext(loaded -> assertThat(loaded.getEndpoints()).isEqualTo(Endpoints.single("info", "info")))
                    .verifyComplete();
    }

    @Test
    public void computeIfPresent_should_not_save_if_not_present() {
        //given
        ApplicationId applicationId = ApplicationId.of("foo");

        //when
        StepVerifier.create(repository.computeIfPresent(applicationId,
                (key, application) -> Mono.error(new AssertionFailedError("Should not call any computation"))))
                    .verifyComplete();

        //then
        StepVerifier.create(repository.find(applicationId)).verifyComplete();
    }

    @Test
    public void should_run_compute_with_null() throws InterruptedException {
        //when
        ApplicationId applicationId = ApplicationId.of("foo");
        StepVerifier.create(repository.compute(applicationId, (key, application) -> {
            assertThat(application).isNull();
            return Mono.just(Application.create(key).register(Registration.create("foo", "http://health").build()));
        })).verifyComplete();

        //then
        StepVerifier.create(repository.find(applicationId))
                    .assertNext(loaded -> assertThat(loaded.getId()).isEqualTo(applicationId))
                    .verifyComplete();
    }

    @Test
    public void should_retry_compute() {
        AtomicLong counter = new AtomicLong(3L);
        //given
        Application application1 = Application.create(ApplicationId.of("foo.1"))
                                              .register(Registration.create("foo", "http://health").build());
        StepVerifier.create(repository.save(application1)).verifyComplete();

        //when
        StepVerifier.create(repository.compute(application1.getId(),
                (key, application) -> counter.getAndDecrement() > 0L ?
                        Mono.just(application1) :
                        Mono.just(application.withEndpoints(Endpoints.single("info", "info"))))).verifyComplete();

        //then
        StepVerifier.create(repository.find(application1.getId()))
                    .assertNext(loaded -> assertThat(loaded.getEndpoints()).isEqualTo(Endpoints.single("info", "info")))
                    .verifyComplete();
    }
}