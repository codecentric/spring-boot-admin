package de.codecentric.boot.admin.notify;

import org.springframework.context.event.EventListener;

import de.codecentric.boot.admin.event.ClientApplicationEvent;

public class NotifierListener {
	private final Notifier notifier;

	public NotifierListener(Notifier notifier) {
		this.notifier = notifier;
	}

	@EventListener
	public void onClientApplicationEvent(ClientApplicationEvent event) {
		notifier.notify(event);
	}
}
