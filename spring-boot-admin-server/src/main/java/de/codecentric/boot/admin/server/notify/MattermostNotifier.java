/*
 * Copyright 2014-2025 the original author or authors.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.notify.filter.AbstractContentNotifier;

/**
 * Notifier submitting events to Mattermost.
 *
 * @author Emir Boyaci
 */
public class MattermostNotifier extends AbstractContentNotifier {

	private static final String DEFAULT_MESSAGE = "**#{name}** (#{id}) is **#{status}**";

	private RestTemplate restTemplate;

	/**
	 * API url for Mattermost (i.e. https://example.mattermost.com/api/v4/posts)
	 */
	@Nullable private URI apiUrl;

	/**
	 * Bot access token (i.e. dufc8q78hjgeccwsfhe37pcq1w)
	 */
	@Nullable private String botAccessToken;

	/**
	 * Optional channel name without # sign (i.e. h616jh436pysjpopp3259mhwxc)
	 */
	@Nullable private String channelId;

	public MattermostNotifier(InstanceRepository repository, RestTemplate restTemplate) {
		super(repository);
		this.restTemplate = restTemplate;
	}

	@Override
	protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
		if (apiUrl == null) {
			return Mono.error(new IllegalStateException("'url' must not be null."));
		}
		return Mono.fromRunnable(() -> restTemplate.postForEntity(apiUrl, createMessage(event, instance), Void.class));
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	protected Object createMessage(InstanceEvent event, Instance instance) {
		Map<String, Object> messageJson = new HashMap<>();
		if (channelId != null) {
			messageJson.put("channel_id", channelId);
		}

		Map<String, Object> attachments = new HashMap<>();
		attachments.put("text", createContent(event, instance));
		attachments.put("fallback", createContent(event, instance));
		attachments.put("color", getColor(event));

		Map<String, Object> props = new HashMap<>();
		props.put("attachments", Collections.singletonList(attachments));

		messageJson.put("props", props);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(botAccessToken);
		return new HttpEntity<>(messageJson, headers);
	}

	@Override
	protected String getDefaultMessage() {
		return DEFAULT_MESSAGE;
	}

	protected String getColor(InstanceEvent event) {
		if (event instanceof InstanceStatusChangedEvent statusChangedEvent) {
			return StatusInfo.STATUS_UP.equals(statusChangedEvent.getStatusInfo().getStatus()) ? "#2eb885" : "#a30100";
		}
		else {
			return "#439FE0";
		}
	}

	@Nullable public URI getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(@Nullable URI apiUrl) {
		this.apiUrl = apiUrl;
	}

	@Nullable public String getChannelId() {
		return channelId;
	}

	public void setChannelId(@Nullable String channelId) {
		this.channelId = channelId;
	}

	@Nullable public String getBotAccessToken() {
		return botAccessToken;
	}

	public void setBotAccessToken(@Nullable String botAccessToken) {
		this.botAccessToken = botAccessToken;
	}

}
