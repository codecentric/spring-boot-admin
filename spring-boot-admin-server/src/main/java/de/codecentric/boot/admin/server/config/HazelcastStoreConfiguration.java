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
package de.codecentric.boot.admin.server.config;

import de.codecentric.boot.admin.server.domain.events.ClientApplicationEvent;
import de.codecentric.boot.admin.server.domain.values.ApplicationId;
import de.codecentric.boot.admin.server.eventstore.ClientApplicationEventStore;
import de.codecentric.boot.admin.server.eventstore.HazelcastEventStore;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

@Configuration
@ConditionalOnSingleCandidate(HazelcastInstance.class)
@ConditionalOnProperty(prefix = "spring.boot.admin.hazelcast", name = "enabled", matchIfMissing = true)
@AutoConfigureBefore(AdminServerWebConfiguration.class)
@AutoConfigureAfter(HazelcastAutoConfiguration.class)
public class HazelcastStoreConfiguration {

    @Value("${spring.boot.admin.hazelcast.event-store:spring-boot-admin-event-store}")
    private String mapName;

    @Bean
    @ConditionalOnMissingBean
    public ClientApplicationEventStore eventStore(HazelcastInstance hazelcastInstance) {
        IMap<ApplicationId, List<ClientApplicationEvent>> map = hazelcastInstance.getMap(mapName);

        return new HazelcastEventStore(map);
    }
}
