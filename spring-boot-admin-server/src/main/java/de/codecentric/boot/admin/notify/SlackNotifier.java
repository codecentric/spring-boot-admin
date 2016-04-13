package de.codecentric.boot.admin.notify;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;

/**
 * Notifier submitting events to Slack.
 *
 * @author Artur Dobosiewicz
 */
public class SlackNotifier extends AbstractStatusChangeNotifier {
	private static final String DEFAULT_MESSAGE = "*#{application.name}* (#{application.id}) is *#{to.status}*";

	private final SpelExpressionParser parser = new SpelExpressionParser();
	private RestTemplate restTemplate = new RestTemplate();

	/**
	 * Webhook url for Slack API (i.e. https://hooks.slack.com/services/xxx)
	 */
	private URI webhookUrl;

	/**
	 * Optional channel name without # sign (i.e. somechannel)
	 */
	private String channel;

	/**
	 * Optional emoji icon without colons (i.e. my-emoji)
	 */
	private String icon;

	/**
	 * Optional username which sends notification
	 */
	private String username = "Spring Boot Admin";

	/**
	 * Message formatted using Slack markups. SpEL template using event as root
	 */
	private Expression message;

	public SlackNotifier() {
		this.message = parser.parseExpression(DEFAULT_MESSAGE, ParserContext.TEMPLATE_EXPRESSION);
	}

	@Override
	protected void doNotify(ClientApplicationStatusChangedEvent event) throws Exception {
		restTemplate.postForEntity(webhookUrl, createMessage(event), Void.class);
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public void setWebhookUrl(URI webhookUrl) {
		this.webhookUrl = webhookUrl;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setMessage(Expression message) {
		this.message = message;
	}

	private Object createMessage(ClientApplicationStatusChangedEvent event) {
		Map<String, Object> messageJson = new HashMap<>();
		messageJson.put("username", username);
		if (icon != null) {
			messageJson.put("icon_emoji", ":" + icon + ":");
		}
		if (channel != null) {
			messageJson.put("channel", channel);
		}

		Map<String, Object> attachments = new HashMap<>();
		attachments.put("text", getText(event));
		attachments.put("color", getColor(event));
		attachments.put("mrkdwn_in", Collections.singletonList("text"));

		messageJson.put("attachments", Collections.singletonList(attachments));

		return messageJson;
	}

	private String getText(ClientApplicationStatusChangedEvent event) {
		return message.getValue(event, String.class);
	}

	private String getColor(ClientApplicationStatusChangedEvent event) {
		return "UP".equals(event.getTo().getStatus()) ? "good" : "danger";
	}
}
