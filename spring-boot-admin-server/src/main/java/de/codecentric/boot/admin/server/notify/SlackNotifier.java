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
import de.codecentric.boot.admin.server.domain.events.ClientApplicationStatusChangedEvent;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.client.RestTemplate;

/**
 * Notifier submitting events to Slack.
 *
 * @author Artur Dobosiewicz
 */
public class SlackNotifier extends AbstractStatusChangeNotifier {
    private static final String DEFAULT_MESSAGE = "*#{application.registration.name}* (#{application.id}) is *#{event.statusInfo.status}*";

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

    public SlackNotifier(ApplicationRepository repository) {
        super(repository);
        this.message = parser.parseExpression(DEFAULT_MESSAGE, ParserContext.TEMPLATE_EXPRESSION);
    }

    @Override
    protected Mono<Void> doNotify(ClientApplicationEvent event, Application application) {
        return Mono.fromRunnable(
                () -> restTemplate.postForEntity(webhookUrl, createMessage(event, application), Void.class));
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected Object createMessage(ClientApplicationEvent event, Application application) {
        Map<String, Object> messageJson = new HashMap<>();
        messageJson.put("username", username);
        if (icon != null) {
            messageJson.put("icon_emoji", ":" + icon + ":");
        }
        if (channel != null) {
            messageJson.put("channel", channel);
        }

        Map<String, Object> attachments = new HashMap<>();
        attachments.put("text", getText(event, application));
        attachments.put("color", getColor(event));
        attachments.put("mrkdwn_in", Collections.singletonList("text"));
        messageJson.put("attachments", Collections.singletonList(attachments));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(messageJson, headers);
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

    protected String getColor(ClientApplicationEvent event) {
        if (event instanceof ClientApplicationStatusChangedEvent) {
            return "UP".equals(((ClientApplicationStatusChangedEvent) event).getStatusInfo().getStatus()) ?
                    "good" :
                    "danger";
        } else {
            return "#439FE0";
        }
    }

    public URI getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(URI webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message.getExpressionString();
    }

    public void setMessage(String message) {
        this.message = parser.parseExpression(message, ParserContext.TEMPLATE_EXPRESSION);
    }
}
