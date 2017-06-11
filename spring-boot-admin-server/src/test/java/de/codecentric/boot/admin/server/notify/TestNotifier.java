package de.codecentric.boot.admin.server.notify;

import de.codecentric.boot.admin.server.event.ClientApplicationEvent;

import java.util.ArrayList;
import java.util.List;

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