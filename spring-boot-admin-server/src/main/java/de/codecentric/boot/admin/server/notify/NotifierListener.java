package de.codecentric.boot.admin.server.notify;

import de.codecentric.boot.admin.server.event.ClientApplicationEvent;

import org.springframework.context.event.EventListener;

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
