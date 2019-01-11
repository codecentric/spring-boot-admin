/*
 * Copyright 2014-2018 the original author or authors.
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

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
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
    private static final String DEFAULT_MESSAGE = "*#{instance.registration.name}* (#{instance.id}) is *#{event.statusInfo.status}*";

    private final SpelExpressionParser parser = new SpelExpressionParser();
    private RestTemplate restTemplate = new RestTemplate();

    /**
     * Host URL for Let´s Chat
     */
    @Nullable
    private URI url;

    /**
     * Name of the room
     */
    @Nullable
    private String room;

    /**
     * Token for the Let´s chat API
     */
    @Nullable
    private String token;

    /**
     * username which sends notification
     */
    private String username = "Spring Boot Admin";

    /**
     * Message template. SpEL template using event as root
     */
    private Expression message;

    public LetsChatNotifier(InstanceRepository repository) {
        super(repository);
        this.message = parser.parseExpression(DEFAULT_MESSAGE, ParserContext.TEMPLATE_EXPRESSION);
    }

    @Override
    protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Let's Chat requiers the token as basic username, the password can be an arbitrary string.
        String auth = Base64Utils.encodeToString(String.format("%s:%s", token, username)
                                                       .getBytes(StandardCharsets.UTF_8));
        headers.add(HttpHeaders.AUTHORIZATION, String.format("Basic %s", auth));
        return Mono.fromRunnable(() -> restTemplate.exchange(createUrl(),
            HttpMethod.POST,
            new HttpEntity<>(createMessage(event, instance), headers),
            Void.class
        ));
    }

    private URI createUrl() {
        if (url == null) {
            throw new IllegalStateException("'url' must not be null.");
        }
        return URI.create(String.format("%s/rooms/%s/messages", url, room));
    }

    protected Object createMessage(InstanceEvent event, Instance instance) {
        Map<String, String> messageJson = new HashMap<>();
        messageJson.put("text", getText(event, instance));
        return messageJson;
    }

    @Nullable
    protected String getText(InstanceEvent event, Instance instance) {
        Map<String, Object> root = new HashMap<>();
        root.put("event", event);
        root.put("instance", instance);
        root.put("lastStatus", getLastStatus(event.getInstance()));
        StandardEvaluationContext context = new StandardEvaluationContext(root);
        context.addPropertyAccessor(new MapAccessor());
        return message.getValue(context, String.class);
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void setUrl(@Nullable URI url) {
        this.url = url;
    }

    @Nullable
    public URI getUrl() {
        return url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setRoom(@Nullable String room) {
        this.room = room;
    }

    @Nullable
    public String getRoom() {
        return room;
    }

    public void setToken(@Nullable String token) {
        this.token = token;
    }

    @Nullable
    public String getToken() {
        return token;
    }

    public void setMessage(String message) {
        this.message = parser.parseExpression(message, ParserContext.TEMPLATE_EXPRESSION);
    }

    public String getMessage() {
        return message.getExpressionString();
    }
}
