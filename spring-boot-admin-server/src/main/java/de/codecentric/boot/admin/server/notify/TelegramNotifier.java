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

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.client.RestTemplate;

/**
 * Notifier submitting events to Telegram.
 */
public class TelegramNotifier extends AbstractStatusChangeNotifier {
    private static final String DEFAULT_MESSAGE = "<strong>#{instance.registration.name}</strong>/#{instance.id} is <strong>#{event.statusInfo.status}</strong>";
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private RestTemplate restTemplate = new RestTemplate();

    /**
     * base url for telegram (i.e. https://api.telegram.org)
     */
    private String apiUrl = "https://api.telegram.org";

    /**
     * Unique identifier for the target chat or username of the target channel
     */
    @Nullable
    private String chatId;

    /**
     * The token identifiying und authorizing your Telegram bot (e.g. `123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11`)
     */
    @Nullable
    private String authToken;

    /**
     * Send Markdown or HTML, if you want Telegram apps to show bold, italic, fixed-width text or
     * inline URLs in your bot's message.
     */
    private String parseMode = "HTML";

    /**
     * If true users will receive a notification with no sound.
     */
    private boolean disableNotify = false;

    private Expression message;

    public TelegramNotifier(InstanceRepository repository) {
        super(repository);
        this.message = parser.parseExpression(DEFAULT_MESSAGE, ParserContext.TEMPLATE_EXPRESSION);
    }

    @Override
    protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
        return Mono.fromRunnable(
            () -> restTemplate.getForObject(buildUrl(), Void.class, createMessage(event, instance)));
    }

    protected String buildUrl() {
        return String.format("%s/bot%s/sendmessage?chat_id={chat_id}&text={text}&parse_mode={parse_mode}" +
                             "&disable_notification={disable_notification}", this.apiUrl, this.authToken);
    }

    private Map<String, Object> createMessage(InstanceEvent event, Instance instance) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("chat_id", this.chatId);
        parameters.put("parse_mode", this.parseMode);
        parameters.put("disable_notification", this.disableNotify);
        parameters.put("text", getText(event, instance));
        return parameters;
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

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    @Nullable
    public String getChatId() {
        return chatId;
    }

    public void setChatId(@Nullable String chatId) {
        this.chatId = chatId;
    }

    @Nullable
    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(@Nullable String authToken) {
        this.authToken = authToken;
    }

    public boolean isDisableNotify() {
        return disableNotify;
    }

    public void setDisableNotify(boolean disableNotify) {
        this.disableNotify = disableNotify;
    }

    public String getParseMode() {
        return parseMode;
    }

    public void setParseMode(String parseMode) {
        this.parseMode = parseMode;
    }

    public void setMessage(String message) {
        this.message = parser.parseExpression(message, ParserContext.TEMPLATE_EXPRESSION);
    }
}
