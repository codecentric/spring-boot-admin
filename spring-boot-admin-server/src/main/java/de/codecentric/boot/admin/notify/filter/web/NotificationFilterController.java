/*
 * Copyright 2014 the original author or authors.
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
package de.codecentric.boot.admin.notify.filter.web;

import static org.springframework.util.StringUtils.hasText;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.codecentric.boot.admin.notify.filter.ApplicationIdNotificationFilter;
import de.codecentric.boot.admin.notify.filter.ApplicationNameNotificationFilter;
import de.codecentric.boot.admin.notify.filter.FilteringNotifier;
import de.codecentric.boot.admin.notify.filter.NotificationFilter;

/**
 * REST-Controller for managing notification filters
 *
 * @author Johannes Edmeier
 */
@ResponseBody
public class NotificationFilterController {
	private FilteringNotifier filteringNotifier;

	public NotificationFilterController(FilteringNotifier filteringNotifier) {
		this.filteringNotifier = filteringNotifier;
	}

	@RequestMapping(path = "/api/notifications/filters", method = {
			RequestMethod.GET }, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public Map<String, NotificationFilter> getFilters() {
		return filteringNotifier.getNotificationFilters();
	}

	@RequestMapping(path = "/api/notifications/filters", method = {
			RequestMethod.POST }, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> addFilter(@RequestParam(name = "id", required = false) String id,
			@RequestParam(name = "name", required = false) String name,
			@RequestParam(name = "ttl", required = false, defaultValue = "-1") long ttl) {
		if (hasText(id) || hasText(name)) {
			return ResponseEntity.ok(filteringNotifier.addFilter(createFilter(id, name, ttl)));
		} else {
			return ResponseEntity.badRequest().body("Either 'id' or 'name' must be set");
		}
	}

	@RequestMapping(path = "/api/notifications/filters/{id}", method = { RequestMethod.DELETE })
	public ResponseEntity<?> deleteFilter(@PathVariable("id") String id) {
		NotificationFilter deleted = filteringNotifier.removeFilter(id);
		if (deleted != null) {
			return ResponseEntity.ok(deleted);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	private NotificationFilter createFilter(String id, String name, long ttl) {
		long expiry = ttl > 0L ? System.currentTimeMillis() + ttl : ttl;

		NotificationFilter filter = hasText(id) ? new ApplicationIdNotificationFilter(id, expiry)
				: new ApplicationNameNotificationFilter(name, expiry);
		return filter;
	}
}
