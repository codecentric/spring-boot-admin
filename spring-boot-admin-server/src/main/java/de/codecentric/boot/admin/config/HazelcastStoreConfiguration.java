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
package de.codecentric.boot.admin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.Config;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEvent;

import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationUnregisteredEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.registry.store.ApplicationStore;
import de.codecentric.boot.admin.registry.store.HazelcastApplicationStore;

@Configuration
@ConditionalOnClass({ Hazelcast.class })
@ConditionalOnProperty(prefix = "spring.boot.admin.hazelcast", name = "enabled", matchIfMissing = true)
public  class HazelcastStoreConfiguration {

	@Value("${spring.boot.admin.hazelcast.map:spring-boot-admin-application-store}")
	private String hazelcastMapName;

	@Bean
	@ConditionalOnMissingBean
	public Config hazelcastConfig() {
		return new Config();
	}

	@Bean(destroyMethod = "shutdown")
	@ConditionalOnMissingBean
	public HazelcastInstance hazelcastInstance(Config hazelcastConfig) {
		return Hazelcast.newHazelcastInstance(hazelcastConfig);
	}

	@Bean
	@ConditionalOnMissingBean
	public ApplicationStore applicationStore(HazelcastInstance hazelcast) {
		IMap<String, Application> map = hazelcast.<String, Application> getMap(hazelcastMapName);
		map.addIndex("name", false);
		map.addEntryListener(entryListener(), false);
		return new HazelcastApplicationStore(map);
	}

	@Bean
	public EntryListener<String, Application> entryListener() {
		return new ApplicationEntryListener();
	}

	private static class ApplicationEntryListener implements EntryListener<String, Application> {
		@Autowired
		ApplicationEventPublisher publisher;

		@Override
		public void entryAdded(EntryEvent<String, Application> event) {
			publisher.publishEvent(new ClientApplicationRegisteredEvent(this,event.getValue()));
		}

		@Override
		public void entryRemoved(EntryEvent<String, Application> event) {
			publisher.publishEvent(new ClientApplicationUnregisteredEvent(this,event.getValue()));
		}

		@Override
		public void entryUpdated(EntryEvent<String, Application> event) {
			publisher.publishEvent(new ClientApplicationRegisteredEvent(this,event.getValue()));
		}

		@Override
		public void entryEvicted(EntryEvent<String, Application> event) {
			publisher.publishEvent(new ClientApplicationRegisteredEvent(this,null));
		}

		@Override
		public void mapEvicted(MapEvent event) {
			publisher.publishEvent(new ClientApplicationRegisteredEvent(this,null));
		}

		@Override
		public void mapCleared(MapEvent event) {
			publisher.publishEvent(new ClientApplicationRegisteredEvent(this,null));
		}
	}
}
