package de.codecentric.boot.admin.notify;

import java.util.ArrayList;
import java.util.List;

import de.codecentric.boot.admin.event.ClientApplicationEvent;

public class TestNotifier implements Notifier {
	private List<ClientApplicationEvent> events = new ArrayList<ClientApplicationEvent>();

	@Override
	public void notify(ClientApplicationEvent event) {
		this.events.add(event);
	}

	public List<ClientApplicationEvent> getEvents() {
		return events;
	}
}