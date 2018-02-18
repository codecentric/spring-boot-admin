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


import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
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
import org.springframework.web.client.RestTemplate;

/**
 * Notifier submitting events to opsgenie.com.
 *
 * @author Fernando Sure
 */
public class OpsGenieNotifier extends AbstractStatusChangeNotifier {
    private static final URI DEFAULT_URI = URI.create("https://api.opsgenie.com/v1/json/alert");
    private static final String DEFAULT_MESSAGE = "#{instance.registration.name}/#{instance.id} is #{instance.statusInfo.status}";
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private RestTemplate restTemplate = new RestTemplate();

    /**
     * BASE URL for OpsGenie API
     */
    private URI url = DEFAULT_URI;

    /**
     * Integration ApiKey
     */
    private String apiKey;

    /**
     * Comma separated list of users, groups, schedules or escalation names
     * to calculate which users will receive the notifications of the alert.
     */
    private String recipients;

    /**
     * Comma separated list of actions that can be executed.
     */
    private String actions;

    /**
     * Field to specify source of alert. By default, it will be assigned to IP address of incoming request
     */
    private String source;

    /**
     * Comma separated list of labels attached to the alert
     */
    private String tags;

    /**
     * The entity the alert is related to.
     */
    private String entity;

    /**
     * Default owner of the execution. If user is not specified, the system becomes owner of the execution.
     */
    private String user;

    /**
     * Trigger description. SpEL template using event as root;
     */
    private Expression description;

    public OpsGenieNotifier(InstanceRepository repositpry) {
        super(repositpry);
        this.description = parser.parseExpression(DEFAULT_MESSAGE, ParserContext.TEMPLATE_EXPRESSION);
    }


    @Override
    protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
        return Mono.fromRunnable(
            () -> restTemplate.exchange(buildUrl(event), HttpMethod.POST, createRequest(event, instance), Void.class));
    }

    protected String buildUrl(InstanceEvent event) {
        if ((event instanceof InstanceStatusChangedEvent) &&
            (StatusInfo.STATUS_UP.equals(((InstanceStatusChangedEvent) event).getStatusInfo().getStatus()))) {
            return String.format("%s/close", url.toString());
        }
        return url.toString();
    }

    protected HttpEntity createRequest(InstanceEvent event, Instance instance) {
        Map<String, Object> body = new HashMap<>();
        body.put("apiKey", apiKey);
        body.put("message", getMessage(event, instance));
        body.put("alias", instance.getRegistration().getName() + "/" + instance.getId());
        body.put("description", getDescription(event, instance));

        if (event instanceof InstanceStatusChangedEvent &&
            !StatusInfo.STATUS_UP.equals(((InstanceStatusChangedEvent) event).getStatusInfo().getStatus())) {

            if (recipients != null) {
                body.put("recipients", recipients);
            }
            if (actions != null) {
                body.put("actions", actions);
            }
            if (source != null) {
                body.put("source", source);
            }
            if (tags != null) {
                body.put("tags", tags);
            }
            if (entity != null) {
                body.put("entity", entity);
            }
            if (user != null) {
                body.put("user", user);
            }

            Map<String, Object> details = new HashMap<>();
            details.put("type", "link");
            details.put("href", instance.getRegistration().getHealthUrl());
            details.put("text", "Instance health-endpoint");
            body.put("details", details);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    protected String getMessage(InstanceEvent event, Instance instance) {
        Map<String, Object> root = new HashMap<>();
        root.put("event", event);
        root.put("instance", instance);
        root.put("lastStatus", getLastStatus(event.getInstance()));
        StandardEvaluationContext context = new StandardEvaluationContext(root);
        context.addPropertyAccessor(new MapAccessor());
        return description.getValue(context, String.class);
    }

    protected String getDescription(InstanceEvent event, Instance instance) {
        return String.format("Instance %s (%s) went from %s to %s", instance.getRegistration().getName(),
            instance.getId(), getLastStatus(instance.getId()),
            ((InstanceStatusChangedEvent) event).getStatusInfo().getStatus());
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setDescription(String description) {
        this.description = parser.parseExpression(description, ParserContext.TEMPLATE_EXPRESSION);
    }

    public String getMessage() {
        return description.getExpressionString();
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getRecipients() {
        return recipients;
    }

    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

}
