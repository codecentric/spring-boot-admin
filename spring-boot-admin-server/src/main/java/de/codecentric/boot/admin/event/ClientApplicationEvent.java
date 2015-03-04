package de.codecentric.boot.admin.event;

import org.springframework.context.ApplicationEvent;

import de.codecentric.boot.admin.model.Application;

/**
 * Abstract Event regearding spring boot admin clients
 * @author Johannes Stelzer
 */
public abstract class ClientApplicationEvent extends ApplicationEvent {
	private static final long serialVersionUID = 1L;

	private final Application application;

	public ClientApplicationEvent(Object source, Application application) {
		super(source);
		this.application = application;
	}

	public Application getApplication() {
		return application;
	}
}
