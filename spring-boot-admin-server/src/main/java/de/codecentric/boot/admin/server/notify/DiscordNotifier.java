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
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

/**
 * Notifier submitting events to Discord by webhooks.
 *
 * @author Movitz Sunar
 * @see <a href="https://discordapp.com/developers/docs/resources/webhook#execute-webhook">https://discordapp.com/developers/docs/resources/webhook#execute-webhook</a>
 */
public class DiscordNotifier extends AbstractStatusChangeNotifier {
    private static final String DEFAULT_MESSAGE = "*#{instance.registration.name}* (#{instance.id}) is *#{event.statusInfo.status}*";
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private RestTemplate restTemplate = new RestTemplate();
    private Expression message;

    /**
     * Webhook URI for the Discord API (i.e. https://discordapp.com/api/webhooks/{webhook.id}/{webhook.token})
     */
    @Nullable
    private URI webhookUrl;

    /**
     * If the message is a text to speech message. False by default.
     */
    private boolean tts = false;

    /**
     * Optional username. Default is set in Discord.
     */
    @Nullable
    private String username;

    /**
     * Optional URL to avatar.
     */
    @Nullable
    private String avatarUrl;

    public DiscordNotifier(InstanceRepository repository) {
        super(repository);
        this.message = parser.parseExpression(DEFAULT_MESSAGE, ParserContext.TEMPLATE_EXPRESSION);
    }

    @Override
    protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
        if (webhookUrl == null) {
            return Mono.error(new IllegalStateException("'webhookUrl' must not be null."));
        }
        return Mono.fromRunnable(() -> restTemplate.postForEntity(
            webhookUrl,
            createDiscordNotification(event, instance),
            Void.class
        ));
    }

    protected Object createDiscordNotification(InstanceEvent event, Instance instance) {
        Map<String, Object> body = new HashMap<>();
        body.put("content", createContent(event, instance));
        body.put("tts", tts);

        if (avatarUrl != null) {
            body.put("avatar_url", avatarUrl);
        }
        if (username != null) {
            body.put("username", username);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.USER_AGENT, "RestTemplate");
        return new HttpEntity<>(body, headers);
    }

    @Nullable
    protected String createContent(InstanceEvent event, Instance instance) {
        Map<String, Object> root = new HashMap<>();
        root.put("event", event);
        root.put("instance", instance);
        root.put("lastStatus", getLastStatus(event.getInstance()));
        StandardEvaluationContext context = new StandardEvaluationContext(root);
        context.addPropertyAccessor(new MapAccessor());
        return message.getValue(context, String.class);
    }

    @Nullable
    public URI getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(@Nullable URI webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public boolean isTts() {
        return tts;
    }

    public void setTts(boolean tts) {
        this.tts = tts;
    }

    @Nullable
    public String getUsername() {
        return username;
    }

    public void setUsername(@Nullable String username) {
        this.username = username;
    }

    @Nullable
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(@Nullable String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getMessage() {
        return message.getExpressionString();
    }

    public void setMessage(String message) {
        this.message = parser.parseExpression(message, ParserContext.TEMPLATE_EXPRESSION);
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
