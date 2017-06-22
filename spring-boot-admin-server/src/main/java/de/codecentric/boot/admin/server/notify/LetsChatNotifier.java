/*
 * Copyright 2014-2017 the original author or authors.
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
package de.codecentric.boot.admin.server.notify;

import de.codecentric.boot.admin.server.domain.entities.Application;
import de.codecentric.boot.admin.server.domain.entities.ApplicationRepository;
import de.codecentric.boot.admin.server.domain.events.ClientApplicationEvent;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;


/**
 * Notifier submitting events to let´s Chat.
 *
 * @author Rico Pahlisch
 */
public class LetsChatNotifier extends AbstractStatusChangeNotifier {
    private static final String DEFAULT_MESSAGE = "*#{application.registration.name}* (#{application.id}) is *#{event.statusInfo.status}*";

    private final SpelExpressionParser parser = new SpelExpressionParser();
    private RestTemplate restTemplate = new RestTemplate();

    /**
     * Host URL for Let´s Chat
     */
    private URI url;

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

    public LetsChatNotifier(ApplicationRepository repository) {
        super(repository);
        this.message = parser.parseExpression(DEFAULT_MESSAGE, ParserContext.TEMPLATE_EXPRESSION);
    }

    @Override
    protected Mono<Void> doNotify(ClientApplicationEvent event, Application application) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Let's Chat requiers the token as basic username, the password can be an arbitrary string.
        String auth = Base64Utils.encodeToString(String.format("%s:%s", token, username).getBytes());
        headers.add(HttpHeaders.AUTHORIZATION, String.format("Basic %s", auth));
        return Mono.fromRunnable(() -> restTemplate.exchange(createUrl(), HttpMethod.POST,
                new HttpEntity<>(createMessage(event, application), headers), Void.class));
    }

    private URI createUrl() {
        return URI.create(String.format("%s/rooms/%s/messages", url, room));
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void setUrl(URI url) {
        this.url = url;
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

    protected Object createMessage(ClientApplicationEvent event, Application application) {
        Map<String, String> messageJson = new HashMap<>();
        messageJson.put("text", getText(event, application));
        return messageJson;
    }

    protected String getText(ClientApplicationEvent event, Application application) {
        Map<String, Object> root = new HashMap<>();
        root.put("event", event);
        root.put("application", application);
        root.put("lastStatus", getLastStatus(event.getApplication()));
        StandardEvaluationContext context = new StandardEvaluationContext(root);
        context.addPropertyAccessor(new MapAccessor());
        return message.getValue(context, String.class);
    }


    public URI getUrl() {
        return url;
    }

    public String getRoom() {
        return room;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message.getExpressionString();
    }
}
