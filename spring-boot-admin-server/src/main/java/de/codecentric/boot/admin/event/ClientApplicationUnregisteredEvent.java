package de.codecentric.boot.admin.event;

import de.codecentric.boot.admin.model.Application;

public class ClientApplicationUnregisteredEvent extends ClientApplicationEvent {
	public ClientApplicationUnregisteredEvent(Object source, Application application) {
		super(source, application);
	}

	private static final long serialVersionUID = 1L;

}
