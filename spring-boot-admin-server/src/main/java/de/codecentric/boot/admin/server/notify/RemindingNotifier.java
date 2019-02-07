/*
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.notify;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * Notifier that reminds certain statuses to send reminder notification using a delegate.
 *
 * @author Johannes Edmeier
 */
public class RemindingNotifier extends AbstractEventNotifier {
    private static final Logger log = LoggerFactory.getLogger(RemindingNotifier.class);
    private final ConcurrentHashMap<InstanceId, Reminder> reminders = new ConcurrentHashMap<>();
    private final Notifier delegate;
    private Duration checkReminderInverval = Duration.ofSeconds(10);
    private Duration reminderPeriod = Duration.ofMinutes(10);
    private String[] reminderStatuses = {"DOWN", "OFFLINE"};
    @Nullable
    private Disposable subscription;

    public RemindingNotifier(Notifier delegate, InstanceRepository repository) {
        super(repository);
        Assert.notNull(delegate, "'delegate' must not be null!");
        this.delegate = delegate;
    }

    @Override
    public Mono<Void> doNotify(InstanceEvent event, Instance instance) {
        return delegate.notify(event).onErrorResume(error -> Mono.empty()).then(Mono.fromRunnable(() -> {
            if (shouldEndReminder(event)) {
                reminders.remove(event.getInstance());
            } else if (shouldStartReminder(event)) {
                reminders.putIfAbsent(event.getInstance(), new Reminder(event));
            }
        }));
    }

    public void start() {
        Scheduler scheduler = Schedulers.newSingle("reminders");
        this.subscription = Flux.interval(this.checkReminderInverval, scheduler)
                                .log(log.getName(), Level.FINEST)
                                .doOnSubscribe(s -> log.debug("Started reminders"))
                                .flatMap(i -> this.sendReminders())
                                .onErrorContinue((ex, value) -> log.warn(
                                    "Unexpected error while sending reminders",
                                    ex
                                )).doFinally(s -> scheduler.dispose())
                                .subscribe();
    }

    public void stop() {
        if (this.subscription != null && !this.subscription.isDisposed()) {
            log.debug("stopped reminders");
            this.subscription.dispose();
        }
    }

    protected Mono<Void> sendReminders() {
        Instant now = Instant.now();

        return Flux.fromIterable(this.reminders.values())
                   .filter(reminder -> reminder.getLastNotification().plus(reminderPeriod).isBefore(now))
                   .flatMap(reminder -> delegate.notify(reminder.getEvent())
                                                .doOnSuccess(signal -> reminder.setLastNotification(now)))
                   .then();
    }

    protected boolean shouldStartReminder(InstanceEvent event) {
        if (event instanceof InstanceStatusChangedEvent) {
            return Arrays.binarySearch(reminderStatuses,
                ((InstanceStatusChangedEvent) event).getStatusInfo().getStatus()) >= 0;
        }
        return false;
    }

    protected boolean shouldEndReminder(InstanceEvent event) {
        if (event instanceof InstanceDeregisteredEvent) {
            return true;
        }
        if (event instanceof InstanceStatusChangedEvent) {
            return Arrays.binarySearch(reminderStatuses,
                ((InstanceStatusChangedEvent) event).getStatusInfo().getStatus()) < 0;
        }
        return false;
    }

    public void setReminderPeriod(Duration reminderPeriod) {
        this.reminderPeriod = reminderPeriod;
    }

    public void setReminderStatuses(String[] reminderStatuses) {
        String[] copy = Arrays.copyOf(reminderStatuses, reminderStatuses.length);
        Arrays.sort(copy);
        this.reminderStatuses = copy;
    }

    public void setCheckReminderInverval(Duration checkReminderInverval) {
        this.checkReminderInverval = checkReminderInverval;
    }

    private static class Reminder {
        private final InstanceEvent event;
        private Instant lastNotification;

        private Reminder(InstanceEvent event) {
            this.event = event;
            this.lastNotification = event.getTimestamp();
        }

        public void setLastNotification(Instant lastNotification) {
            this.lastNotification = lastNotification;
        }

        public Instant getLastNotification() {
            return lastNotification;
        }

        public InstanceEvent getEvent() {
            return event;
        }
    }
}
