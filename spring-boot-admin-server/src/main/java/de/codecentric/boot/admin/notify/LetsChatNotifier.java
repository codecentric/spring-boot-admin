package de.codecentric.boot.admin.notify;

import de.codecentric.boot.admin.event.ClientApplicationEvent;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;


/**
 * Notifier submitting events to let´s Chat.
 *
 * @author Rico Pahlisch
 */
public class LetsChatNotifier extends AbstractStatusChangeNotifier {
	private static final String DEFAULT_MESSAGE = "*#{application.name}* (#{application.id}) is *#{to.status}*";

	private final SpelExpressionParser parser = new SpelExpressionParser();
	private RestTemplate restTemplate = new RestTemplate();

	/**
	 * Host URL for Let´s Chat
	 */
	private URI hostUrl;

	/**
	 * Name of the room
	 */
	private String room;

	/**
	 * Token for the Let´s chat API
	 */
	private String token;

	/**
	 * username which sends notification
	 */
	private String username = "Spring Boot Admin";

	/**
	 * Message template. SpEL template using event as root
	 */
	private Expression message;

	public LetsChatNotifier() {
		this.message = parser.parseExpression(DEFAULT_MESSAGE, ParserContext.TEMPLATE_EXPRESSION);
	}

	@Override
	protected void doNotify(ClientApplicationEvent event) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String auth = Base64Utils.encodeToString(String.format("%s:%s", token, username).getBytes());
		headers.add(HttpHeaders.AUTHORIZATION, String.format("Basic %s", auth));

		restTemplate.exchange(createUrl(), HttpMethod.POST, new HttpEntity<>(createMessage(event), headers), Void.class);
	}

	private URI createUrl() {
		return URI.create(String.format("%s/rooms/%s/messages", hostUrl, room));
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public void setHostUrl(URI hostUrl) {
		this.hostUrl = hostUrl;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setMessage(String message) {
		this.message = parser.parseExpression(message, ParserContext.TEMPLATE_EXPRESSION);
	}

	protected Object createMessage(ClientApplicationEvent event) {
		Map<String, String> messageJson = new HashMap<>();
		messageJson.put("text", getText(event));
		return messageJson;
	}

	protected String getText(ClientApplicationEvent event) {
		return message.getValue(event, String.class);
	}
}
