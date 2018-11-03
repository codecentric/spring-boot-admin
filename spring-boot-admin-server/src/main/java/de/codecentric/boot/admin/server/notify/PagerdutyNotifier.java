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
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
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
import org.springframework.web.client.RestTemplate;

import static java.util.Collections.singletonList;

/**
 * Notifier submitting events to Pagerduty.
 *
 * @author Johannes Edmeier
 */
public class PagerdutyNotifier extends AbstractStatusChangeNotifier {
    public static final URI DEFAULT_URI = URI.create(
        "https://events.pagerduty.com/generic/2010-04-15/create_event.json");

    private static final String DEFAULT_DESCRIPTION = "#{instance.registration.name}/#{instance.id} is #{instance.statusInfo.status}";

    private final SpelExpressionParser parser = new SpelExpressionParser();
    private RestTemplate restTemplate = new RestTemplate();

    /**
     * URI for pagerduty-REST-API
     */
    private URI url = DEFAULT_URI;

    /**
     * Service-Key for pagerduty-REST-API
     */
    @Nullable
    private String serviceKey;

    /**
     * Client for pagerduty-REST-API
     */
    @Nullable
    private String client;

    /**
     * Client-url for pagerduty-REST-API
     */
    @Nullable
    private URI clientUrl;

    /**
     * Trigger description. SpEL template using event as root;
     */
    private Expression description;

    public PagerdutyNotifier(InstanceRepository repository) {
        super(repository);
        this.description = parser.parseExpression(DEFAULT_DESCRIPTION, ParserContext.TEMPLATE_EXPRESSION);
    }


    @Override
    protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
        return Mono.fromRunnable(
            () -> restTemplate.postForEntity(url, createPagerdutyEvent(event, instance), Void.class));
    }


    protected Map<String, Object> createPagerdutyEvent(InstanceEvent event, Instance instance) {
        Map<String, Object> result = new HashMap<>();
        result.put("service_key", serviceKey);
        result.put("incident_key", instance.getRegistration().getName() + "/" + event.getInstance());
        result.put("description", getDescription(event, instance));

        Map<String, Object> details = getDetails(event);
        result.put("details", details);

        if (event instanceof InstanceStatusChangedEvent) {
            if ("UP".equals(((InstanceStatusChangedEvent) event).getStatusInfo().getStatus())) {
                result.put("event_type", "resolve");
            } else {
                result.put("event_type", "trigger");
                if (client != null) {
                    result.put("client", client);
                }
                if (clientUrl != null) {
                    result.put("client_url", clientUrl);
                }

                Map<String, Object> context = new HashMap<>();
                context.put("type", "link");
                context.put("href", instance.getRegistration().getHealthUrl());
                context.put("text", "Application health-endpoint");
                result.put("contexts", singletonList(context));
            }
        }

        return result;
    }

    @Nullable
    protected String getDescription(InstanceEvent event, Instance instance) {
        Map<String, Object> root = new HashMap<>();
        root.put("event", event);
        root.put("instance", instance);
        root.put("lastStatus", getLastStatus(event.getInstance()));
        StandardEvaluationContext context = new StandardEvaluationContext(root);
        context.addPropertyAccessor(new MapAccessor());

        return description.getValue(context, String.class);
    }

    protected Map<String, Object> getDetails(InstanceEvent event) {
        Map<String, Object> details = new HashMap<>();
        if (event instanceof InstanceStatusChangedEvent) {
            details.put("from", this.getLastStatus(event.getInstance()));
            details.put("to", ((InstanceStatusChangedEvent) event).getStatusInfo());
        }
        return details;
    }

    public void setUrl(URI url) {
        this.url = url;
    }

    public URI getUrl() {
        return url;
    }

    public void setClient(@Nullable String client) {
        this.client = client;
    }

    @Nullable
    public String getClient() {
        return client;
    }

    public void setClientUrl(@Nullable URI clientUrl) {
        this.clientUrl = clientUrl;
    }

    @Nullable
    public URI getClientUrl() {
        return clientUrl;
    }

    public void setServiceKey(@Nullable String serviceKey) {
        this.serviceKey = serviceKey;
    }

    @Nullable
    public String getServiceKey() {
        return serviceKey;
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
