package de.codecentric.boot.admin.notify;

import static java.util.Collections.singletonList;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingFormatArgumentException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.event.ClientApplicationDeregisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationEvent;
import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;

public class MicrosoftTeamsNotifier extends AbstractEventNotifier {
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
	private String registerActivitySubtitlePattern = "%s with id %s has registered from Spring Boot Admin";

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

	@Override
	protected void doNotify(ClientApplicationEvent event) throws Exception {
		Message message;
		if (event instanceof ClientApplicationRegisteredEvent) {
			message = getRegisteredMessage(event.getApplication());
		} else if (event instanceof ClientApplicationDeregisteredEvent) {
			message = getDeregisteredMessage(event.getApplication());
		} else if (event instanceof ClientApplicationStatusChangedEvent) {
			ClientApplicationStatusChangedEvent statusChangedEvent = (ClientApplicationStatusChangedEvent) event;
			message = getStatusChangedMessage(statusChangedEvent.getApplication(),
					statusChangedEvent.getFrom(), statusChangedEvent.getTo());
		} else {
			return;
		}

		this.restTemplate.postForEntity(webhookUrl, message, Void.class);
	}

	@Override
	protected boolean shouldNotify(ClientApplicationEvent event) {
		return event instanceof ClientApplicationRegisteredEvent
				|| event instanceof ClientApplicationDeregisteredEvent
				|| event instanceof ClientApplicationStatusChangedEvent;
	}

	protected Message getDeregisteredMessage(Application app) {
		String activitySubtitle = this.safeFormat(deregisterActivitySubtitlePattern, app.getName(),
				app.getId());
		return createMessage(app, deRegisteredTitle, activitySubtitle);
	}

	protected Message getRegisteredMessage(Application app) {
		String activitySubtitle = this.safeFormat(registerActivitySubtitlePattern, app.getName(),
				app.getId());
		return createMessage(app, registeredTitle, activitySubtitle);
	}

	protected Message getStatusChangedMessage(Application app, StatusInfo from, StatusInfo to) {
		String activitySubtitle = this.safeFormat(statusActivitySubtitlePattern, app.getName(),
				app.getId(), from.getStatus(), to.getStatus());
		return createMessage(app, statusChangedTitle, activitySubtitle);
	}

	private String safeFormat(String format, Object... args) {
		try {
			return String.format(format, args);
		} catch (MissingFormatArgumentException e) {
			LOGGER.warn(
					"Exception while trying to format the message. Falling back by using the format string.",
					e);
			return format;
		}
	}

	protected Message createMessage(Application app, String registeredTitle,
			String activitySubtitle) {
		Message message = new Message();
		message.setTitle(registeredTitle);
		message.setSummary(messageSummary);
		message.setThemeColor(themeColor);

		Section section = new Section();
		section.setActivityTitle(app.getName());
		section.setActivitySubtitle(activitySubtitle);

		List<Fact> facts = new ArrayList<>();
		facts.add(new Fact(STATUS_KEY, app.getStatusInfo().getStatus()));
		facts.add(new Fact(SERVICE_URL_KEY, app.getServiceUrl()));
		facts.add(new Fact(HEALTH_URL_KEY, app.getHealthUrl()));
		facts.add(new Fact(MANAGEMENT_URL_KEY, app.getManagementUrl()));
		facts.add(new Fact(SOURCE_KEY, app.getSource()));
		section.setFacts(facts);
		message.setSections(singletonList(section));
		return message;
	}

	public void setWebhookUrl(URI webhookUrl) {
		this.webhookUrl = webhookUrl;
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

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public static class Message {
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

	public static class Section {
		private String activityTitle;
		private String activitySubtitle;
		private List<Fact> facts = new ArrayList<>();

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

		public List<Fact> getFacts() {
			return facts;
		}
	}

	public static class Fact {
		private final String name;
		private final String value;

		public Fact(String name, String value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}
	}
}
