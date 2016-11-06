/*
 * Copyright 2013-2014 the original author or authors.
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
package de.codecentric.boot.admin.notify;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.event.ClientApplicationEvent;
import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;

/**
 * Notifier submitting events to Pagerduty.
 *
 * @author Johannes Edmeier
 */
public class PagerdutyNotifier extends AbstractStatusChangeNotifier {
	public static final URI DEFAULT_URI = URI
			.create("https://events.pagerduty.com/generic/2010-04-15/create_event.json");

	private static final String DEFAULT_DESCRIPTION = "#{application.name}/#{application.id} is #{to.status}";

	private final SpelExpressionParser parser = new SpelExpressionParser();
	private RestTemplate restTemplate = new RestTemplate();

	/**
	 * URI for pagerduty-REST-API
	 */
	private URI url = DEFAULT_URI;

	/**
	 * Service-Key for pagerduty-REST-API
	 */
	private String serviceKey;

	/**
	 * Client for pagerduty-REST-API
	 */
	private String client;

	/**
	 * Client-url for pagerduty-REST-API
	 */
	private URI clientUrl;

	/**
	 * Trigger description. SpEL template using event as root;
	 */
	private Expression description;

	public PagerdutyNotifier() {
		this.description = parser.parseExpression(DEFAULT_DESCRIPTION,
				ParserContext.TEMPLATE_EXPRESSION);
	}

	@Override
	protected void doNotify(ClientApplicationEvent event) throws Exception {
		restTemplate.postForEntity(url, createPagerdutyEvent(event), Void.class);
	}

	protected Map<String, Object> createPagerdutyEvent(ClientApplicationEvent event) {
		Map<String, Object> result = new HashMap<>();
		result.put("service_key", serviceKey);
		result.put("incident_key",
				event.getApplication().getName() + "/" + event.getApplication().getId());
		result.put("description", getDescirption(event));

		Map<String, Object> details = getDetails(event);
		result.put("details", details);

		if (event instanceof ClientApplicationStatusChangedEvent) {
			if ("UP".equals(((ClientApplicationStatusChangedEvent) event).getTo().getStatus())) {
				result.put("event_type", "resolve");
			} else {
				result.put("event_type", "trigger");
				if (client != null) {
					result.put("client", client);
				}
				if (clientUrl != null) {
					result.put("client_url", clientUrl);
				}

				Map<String, Object> context = new HashMap<>();
				context.put("type", "link");
				context.put("href", event.getApplication().getHealthUrl());
				context.put("text", "Application health-endpoint");
				result.put("contexts", Arrays.asList(context));
			}
		}

		return result;
	}

	protected String getDescirption(ClientApplicationEvent event) {
		return description.getValue(event, String.class);
	}

	protected Map<String, Object> getDetails(ClientApplicationEvent event) {
		Map<String, Object> details = new HashMap<>();
		if (event instanceof ClientApplicationStatusChangedEvent) {
			details.put("from", ((ClientApplicationStatusChangedEvent) event).getFrom());
			details.put("to", ((ClientApplicationStatusChangedEvent) event).getTo());
		}
		return details;
	}

	public void setUrl(URI url) {
		this.url = url;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public void setClientUrl(URI clientUrl) {
		this.clientUrl = clientUrl;
	}

	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
	}

	public void setDescription(String description) {
		this.description = parser.parseExpression(description, ParserContext.TEMPLATE_EXPRESSION);
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
}
