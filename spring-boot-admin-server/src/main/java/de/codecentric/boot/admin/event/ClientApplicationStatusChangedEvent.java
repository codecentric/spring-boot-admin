package de.codecentric.boot.admin.event;

import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;

public class ClientApplicationStatusChangedEvent extends ClientApplicationEvent {
	private static final long serialVersionUID = 1L;
	private final StatusInfo from;
	private final StatusInfo to;

	public ClientApplicationStatusChangedEvent(Object source,
			Application application, StatusInfo from, StatusInfo to) {
		super(source, application);
		this.from = from;
		this.to = to;
	}

	public StatusInfo getFrom() {
		return from;
	}

	public StatusInfo getTo() {
		return to;
	}

}
