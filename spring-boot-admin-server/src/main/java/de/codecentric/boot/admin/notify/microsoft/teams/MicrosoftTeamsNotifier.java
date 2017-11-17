package de.codecentric.boot.admin.notify.microsoft.teams;

import de.codecentric.boot.admin.event.ClientApplicationDeregisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationEvent;
import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;
import de.codecentric.boot.admin.notify.AbstractEventNotifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

public class MicrosoftTeamsNotifier extends AbstractEventNotifier {

    static final String DE_REGISTERED_TITLE = "De-Registered";
    static final String REGISTERED_TITLE = "Registered";
    static final String STATUS_CHANGED_TITLE = "Status Changed";
    static final String MESSAGE_SUMMARY = "Spring Boot Admin Notification";
    static final String DEREGISTER_ACTIVITY_SUBTITLE_PATTERN = "%s with id %s has de-registered" +
            " from Spring Boot Admin";
    static final String REGISTER_ACTIVITY_SUBTITLE_PATTERN = "%s with id %s has registered" +
            " from Spring Boot Admin";
    static final String STATUS_ACTIVITY_SUBTITLE_PATTERN = "%s with id %s changed status from %s to %s";
    static final String STATUS_KEY = "Status";
    static final String SERVICE_URL_KEY = "Service URL";
    static final String HEALTH_URL_KEY = "Health URL";
    static final String MANAGEMENT_URL_KEY = "Management URL";
    static final String SOURCE_KEY = "Source";


    /**
     * Webhook url for Microsoft Teams Channel Webhook connector
     *  (i.e. https://outlook.office.com/webhook/{webhook-id})
     */
    private URI webhookUrl;

    /**
     * Image url for image displayed in activity section
     *  (i.e. https://raw.githubusercontent.com/tomd8451/spring-boot-admin
     *          /master/spring-boot-admin-server-ui/core/img/platform-spring-boot.jpg)
     */
    private URI imageUrl = URI.create("https://raw.githubusercontent.com/tomd8451/spring-boot-admin" +
            "/master/spring-boot-admin-server-ui/core/img/platform-spring-boot.jpg");

    /**
     * Theme Color is the color of the accent on the message that appears in Microsoft Teams. Default is Spring Green
     */
    private String themeColor = "6db33f";

    private RestTemplate restTemplate;

    public MicrosoftTeamsNotifier() {
        // Initialize the restTemplate using builder to allow for customization
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        this.restTemplate = restTemplateBuilder.build();
    }

    Message getDeregisteredMessage(Application app) {

        return getMessage(app, DE_REGISTERED_TITLE, String.format(DEREGISTER_ACTIVITY_SUBTITLE_PATTERN,
                app.getName(),
                app.getId()));
    }

    Message getRegisteredMessage(Application app) {
        return getMessage(app, REGISTERED_TITLE, String.format(REGISTER_ACTIVITY_SUBTITLE_PATTERN,
                app.getName(),
                app.getId()));
    }

    Message getStatusChangedMessage(Application app, StatusInfo from, StatusInfo to) {
        return getMessage(app, STATUS_CHANGED_TITLE, String.format(STATUS_ACTIVITY_SUBTITLE_PATTERN,
                app.getName(),
                app.getId(),
                from.getStatus(),
                to.getStatus()));
    }

    private Message getMessage(Application app, String registeredTitle, String activitySubtitle) {
        Message message = new Message();

        message.setTitle(registeredTitle);
        message.setSummary(MESSAGE_SUMMARY);
        message.setThemeColor(themeColor);

        Section section = new Section();
        section.setActivityTitle(app.getName());
        if(imageUrl != null) {
            section.setActivityImage(imageUrl.toString());
        }
        section.setActivitySubtitle(activitySubtitle);
        section.getFacts().add(new Fact(STATUS_KEY, app.getStatusInfo().getStatus()));
        section.getFacts().add(new Fact(SERVICE_URL_KEY, app.getServiceUrl()));
        section.getFacts().add(new Fact(HEALTH_URL_KEY, app.getHealthUrl()));
        section.getFacts().add(new Fact(MANAGEMENT_URL_KEY, app.getManagementUrl()));
        section.getFacts().add(new Fact(SOURCE_KEY, app.getSource()));

        message.getSections().add(section);

        return message;
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
            Message statusChangeMessage = getStatusChangedMessage(statusChangedEvent.getApplication(),
                    statusChangedEvent.getFrom(),
                    statusChangedEvent.getTo());

            this.restTemplate.postForObject(webhookUrl, statusChangeMessage, Object.class);
        }
    }

    @Override
    protected boolean shouldNotify(ClientApplicationEvent event) {
        return event instanceof ClientApplicationRegisteredEvent ||
                event instanceof  ClientApplicationDeregisteredEvent ||
                event instanceof  ClientApplicationStatusChangedEvent;
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

    public URI getImageUrl() { return imageUrl; }

    public void setImageUrl(URI imageUrl) { this.imageUrl = imageUrl; }

    public String getThemeColor() { return themeColor; }

    public void setThemeColor(String themeColor) { this.themeColor = themeColor; }

}
