package de.codecentric.boot.admin.notify;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;

public abstract class AbstractNotifier {

	/**
	 * List of changes to ignore. Must be in Format OLD:NEW, for any status use * as wildcard, e.g.
	 * *:UP or OFFLINE:*
	 */
	protected String[] ignoreChanges = { "UNKNOWN:UP" };

	/**
	 * Enables the mail notification.
	 */
	private boolean enabled = true;

	@EventListener
	public void onClientApplicationStatusChanged(ClientApplicationStatusChangedEvent event) {
		if (enabled && shouldNotify(event.getFrom().getStatus(), event.getTo().getStatus())) {
			try {
				notify(event);
			} catch (Exception ex) {
				getLogger().error("Couldn't notify for status change {} ", event, ex);
			}
		}
	}

	protected boolean shouldNotify(String from, String to) {
		return Arrays.binarySearch(ignoreChanges, (from + ":" + to)) < 0
				&& Arrays.binarySearch(ignoreChanges, ("*:" + to)) < 0
				&& Arrays.binarySearch(ignoreChanges, (from + ":*")) < 0;
	}

	protected abstract void notify(ClientApplicationStatusChangedEvent event) throws Exception;

	private Logger getLogger() {
		return LoggerFactory.getLogger(this.getClass());
	}

	public void setIgnoreChanges(String[] ignoreChanges) {
		String[] copy = Arrays.copyOf(ignoreChanges, ignoreChanges.length);
		Arrays.sort(copy);
		this.ignoreChanges = copy;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}