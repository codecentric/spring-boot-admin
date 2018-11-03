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
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import lombok.Builder;
import lombok.Data;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingFormatArgumentException;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import static java.util.Collections.singletonList;

public class MicrosoftTeamsNotifier extends AbstractStatusChangeNotifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(MicrosoftTeamsNotifier.class);
    private static final String STATUS_KEY = "Status";
    private static final String SERVICE_URL_KEY = "Service URL";
    private static final String HEALTH_URL_KEY = "Health URL";
    private static final String MANAGEMENT_URL_KEY = "Management URL";
    private static final String SOURCE_KEY = "Source";
    private RestTemplate restTemplate = new RestTemplate();

    /**
     * Webhook url for Microsoft Teams Channel Webhook connector (i.e.
     * https://outlook.office.com/webhook/{webhook-id})
     */
    @Nullable
    private URI webhookUrl;

    /**
     * Theme Color is the color of the accent on the message that appears in Microsoft Teams.
     * Default is Spring Green
     */
    private String themeColor = "6db33f";

    /**
     * Message will be used as title of the Activity section of the Teams message when an app
     * de-registers.
     */
    private String deregisterActivitySubtitlePattern = "%s with id %s has de-registered from Spring Boot Admin";

    /**
     * Message will be used as title of the Activity section of the Teams message when an app
     * registers
     */
    private String registerActivitySubtitlePattern = "%s with id %s has registered with Spring Boot Admin";

    /**
     * Message will be used as title of the Activity section of the Teams message when an app
     * changes status
     */
    private String statusActivitySubtitlePattern = "%s with id %s changed status from %s to %s";

    /**
     * Title of the Teams message when an app de-registers
     */
    private String deRegisteredTitle = "De-Registered";

    /**
     * Title of the Teams message when an app registers
     */
    private String registeredTitle = "Registered";

    /**
     * Title of the Teams message when an app changes status
     */
    private String statusChangedTitle = "Status Changed";

    /**
     * Summary section of every Teams message originating from Spring Boot Admin
     */
    private String messageSummary = "Spring Boot Admin Notification";

    public MicrosoftTeamsNotifier(InstanceRepository repository) {
        super(repository);
    }

    @Override
    protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
        Message message;
        if (event instanceof InstanceRegisteredEvent) {
            message = getRegisteredMessage(instance);
        } else if (event instanceof InstanceDeregisteredEvent) {
            message = getDeregisteredMessage(instance);
        } else if (event instanceof InstanceStatusChangedEvent) {
            InstanceStatusChangedEvent statusChangedEvent = (InstanceStatusChangedEvent) event;
            message = getStatusChangedMessage(instance,
                getLastStatus(event.getInstance()),
                statusChangedEvent.getStatusInfo().getStatus()
            );
        } else {
            return Mono.empty();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (webhookUrl == null) {
            return Mono.error(new IllegalStateException("'webhookUrl' must not be null."));
        }

        return Mono.fromRunnable(() -> this.restTemplate.postForEntity(webhookUrl,
            new HttpEntity<Object>(message, headers),
            Void.class
        ));
    }

    @Override
    protected boolean shouldNotify(InstanceEvent event, Instance instance) {
        return event instanceof InstanceRegisteredEvent ||
               event instanceof InstanceDeregisteredEvent ||
               super.shouldNotify(event, instance);
    }

    protected Message getDeregisteredMessage(Instance instance) {
        String activitySubtitle = this.safeFormat(deregisterActivitySubtitlePattern,
            instance.getRegistration().getName(),
            instance.getId()
        );
        return createMessage(instance, deRegisteredTitle, activitySubtitle);
    }

    protected Message getRegisteredMessage(Instance instance) {
        String activitySubtitle = this.safeFormat(registerActivitySubtitlePattern,
            instance.getRegistration().getName(),
            instance.getId()
        );
        return createMessage(instance, registeredTitle, activitySubtitle);
    }

    protected Message getStatusChangedMessage(Instance instance, String statusFrom, String statusTo) {
        String activitySubtitle = this.safeFormat(statusActivitySubtitlePattern,
            instance.getRegistration().getName(),
            instance.getId(),
            statusFrom,
            statusTo
        );
        return createMessage(instance, statusChangedTitle, activitySubtitle);
    }

    private String safeFormat(String format, Object... args) {
        try {
            return String.format(format, args);
        } catch (MissingFormatArgumentException e) {
            LOGGER.warn("Exception while trying to format the message. Falling back by using the format string.", e);
            return format;
        }
    }

    protected Message createMessage(Instance instance, String registeredTitle, String activitySubtitle) {
        List<Fact> facts = new ArrayList<>();
        facts.add(new Fact(STATUS_KEY, instance.getStatusInfo().getStatus()));
        facts.add(new Fact(SERVICE_URL_KEY, instance.getRegistration().getServiceUrl()));
        facts.add(new Fact(HEALTH_URL_KEY, instance.getRegistration().getHealthUrl()));
        facts.add(new Fact(MANAGEMENT_URL_KEY, instance.getRegistration().getManagementUrl()));
        facts.add(new Fact(SOURCE_KEY, instance.getRegistration().getSource()));

        Section section = Section.builder()
                                 .activityTitle(instance.getRegistration().getName())
                                 .activitySubtitle(activitySubtitle)
                                 .facts(facts)
                                 .build();

        return Message.builder()
                      .title(registeredTitle)
                      .summary(messageSummary)
                      .themeColor(themeColor)
                      .sections(singletonList(section))
                      .build();
    }

    public void setWebhookUrl(@Nullable URI webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    @Nullable
    public URI getWebhookUrl() {
        return webhookUrl;
    }

    public void setThemeColor(String themeColor) {
        this.themeColor = themeColor;
    }

    public String getThemeColor() {
        return themeColor;
    }

    public String getDeregisterActivitySubtitlePattern() {
        return deregisterActivitySubtitlePattern;
    }

    public void setDeregisterActivitySubtitlePattern(String deregisterActivitySubtitlePattern) {
        this.deregisterActivitySubtitlePattern = deregisterActivitySubtitlePattern;
    }

    public String getRegisterActivitySubtitlePattern() {
        return registerActivitySubtitlePattern;
    }

    public void setRegisterActivitySubtitlePattern(String registerActivitySubtitlePattern) {
        this.registerActivitySubtitlePattern = registerActivitySubtitlePattern;
    }

    public String getStatusActivitySubtitlePattern() {
        return statusActivitySubtitlePattern;
    }

    public void setStatusActivitySubtitlePattern(String statusActivitySubtitlePattern) {
        this.statusActivitySubtitlePattern = statusActivitySubtitlePattern;
    }


    public String getDeRegisteredTitle() {
        return deRegisteredTitle;
    }

    public void setDeRegisteredTitle(String deRegisteredTitle) {
        this.deRegisteredTitle = deRegisteredTitle;
    }

    public String getRegisteredTitle() {
        return registeredTitle;
    }

    public void setRegisteredTitle(String registeredTitle) {
        this.registeredTitle = registeredTitle;
    }

    public String getStatusChangedTitle() {
        return statusChangedTitle;
    }

    public void setStatusChangedTitle(String statusChangedTitle) {
        this.statusChangedTitle = statusChangedTitle;
    }

    public String getMessageSummary() {
        return messageSummary;
    }

    public void setMessageSummary(String messageSummary) {
        this.messageSummary = messageSummary;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Data
    @Builder
    public static class Message {
        private final String summary;
        private final String themeColor;
        private final String title;
        @Builder.Default
        private final List<Section> sections = new ArrayList<>();
    }

    @Data
    @Builder
    public static class Section {
        private final String activityTitle;
        private final String activitySubtitle;
        @Builder.Default
        private final List<Fact> facts = new ArrayList<>();
    }

    @Data
    public static class Fact {
        private final String name;
        @Nullable
        private final String value;
    }
}
