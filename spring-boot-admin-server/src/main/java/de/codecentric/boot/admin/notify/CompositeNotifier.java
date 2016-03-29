package de.codecentric.boot.admin.notify;

import de.codecentric.boot.admin.event.ClientApplicationEvent;

/**
 * A notifier delegating notifications to all specified notifiers.
 *
 * @author sebastian.meiser
 */
public class CompositeNotifier implements Notifier {
	private final Iterable<Notifier> notifiers;

	public CompositeNotifier(Iterable<Notifier> notifiers) {
		this.notifiers = notifiers;
	}

	@Override
	public void notify(ClientApplicationEvent event) {
		for (Notifier notifier : notifiers) {
			notifier.notify(event);
		}
	}
}
