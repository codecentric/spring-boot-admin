/*
 * Copyright 2014-2022 the original author or authors.
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
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;

/**
 * Notifier submitting events to FeiShu by webhooks.
 *
 * @author sweeter
 * @see <a href=
 * "https://open.feishu.cn/document/ukTMukTMukTM/ucTM5YjL3ETO24yNxkjN">https://open.feishu.cn/document/ukTMukTMukTM/ucTM5YjL3ETO24yNxkjN</a>
 *
 */
public class FeiShuNotifier extends AbstractStatusChangeNotifier {

	private static final String DEFAULT_MESSAGE = "ServiceName: #{instance.registration.name}(#{instance.id}) \nServiceUrl: #{instance.registration.serviceUrl} \nStatus: changed status from [#{lastStatus}] to [#{event.statusInfo.status}]";

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final SpelExpressionParser parser = new SpelExpressionParser();

	private RestTemplate restTemplate;

	private Expression message;

	/**
	 * Webhook URL for the FeiShu(飞书) chat group API (i.e.
	 * https://open.feishu.cn/open-apis/bot/v2/hook/xxx).
	 */
	private URI webhookUrl;

	/**
	 * @ all.
	 */
	private boolean atAll = true;

	/**
	 * The secret of the chat group robot from the FeiShu setup.
	 */
	private String secret;

	/**
	 * FeiShu message type: text(文本) interactive(消息卡片)
	 */
	private MessageType messageType = MessageType.interactive;

	/**
	 * Card theme message
	 */
	private Card card = new Card();

	public FeiShuNotifier(InstanceRepository repository, RestTemplate restTemplate) {
		super(repository);
		this.restTemplate = restTemplate;
		this.message = this.parser.parseExpression(DEFAULT_MESSAGE, ParserContext.TEMPLATE_EXPRESSION);
	}

	@Override
	protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
		if (webhookUrl == null) {
			return Mono.error(new IllegalStateException("'webhookUrl' must not be null."));
		}
		return Mono.fromRunnable(() -> {
			ResponseEntity<String> responseEntity = this.restTemplate.postForEntity(this.webhookUrl,
					this.createNotification(event, instance), String.class);
			log.debug("Send a notification message to the FeiShu group,returns the parameter：{}",
					responseEntity.getBody());
		});
	}

	private String generateSign(String secret, long timestamp) {
		try {
			String stringToSign = timestamp + "\n" + secret;
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(stringToSign.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			byte[] signData = mac.doFinal(new byte[] {});
			return new String(Base64.getEncoder().encode(signData));
		}
		catch (Exception ex) {
			log.error("Description Failed to generate the Webhook signature of the FeiShu：{}", ex.getMessage());
		}
		return null;
	}

	protected HttpEntity<Map<String, Object>> createNotification(InstanceEvent event, Instance instance) {
		Map<String, Object> body = new HashMap<>();
		body.put("receive_id", UUID.randomUUID().toString());
		if (StringUtils.hasText(this.secret)) {
			long timestamp = Instant.now().getEpochSecond();
			body.put("timestamp", timestamp);
			body.put("sign", this.generateSign(this.secret, timestamp));
		}
		body.put("msg_type", this.messageType);
		switch (this.messageType) {
			case interactive:
				body.put("card", this.createCardContent(event, instance));
				break;
			case text:
				body.put("content", this.createTextContent(event, instance));
				break;

			default:
				body.put("content", this.createTextContent(event, instance));
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("User-Agent", "Codecentric's Spring Boot Admin");
		return new HttpEntity<>(body, headers);
	}

	private String createContent(InstanceEvent event, Instance instance) {
		Map<String, Object> root = new HashMap<>();
		root.put("event", event);
		root.put("instance", instance);
		root.put("lastStatus", this.getLastStatus(event.getInstance()));
		StandardEvaluationContext context = new StandardEvaluationContext(root);
		context.addPropertyAccessor(new MapAccessor());
		return this.message.getValue(context, String.class);
	}

	private String createTextContent(InstanceEvent event, Instance instance) {
		Map<String, Object> textContent = new HashMap<>();
		String content = this.createContent(event, instance);
		if (this.atAll) {
			content += "\n<at user_id=\"all\">@all</at>";
		}
		textContent.put("text", content);
		return this.toJsonString(textContent);
	}

	private String createCardContent(InstanceEvent event, Instance instance) {
		String content = this.createContent(event, instance);
		Card card = this.card;

		Map<String, Object> header = new HashMap<>();
		header.put("template", StringUtils.hasText(card.getThemeColor()) ? "red" : card.getThemeColor());
		Map<String, String> titleContent = new HashMap<>();
		titleContent.put("tag", "plain_text");
		titleContent.put("content", card.getTitle());
		header.put("title", titleContent);

		List<Map<String, Object>> elements = new ArrayList<>();
		Map<String, Object> item = new HashMap<>();
		item.put("tag", "div");

		Map<String, String> text = new HashMap<>();
		text.put("tag", "plain_text");
		text.put("content", content);
		item.put("text", text);
		elements.add(item);

		if (this.atAll) {
			Map<String, Object> atItem = new HashMap<>();
			atItem.put("tag", "div");
			Map<String, String> atText = new HashMap<>();
			atText.put("tag", "lark_md");
			atText.put("content", "<at id=all></at>");
			atItem.put("text", atText);
			elements.add(atItem);
		}
		Map<String, Object> cardContent = new HashMap<>();
		cardContent.put("header", header);
		cardContent.put("elements", elements);
		return this.toJsonString(cardContent);
	}

	private String toJsonString(Object o) {
		try {
			ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
			return objectMapper.writeValueAsString(o);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public URI getWebhookUrl() {
		return this.webhookUrl;
	}

	public void setWebhookUrl(URI webhookUrl) {
		this.webhookUrl = webhookUrl;
	}

	public String getMessage() {
		return this.message.getExpressionString();
	}

	public void setMessage(String message) {
		this.message = this.parser.parseExpression(message, ParserContext.TEMPLATE_EXPRESSION);
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public boolean isAtAll() {
		return atAll;
	}

	public void setAtAll(boolean atAll) {
		this.atAll = atAll;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	public enum MessageType {

		text, interactive

	}

	public class Card {

		/**
		 * This is header title.
		 */
		private String title = "Codecentric's Spring Boot Admin notice";

		private String themeColor = "red";

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getThemeColor() {
			return themeColor;
		}

		public void setThemeColor(String themeColor) {
			this.themeColor = themeColor;
		}

	}

}
