/*
 * Copyright 2014-2023 the original author or authors.
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

package de.codecentric.boot.admin.server.notify;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.notify.filter.AbstractContentNotifier;

/**
 * Notifier submitting events to RocketChat.
 *
 * @author Nicolas Badenne
 */
public class RocketChatNotifier extends AbstractContentNotifier {

	private static final String DEFAULT_MESSAGE = "*#{name}* (#{id}) is *#{status}*";

	private RestTemplate restTemplate;

	/**
	 * Host URL for RocketChat server
	 */
	@Nullable
	private String url;

	/**
	 * Room Id to send message
	 */
	@Nullable
	private String roomId;

	/**
	 * Token for RocketChat API
	 */
	@Nullable
	private String token;

	/**
	 * User Id for RocketChat API
	 */
	private String userId;

	public RocketChatNotifier(InstanceRepository repository, RestTemplate restTemplate) {
		super(repository);
		this.restTemplate = restTemplate;
	}

	@Override
	protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("X-Auth-Token", token);
		headers.add("X-User-Id", userId);
		return Mono.fromRunnable(() -> restTemplate.exchange(getUri(), HttpMethod.POST,
				new HttpEntity<>(createMessage(event, instance), headers), Void.class));
	}

	private URI getUri() {
		if (url == null) {
			throw new IllegalStateException("'url' must not be null.");
		}
		return URI.create(String.format("%s/api/v1/chat.sendMessage", url));
	}

	protected Object createMessage(InstanceEvent event, Instance instance) {
		Map<String, String> messageJsonData = new HashMap<>();
		messageJsonData.put("rid", roomId);
		messageJsonData.put("msg", createContent(event, instance));
		Map<String, Object> messageJson = new HashMap<>();
		messageJson.put("message", messageJsonData);
		return messageJson;
	}

	@Override
	protected Map<String, Object> buildContentModel(InstanceEvent event, Instance instance) {
		var content = super.buildContentModel(event, instance);
		content.put("roomId", roomId);
		return content;
	}

	@Override
	protected String getDefaultMessage() {
		return DEFAULT_MESSAGE;
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Nullable
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Nullable
	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	@Nullable
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Nullable
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
