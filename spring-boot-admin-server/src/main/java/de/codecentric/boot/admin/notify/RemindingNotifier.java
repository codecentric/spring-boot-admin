/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin.notify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import de.codecentric.boot.admin.event.ClientApplicationDeregisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationEvent;
import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;

/**
 * Notifier that reminds certain statuses to send reminder notification using a delegate.
 *
 * @author Johannes Edmeier
 */
public class RemindingNotifier implements Notifier {
	private final ConcurrentHashMap<String, Reminder> reminders = new ConcurrentHashMap<>();
	private long reminderPeriod = TimeUnit.MINUTES.toMillis(10L);
	private String[] reminderStatuses = { "DOWN", "OFFLINE" };
	private final Notifier delegate;

	public RemindingNotifier(Notifier delegate) {
		this.delegate = delegate;
	}

	@Override
	public void notify(ClientApplicationEvent event) {
		delegate.notify(event);
		if (shouldEndReminder(event)) {
			reminders.remove(event.getApplication().getId());
		} else if (shouldStartReminder(event)) {
			reminders.putIfAbsent(event.getApplication().getId(), new Reminder(event));
		}
	}

	public void sendReminders() {
		long now = System.currentTimeMillis();
		for (Reminder reminder : new ArrayList<>(reminders.values())) {
			if (now - reminder.getLastNotification() > reminderPeriod) {
				reminder.setLastNotification(now);
				delegate.notify(reminder.getEvent());
			}
		}
	}

	protected boolean shouldStartReminder(ClientApplicationEvent event) {
		if (event instanceof ClientApplicationStatusChangedEvent) {
			return Arrays.binarySearch(reminderStatuses,
					event.getApplication().getStatusInfo().getStatus()) >= 0;
		}
		return false;
	}

	protected boolean shouldEndReminder(ClientApplicationEvent event) {
		if (event instanceof ClientApplicationDeregisteredEvent) {
			return true;
		}
		if (event instanceof ClientApplicationStatusChangedEvent) {
			return Arrays.binarySearch(reminderStatuses,
					event.getApplication().getStatusInfo().getStatus()) < 0;
		}
		return false;
	}

	public void setReminderPeriod(long reminderPeriod) {
		this.reminderPeriod = reminderPeriod;
	}

	public void setReminderStatuses(String[] reminderStatuses) {
		String[] copy = Arrays.copyOf(reminderStatuses, reminderStatuses.length);
		Arrays.sort(copy);
		this.reminderStatuses = copy;
	}

	private static class Reminder {
		private final ClientApplicationEvent event;
		private long lastNotification;

		private Reminder(ClientApplicationEvent event) {
			this.event = event;
			this.lastNotification = event.getTimestamp();
		}

		public void setLastNotification(long lastNotification) {
			this.lastNotification = lastNotification;
		}

		public long getLastNotification() {
			return lastNotification;
		}

		public ClientApplicationEvent getEvent() {
			return event;
		}
	}
}
