package de.codecentric.boot.admin.notify;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;

/**
 * Notifier submitting events to HipChat.
 *
 * @author Jamie Brown
 */
public class HipchatNotifier extends AbstractStatusChangeNotifier {
	private final static String DEFAULT_DESCRIPTION = "<strong>#{application.name}</strong>/#{application.id} is <strong>#{to.status}</strong>";

	private final SpelExpressionParser parser = new SpelExpressionParser();
	private RestTemplate restTemplate = new RestTemplate();

	/**
	 * Base URL for HipChat API (i.e. https://ACCOUNT_NAME.hipchat.com/v2
	 */
	private URI url;

	/**
	 * API token that has access to notify in the room
	 */
	private String authToken;

	/**
	 * Id of the room to notify
	 */
	private String roomId;

	/**
	 * TRUE will cause OS notification, FALSE will only notify to room
	 */
	private Boolean notify;

	/**
	 * Trigger description. SpEL template using event as root;
	 */
	private Expression description;

	public HipchatNotifier() {
		this.description = parser.parseExpression(DEFAULT_DESCRIPTION,
				ParserContext.TEMPLATE_EXPRESSION);
	}

	@Override
	protected void doNotify(ClientApplicationStatusChangedEvent event) {
		restTemplate.postForEntity(buildUrl(), createHipChatNotification(event), Void.class);
	}

	protected String buildUrl() {
		return String.format("%s/room/%s/notification?auth_token=%s", url.toString(), roomId,
				authToken);
	}

	private Map<String, Object> createHipChatNotification(
			ClientApplicationStatusChangedEvent event) {
		Map<String, Object> result = new HashMap<String, Object>();
		String color = "UP".equals(event.getTo().getStatus()) ? "green" : "red";
		String message = description.getValue(event, String.class);
		result.put("color", color);
		result.put("message", message);
		result.put("notify", Boolean.TRUE.equals(notify));
		result.put("message_format", "html");
		return result;
	}

	public void setUrl(URI url) {
		this.url = url;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public void setNotify(Boolean notify) {
		this.notify = notify;
	}

	public void setDescription(String description) {
		this.description = parser.parseExpression(description, ParserContext.TEMPLATE_EXPRESSION);
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
}
