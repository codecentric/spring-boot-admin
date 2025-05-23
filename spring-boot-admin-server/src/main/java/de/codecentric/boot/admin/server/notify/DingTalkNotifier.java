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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.hc.client5.http.utils.Base64;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.DataBindingPropertyAccessor;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;

/**
 * Notifier submitting events to DingTalk.
 *
 * @author Mask
 */
public class DingTalkNotifier extends AbstractStatusChangeNotifier {

	private static final String DEFAULT_MESSAGE = "#{instance.registration.name} #{instance.id} is #{event.statusInfo.status}";

	private final SpelExpressionParser parser = new SpelExpressionParser();

	private RestTemplate restTemplate;

	/**
	 * Webhook URI for the DingTalk API.
	 */
	private String webhookUrl;

	/**
	 * Secret for DingTalk.
	 */
	@Nullable
	private String secret;

	private Expression message;

	public DingTalkNotifier(InstanceRepository repository, RestTemplate restTemplate) {
		super(repository);
		this.restTemplate = restTemplate;
		this.message = parser.parseExpression(DEFAULT_MESSAGE, ParserContext.TEMPLATE_EXPRESSION);
	}

	@Override
	protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
		return Mono
			.fromRunnable(() -> restTemplate.postForEntity(buildUrl(), createMessage(event, instance), Void.class));
	}

	private String buildUrl() {
		Long timestamp = System.currentTimeMillis();
		return String.format("%s&timestamp=%s&sign=%s", webhookUrl, timestamp, getSign(timestamp));
	}

	protected Object createMessage(InstanceEvent event, Instance instance) {
		Map<String, Object> messageJson = new HashMap<>();
		messageJson.put("msgtype", "text");

		Map<String, Object> content = new HashMap<>();
		content.put("content", getText(event, instance));
		messageJson.put("text", content);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new HttpEntity<>(messageJson, headers);
	}

	private Object getText(InstanceEvent event, Instance instance) {
		Map<String, Object> root = new HashMap<>();
		root.put("event", event);
		root.put("instance", instance);
		root.put("lastStatus", getLastStatus(event.getInstance()));
		SimpleEvaluationContext context = SimpleEvaluationContext
			.forPropertyAccessors(DataBindingPropertyAccessor.forReadOnlyAccess(), new MapAccessor())
			.withRootObject(root)
			.build();
		return message.getValue(context, String.class);
	}

	private String getSign(Long timestamp) {
		try {
			String stringToSign = timestamp + "\n" + secret;
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
			return URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return "";
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public String getWebhookUrl() {
		return webhookUrl;
	}

	public void setWebhookUrl(String webhookUrl) {
		this.webhookUrl = webhookUrl;
	}

	@Nullable
	public String getSecret() {
		return secret;
	}

	public void setSecret(@Nullable String secret) {
		this.secret = secret;
	}

	public String getMessage() {
		return message.getExpressionString();
	}

	public void setMessage(String message) {
		this.message = parser.parseExpression(message, ParserContext.TEMPLATE_EXPRESSION);
	}

}
