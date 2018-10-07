/*
 * Copyright 2014-2018 the original author or authors.
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

import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import junit.framework.AssertionFailedError;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractInstanceRepositoryTest<T extends InstanceRepository> {
    protected final T repository;

    protected AbstractInstanceRepositoryTest(T repository) {
        this.repository = repository;
    }

    @Test
    public void save() {
        //given
        Instance instance = Instance.create(InstanceId.of("foo"))
                                    .register(Registration.create("name", "http://health").build());

        StepVerifier.create(repository.save(instance)).expectNext(instance).verifyComplete();

        //when/then
        StepVerifier.create(repository.find(instance.getId())).assertNext(loaded -> {
            assertThat(loaded.getId()).isEqualTo(instance.getId());
            assertThat(loaded.getVersion()).isEqualTo(instance.getVersion());
            assertThat(loaded.getRegistration()).isEqualTo(instance.getRegistration());
            assertThat(loaded.getInfo()).isEqualTo(instance.getInfo());
            assertThat(loaded.getStatusInfo()).isEqualTo(instance.getStatusInfo());
        }).verifyComplete();
    }

    @Test
    public void find_methods() {
        //given
        Instance instance1 = Instance.create(InstanceId.of("foo.1"))
                                     .register(Registration.create("foo", "http://health").build());
        Instance instance2 = Instance.create(InstanceId.of("foo.2"))
                                     .register(Registration.create("foo", "http://health").build());
        Instance instance3 = Instance.create(InstanceId.of("bar"))
                                     .register(Registration.create("bar", "http://health").build());

        StepVerifier.create(repository.save(instance1)).expectNextCount(1).verifyComplete();
        StepVerifier.create(repository.save(instance2)).expectNextCount(1).verifyComplete();
        StepVerifier.create(repository.save(instance3)).expectNextCount(1).verifyComplete();

        //when/then
        StepVerifier.create(repository.find(instance2.getId()))
                    .assertNext(loaded2 -> assertThat(loaded2.getId()).isEqualTo(instance2.getId()))
                    .verifyComplete();

        StepVerifier.create(repository.findByName("foo"))
                    .recordWith(ArrayList::new)
                    .thenConsumeWhile(a -> true)
                    .consumeRecordedWith(appsByName -> {
                        assertThat(appsByName.stream()
                                             .map(Instance::getId)
                                             .collect(Collectors.toList())).containsExactlyInAnyOrder(instance1.getId(),
                            instance2.getId()
                        );
                    })
                    .verifyComplete();

        StepVerifier.create(repository.findAll())
                    .recordWith(ArrayList::new)
                    .thenConsumeWhile(a -> true)
                    .consumeRecordedWith(allApps -> {
                        assertThat(allApps.stream()
                                          .map(Instance::getId)
                                          .collect(Collectors.toList())).containsExactlyInAnyOrder(instance1.getId(),
                            instance2.getId(),
                            instance3.getId()
                        );
                    })
                    .verifyComplete();
    }

    @Test
    public void should_retry_computeIfPresent() {
        AtomicLong counter = new AtomicLong(3L);
        //given
        Instance instance1 = Instance.create(InstanceId.of("foo.1"))
                                     .register(Registration.create("foo", "http://health").build());
        StepVerifier.create(repository.save(instance1)).expectNextCount(1).verifyComplete();

        //when
        StepVerifier.create(repository.computeIfPresent(instance1.getId(),
            (key, application) -> counter.getAndDecrement() >
                                  0L ? Mono.just(instance1) : Mono.just(application.withEndpoints(Endpoints.single(
                "info",
                "info"
            )))
        )).expectNext(instance1.withEndpoints(Endpoints.single("info", "info"))).verifyComplete();

        //then
        StepVerifier.create(repository.find(instance1.getId()))
                    .assertNext(loaded -> assertThat(loaded.getEndpoints()).isEqualTo(Endpoints.single("info", "info")
                                                                                               .withEndpoint(
                                                                                                   "health",
                                                                                                   "http://health"
                                                                                               )))
                    .verifyComplete();
    }

    @Test
    public void computeIfPresent_should_not_save_if_not_present() {
        //given
        InstanceId instanceId = InstanceId.of("foo");

        //when
        StepVerifier.create(repository.computeIfPresent(instanceId,
            (key, application) -> Mono.error(new AssertionFailedError("Should not call any computation"))
        ))
                    .verifyComplete();

        //then
        StepVerifier.create(repository.find(instanceId)).verifyComplete();
    }

    @Test
    public void should_run_compute_with_null() {
        //when
        InstanceId instanceId = InstanceId.of("foo");
        StepVerifier.create(repository.compute(instanceId, (key, application) -> {
            assertThat(application).isNull();
            return Mono.just(Instance.create(key).register(Registration.create("foo", "http://health").build()));
        }))
                    .expectNext(Instance.create(instanceId)
                                        .register(Registration.create("foo", "http://health").build()))
                    .verifyComplete();

        //then
        StepVerifier.create(repository.find(instanceId))
                    .assertNext(loaded -> assertThat(loaded.getId()).isEqualTo(instanceId))
                    .verifyComplete();
    }

    @Test
    public void should_retry_compute() {
        AtomicLong counter = new AtomicLong(3L);
        //given
        Instance instance = Instance.create(InstanceId.of("foo.1"))
                                    .register(Registration.create("foo", "http://health").build());
        StepVerifier.create(repository.save(instance)).expectNextCount(1).verifyComplete();

        //when
        StepVerifier.create(repository.compute(
            instance.getId(),
            (key, application) -> counter.getAndDecrement() >
                                  0L ? Mono.just(instance) : Mono.just(application.withEndpoints(Endpoints.single(
                "info",
                "info"
            )))
        )).expectNext(instance.withEndpoints(Endpoints.single("info", "info"))).verifyComplete();

        //then
        StepVerifier.create(repository.find(instance.getId()))
                    .assertNext(loaded -> assertThat(loaded.getEndpoints()).isEqualTo(Endpoints.single("info", "info")
                                                                                               .withEndpoint(
                                                                                                   "health",
                                                                                                   "http://health"
                                                                                               )))
                    .verifyComplete();
    }
}
