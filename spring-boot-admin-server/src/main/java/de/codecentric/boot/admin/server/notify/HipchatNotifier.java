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
package de.codecentric.boot.admin.server.notify;

import de.codecentric.boot.admin.server.event.ClientApplicationEvent;
import de.codecentric.boot.admin.server.event.ClientApplicationStatusChangedEvent;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

/**
 * Notifier submitting events to HipChat.
 *
 * @author Jamie Brown
 */
public class HipchatNotifier extends AbstractStatusChangeNotifier {
    private static final String DEFAULT_DESCRIPTION = "<strong>#{application.registration.name}</strong>/#{application.id} is <strong>#{to.status}</strong>";

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
    private boolean notify = false;

    /**
     * Trigger description. SpEL template using event as root;
     */
    private Expression description;

    public HipchatNotifier() {
        this.description = parser.parseExpression(DEFAULT_DESCRIPTION, ParserContext.TEMPLATE_EXPRESSION);
    }

    @Override
    protected void doNotify(ClientApplicationEvent event) {
        restTemplate.postForEntity(buildUrl(), createHipChatNotification(event), Void.class);
    }

    protected String buildUrl() {
        return String.format("%s/room/%s/notification?auth_token=%s", url.toString(), roomId, authToken);
    }

    protected HttpEntity<Map<String, Object>> createHipChatNotification(ClientApplicationEvent event) {
        Map<String, Object> body = new HashMap<>();
        body.put("color", getColor(event));
        body.put("message", getMessage(event));
        body.put("notify", getNotify());
        body.put("message_format", "html");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    protected boolean getNotify() {
        return notify;
    }

    protected String getMessage(ClientApplicationEvent event) {
        return description.getValue(event, String.class);
    }

    protected String getColor(ClientApplicationEvent event) {
        if (event instanceof ClientApplicationStatusChangedEvent) {
            return "UP".equals(((ClientApplicationStatusChangedEvent) event).getTo().getStatus()) ? "green" : "red";
        } else {
            return "gray";
        }
    }

    public void setUrl(URI url) {
        this.url = url;
    }

    public URI getUrl() {
        return url;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setDescription(String description) {
        this.description = parser.parseExpression(description, ParserContext.TEMPLATE_EXPRESSION);
    }

    public String getDescription() {
        return description.getExpressionString();
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
