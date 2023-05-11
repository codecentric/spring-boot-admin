/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.notify.filter.web;

import java.time.Instant;
import java.util.Collection;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.notify.filter.ApplicationNameNotificationFilter;
import de.codecentric.boot.admin.server.notify.filter.FilteringNotifier;
import de.codecentric.boot.admin.server.notify.filter.InstanceIdNotificationFilter;
import de.codecentric.boot.admin.server.notify.filter.NotificationFilter;
import de.codecentric.boot.admin.server.web.AdminController;

import static org.springframework.util.StringUtils.hasText;

/**
 * REST-Controller for managing notification filters
 *
 * @author Johannes Edmeier
 */
@AdminController
@ResponseBody
public class NotificationFilterController {

	private final FilteringNotifier filteringNotifier;

	public NotificationFilterController(FilteringNotifier filteringNotifier) {
		this.filteringNotifier = filteringNotifier;
	}

	@GetMapping(path = "/notifications/filters", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public Collection<NotificationFilter> getFilters() {
		return filteringNotifier.getNotificationFilters().values();
	}

	@PostMapping(path = "/notifications/filters", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> addFilter(@RequestParam(name = "instanceId", required = false) String instanceId,
			@RequestParam(name = "applicationName", required = false) String name,
			@RequestParam(name = "ttl", required = false) Long ttl) {
		if (hasText(instanceId) || hasText(name)) {
			NotificationFilter filter = createFilter(hasText(instanceId) ? InstanceId.of(instanceId) : null, name, ttl);
			filteringNotifier.addFilter(filter);
			return ResponseEntity.ok(filter);
		}
		else {
			return ResponseEntity.badRequest().body("Either 'instanceId' or 'applicationName' must be set");
		}
	}

	@DeleteMapping(path = "/notifications/filters/{id}")
	public ResponseEntity<Void> deleteFilter(@PathVariable("id") String id) {
		NotificationFilter deleted = filteringNotifier.removeFilter(id);
		if (deleted != null) {
			return ResponseEntity.ok().build();
		}
		else {
			return ResponseEntity.notFound().build();
		}
	}

	private NotificationFilter createFilter(@Nullable InstanceId id, String name, @Nullable Long ttl) {
		Instant expiry = ((ttl != null) && (ttl >= 0)) ? Instant.now().plusMillis(ttl) : null;
		if (id != null) {
			return new InstanceIdNotificationFilter(id, expiry);
		}
		else {
			return new ApplicationNameNotificationFilter(name, expiry);
		}
	}

}
