package de.codecentric.boot.admin.notify;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.codecentric.boot.admin.event.ClientApplicationDeregisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationEvent;
import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingFormatArgumentException;

public class MicrosoftTeamsNotifier extends AbstractEventNotifier {

    public static final String STATUS_KEY = "Status";
    public static final String SERVICE_URL_KEY = "Service URL";
    public static final String HEALTH_URL_KEY = "Health URL";
    public static final String MANAGEMENT_URL_KEY = "Management URL";
    public static final String SOURCE_KEY = "Source";
    private final RestTemplate restTemplate;

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

    /**
     * Message will be used as title of the Activity section of the Teams message when an app de-registers.
     */
    private String deregisterActivitySubtitlePattern = "%s with id %s has de-registered" +
            " from Spring Boot Admin";

    /**
     * Message will be used as title of the Activity section of the Teams message when an app registers
     */
    private String registerActivitySubtitlePattern = "%s with id %s has registered" +
            " from Spring Boot Admin";

    /**
     * Message will be used as title of the Activity section of the Teams message when an app changes status
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

    @Override
    protected void doNotify(ClientApplicationEvent event) throws Exception {
        Message message = null;
        if(event instanceof ClientApplicationRegisteredEvent) {
            ClientApplicationRegisteredEvent registeredEvent = (ClientApplicationRegisteredEvent)event;
            message = getRegisteredMessage(registeredEvent.getApplication());

        } else if (event instanceof ClientApplicationDeregisteredEvent) {
            ClientApplicationDeregisteredEvent deregisteredEvent = (ClientApplicationDeregisteredEvent)event;
            message = getDeregisteredMessage(deregisteredEvent.getApplication());

        } else {
            ClientApplicationStatusChangedEvent statusChangedEvent = (ClientApplicationStatusChangedEvent)event;
            message = getStatusChangedMessage(statusChangedEvent.getApplication(),
                    statusChangedEvent.getFrom(),
                    statusChangedEvent.getTo());
        }

        this.restTemplate.postForObject(webhookUrl, message, Void.class);
    }

    @Override
    protected boolean shouldNotify(ClientApplicationEvent event) {
        return event instanceof ClientApplicationRegisteredEvent ||
                event instanceof  ClientApplicationDeregisteredEvent ||
                event instanceof  ClientApplicationStatusChangedEvent;
    }

    protected Message getDeregisteredMessage(Application app) {
        Message message = null;
        try {
            message = getMessage(app, deRegisteredTitle, String.format(deregisterActivitySubtitlePattern,
                    app.getName(),
                    app.getId()));
        } catch( MissingFormatArgumentException e) {
            message = getMessage(app, deRegisteredTitle, deregisterActivitySubtitlePattern);
        }

        return message;
    }

    protected Message getRegisteredMessage(Application app) {
        Message message = null;
        try {
            message = getMessage(app, registeredTitle, String.format(registerActivitySubtitlePattern,
                    app.getName(),
                    app.getId()));
        } catch( MissingFormatArgumentException e) {
            message = getMessage(app, registeredTitle, registerActivitySubtitlePattern);
        }
        return message;
    }

    protected Message getStatusChangedMessage(Application app, StatusInfo from, StatusInfo to) {
        Message message = null;
        try {
            message = getMessage(app, statusChangedTitle, String.format(statusActivitySubtitlePattern,
                    app.getName(),
                    app.getId(),
                    from.getStatus(),
                    to.getStatus()));
        } catch( MissingFormatArgumentException e) {
            message = getMessage(app, statusChangedTitle, statusActivitySubtitlePattern);
        }
        return message;
    }

    protected Message getMessage(Application app, String registeredTitle, String activitySubtitle) {
        Message message = new Message();

        message.setTitle(registeredTitle);
        message.setSummary(messageSummary);
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

    public MicrosoftTeamsNotifier() {
        this.restTemplate = new RestTemplate();
    }

    public MicrosoftTeamsNotifier(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void setWebhookUrl(URI webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public URI getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(URI imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setThemeColor(String themeColor) {
        this.themeColor = themeColor;
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


    @JsonSerialize
    class Message {

        private String summary;
        private String themeColor;
        private String title;
        private List<Section> sections = new ArrayList<>();

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getTitle() {
            return title;
        }

        public String getThemeColor() {
            return themeColor;
        }

        public void setSections(List<Section> sections) {
            this.sections = sections;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setThemeColor(String themeColor) {
            this.themeColor = themeColor;
        }

        public List<Section> getSections() {
            return sections;
        }

    }

    @JsonSerialize
    class Section {

        private String activityTitle;
        private String activitySubtitle;
        private String activityImage;
        private List<Fact> facts = new ArrayList<Fact>();
        private String text;

        public String getActivityTitle() {
            return activityTitle;
        }

        public void setActivityTitle(String activityTitle) {
            this.activityTitle = activityTitle;
        }

        public String getActivitySubtitle() {
            return activitySubtitle;
        }

        public void setActivitySubtitle(String activitySubtitle) {
            this.activitySubtitle = activitySubtitle;
        }

        public void setFacts(List<Fact> facts) {
            this.facts = facts;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getActivityImage() {
            return activityImage;
        }

        public void setActivityImage(String activityImage) {
            this.activityImage = activityImage;
        }

        public List<Fact> getFacts() {
            return facts;
        }
    }

    @JsonSerialize
    class Fact {

        private String name;
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Fact(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }
}
