/*
 * Copyright 2014-2020 the original author or authors.
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

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.support.collections.DefaultRedisMap;
import org.springframework.data.redis.support.collections.RedisMap;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
import de.codecentric.boot.admin.server.eventstore.RedisEventStore;
import de.codecentric.boot.admin.server.notify.NotificationTrigger;
import de.codecentric.boot.admin.server.notify.Notifier;
import de.codecentric.boot.admin.server.notify.RedisNotificationTrigger;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(AdminServerMarkerConfiguration.Marker.class)
@ConditionalOnProperty(prefix = "spring.boot.admin.redis", name = "enabled", matchIfMissing = false)
@AutoConfigureBefore({ AdminServerAutoConfiguration.class, AdminServerNotifierAutoConfiguration.class })
@EnableConfigurationProperties(RedisProperties.class)
// The AutoConfiguration for Redis is filtered out, so it has to be imported as normal
// configuration (see RedisAutoConfigurationImportFilter)
@Import(RedisAutoConfiguration.class)
public class AdminServerRedisAutoConfiguration {

	public static final String DEFAULT_NAME_EVENT_STORE_MAP = "spring-boot-admin-event-store";

	public static final String DEFAULT_NAME_SENT_NOTIFICATIONS_MAP = "spring-boot-admin-sent-notifications";

	@Value("${spring.boot.admin.redis.event-store:" + DEFAULT_NAME_EVENT_STORE_MAP + "}")
	private String nameEventStoreMap = DEFAULT_NAME_EVENT_STORE_MAP;

	@Bean
	@ConditionalOnMissingBean(InstanceEventStore.class)
	public RedisEventStore eventStore(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
		RedisTemplate<String, List<InstanceEvent>> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer(StandardCharsets.UTF_8));

		Jackson2JsonRedisSerializer hashKeySerializer = new Jackson2JsonRedisSerializer(InstanceId.class);
		hashKeySerializer.setObjectMapper(objectMapper);
		redisTemplate.setHashKeySerializer(hashKeySerializer);

		CollectionType type = TypeFactory.defaultInstance().constructCollectionType(List.class, InstanceEvent.class);
		Jackson2JsonRedisSerializer hashValueSerializer = new Jackson2JsonRedisSerializer(type);
		hashValueSerializer.setObjectMapper(objectMapper);
		redisTemplate.setHashValueSerializer(hashValueSerializer);

		redisTemplate.afterPropertiesSet();

		RedisMap<InstanceId, List<InstanceEvent>> map = new DefaultRedisMap<InstanceId, List<InstanceEvent>>(
				nameEventStoreMap, redisTemplate);
		return new RedisEventStore(map);
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnBean(Notifier.class)
	public static class NotifierTriggerConfiguration {

		@Value("${spring.boot.admin.redis.sent-notifications:" + DEFAULT_NAME_SENT_NOTIFICATIONS_MAP + "}")
		private String nameSentNotificationsMap = DEFAULT_NAME_SENT_NOTIFICATIONS_MAP;

		@Bean(initMethod = "start", destroyMethod = "stop")
		@ConditionalOnMissingBean(NotificationTrigger.class)
		public NotificationTrigger notificationTrigger(RedisConnectionFactory connectionFactory, Notifier notifier,
				Publisher<InstanceEvent> events, ObjectMapper objectMapper) {

			RedisTemplate<String, Long> redisTemplate = new RedisTemplate<>();
			redisTemplate.setConnectionFactory(connectionFactory);
			redisTemplate.setKeySerializer(new StringRedisSerializer(StandardCharsets.UTF_8));

			Jackson2JsonRedisSerializer hashKeySerializer = new Jackson2JsonRedisSerializer(InstanceId.class);
			hashKeySerializer.setObjectMapper(objectMapper);
			redisTemplate.setHashKeySerializer(hashKeySerializer);

			Jackson2JsonRedisSerializer<Long> hashValueSerializer = new Jackson2JsonRedisSerializer(Long.class);
			hashValueSerializer.setObjectMapper(objectMapper);
			redisTemplate.setHashValueSerializer(hashValueSerializer);

			redisTemplate.afterPropertiesSet();

			RedisMap<InstanceId, Long> map = new DefaultRedisMap<InstanceId, Long>(nameSentNotificationsMap,
					redisTemplate);
			return new RedisNotificationTrigger(notifier, events, map);
		}

	}

	/**
	 * Filter out the RedisAutoConfiguration because otherwise even if
	 * <code>spring.boot.admin.redis.enabled</code> is set to <code>false</code> a Redis
	 * connection will be created.
	 */
	public static class RedisAutoConfigurationImportFilter implements AutoConfigurationImportFilter {

		private static final Set<String> SHOULD_SKIP = Collections
				.singleton("org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration");

		@Override
		public boolean[] match(String[] classNames, AutoConfigurationMetadata metadata) {
			boolean[] matches = new boolean[classNames.length];

			for (int i = 0; i < classNames.length; i++) {
				matches[i] = !SHOULD_SKIP.contains(classNames[i]);
			}
			return matches;
		}

	}

}
