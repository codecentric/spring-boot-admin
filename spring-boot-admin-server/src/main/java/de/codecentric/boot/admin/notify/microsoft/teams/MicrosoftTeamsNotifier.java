package de.codecentric.boot.admin.notify.microsoft.teams;

import de.codecentric.boot.admin.event.ClientApplicationDeregisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationEvent;
import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;
import de.codecentric.boot.admin.notify.AbstractEventNotifier;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;

public class MicrosoftTeamsNotifier extends AbstractEventNotifier {

    public static final String DE_REGISTERED_TITLE = "De-Registered";
    public static final String REGISTERED_TITLE = "Registered";
    public static final String STATUS_CHANGED_TITLE = "Status Changed";

    public static final String MESSAGE_SUMMARY = "Spring Boot Admin Notification";

    public static final String DEREGISTER_ACTIVITY_SUBTITLE_PATTERN = "%s with id %s has de-registered" +
            " from Spring Boot Admin";
    public static final String REGISTER_ACTIVITY_SUBTITLE_PATTERN = "%s with id %s has registered" +
            " from Spring Boot Admin";
    public static final String STATUS_ACTIVITY_SUBTITLE_PATTERN = "%s with id %s changed status from %s to %s";

    public static final String STATUS_KEY = "Status";
    public static final String SERVICE_URL_KEY = "Service URL";
    public static final String HEALTH_URL_KEY = "Health URL";
    public static final String MANAGEMENT_URL_KEY = "Management URL";
    public static final String SOURCE_KEY = "Source";


    /**
     * Webhook url for Microsoft Teams Channel Webhook connector
     *  (i.e. https://outlook.office.com/webhook/{webhook-id})
     */
    private URI webhookUrl;
    private RestTemplate restTemplate;

    public MicrosoftTeamsNotifier() {
        // Initialize the restTemplate using builder to allow for customization
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    protected void doNotify(ClientApplicationEvent event) throws Exception {
        if(event instanceof ClientApplicationRegisteredEvent) {
            ClientApplicationRegisteredEvent registeredEvent = (ClientApplicationRegisteredEvent)event;
            Message registeredMessage = getRegisteredMessage(registeredEvent.getApplication());

            this.restTemplate.postForObject(webhookUrl, registeredMessage, Object.class);

        } else if (event instanceof ClientApplicationDeregisteredEvent) {
            ClientApplicationDeregisteredEvent deregisteredEvent = (ClientApplicationDeregisteredEvent)event;
            Message deRegisteredMessage = getDeregisteredMessage(deregisteredEvent.getApplication());

            this.restTemplate.postForObject(webhookUrl, deRegisteredMessage, Object.class);

        } else {
            ClientApplicationStatusChangedEvent statusChangedEvent = (ClientApplicationStatusChangedEvent)event;
            Message statusChangeMessage = getStatusChangedMessage(event.getApplication(),
                    ((ClientApplicationStatusChangedEvent) event).getFrom(),
                    ((ClientApplicationStatusChangedEvent) event).getTo());

            this.restTemplate.postForObject(webhookUrl, statusChangeMessage, Object.class);
        }
    }

    @Override
    protected boolean shouldNotify(ClientApplicationEvent event) {
        return event instanceof ClientApplicationRegisteredEvent ||
                event instanceof  ClientApplicationDeregisteredEvent ||
                event instanceof  ClientApplicationStatusChangedEvent;
    }

    public static Message getDeregisteredMessage(Application app) {
        Message message = getMessage(app, DE_REGISTERED_TITLE, String.format(DEREGISTER_ACTIVITY_SUBTITLE_PATTERN,
                app.getName(),
                app.getId()));

        return message;
    }

    public static Message getRegisteredMessage(Application app) {
        return getMessage(app, REGISTERED_TITLE, String.format(REGISTER_ACTIVITY_SUBTITLE_PATTERN,
                app.getName(),
                app.getId()));
    }

    public static Message getStatusChangedMessage(Application app, StatusInfo from, StatusInfo to) {
        return getMessage(app, STATUS_CHANGED_TITLE, String.format(STATUS_ACTIVITY_SUBTITLE_PATTERN,
                app.getName(),
                app.getId(),
                from,
                to));
    }

    @NotNull
    private static Message getMessage(Application app, String registeredTitle, String activitySubtitle) {
        Message message = new Message();

        message.setTitle(registeredTitle);
        message.setSummary(MESSAGE_SUMMARY);

        Section section = new Section();
        section.setActivityTitle(app.getName());
        section.setActivitySubtitle(activitySubtitle);
        section.setFacts(new HashMap<String, String>());
        section.getFacts().put(STATUS_KEY, app.getStatusInfo().getStatus());
        section.getFacts().put(SERVICE_URL_KEY, app.getServiceUrl());
        section.getFacts().put(HEALTH_URL_KEY, app.getHealthUrl());
        section.getFacts().put(MANAGEMENT_URL_KEY, app.getManagementUrl());
        section.getFacts().put(SOURCE_KEY, app.getSource());

        message.getSections().add(section);

        return message;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public URI getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(URI webhookUrl) {
        this.webhookUrl = webhookUrl;
    }



}
