/*
 * Copyright 2014-2018 the original author or authors.
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

package de.codecentric.boot.admin.server.notify.filter.web;

import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.notify.filter.ApplicationNameNotificationFilter;
import de.codecentric.boot.admin.server.notify.filter.FilteringNotifier;
import de.codecentric.boot.admin.server.notify.filter.InstanceIdNotificationFilter;
import de.codecentric.boot.admin.server.notify.filter.NotificationFilter;
import de.codecentric.boot.admin.server.web.AdminController;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.util.StringUtils.hasText;

/**
 * REST-Controller for managing notification filters
 *
 * @author Johannes Edmeier
 */
@AdminController
@ResponseBody
public class NotificationFilterController {
    private FilteringNotifier filteringNotifier;

    public NotificationFilterController(FilteringNotifier filteringNotifier) {
        this.filteringNotifier = filteringNotifier;
    }

    @GetMapping(path = "/notifications/filters", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Map<String, NotificationFilter> getFilters() {
        return filteringNotifier.getNotificationFilters();
    }

    @PostMapping(path = "/notifications/filters", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addFilter(@RequestParam(name = "id", required = false) String id,
                                       @RequestParam(name = "name", required = false) String name,
                                       @RequestParam(name = "ttl", required = false) Duration ttl) {
        if (hasText(id) || hasText(name)) {
            NotificationFilter filter = createFilter(hasText(id) ? InstanceId.of(id) : null, name, ttl);
            String filterId = filteringNotifier.addFilter(filter);
            return ResponseEntity.ok(Collections.singletonMap(filterId, filter));
        } else {
            return ResponseEntity.badRequest().body("Either 'id' or 'name' must be set");
        }
    }

    @DeleteMapping(path = "/notifications/filters/{id}")
    public ResponseEntity<?> deleteFilter(@PathVariable("id") String id) {
        NotificationFilter deleted = filteringNotifier.removeFilter(id);
        if (deleted != null) {
            return ResponseEntity.ok(deleted);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private NotificationFilter createFilter(InstanceId id, String name, Duration ttl) {
        Instant expiry = ttl != null ? Instant.now().plus(ttl) : null;

        return id != null ?
            new InstanceIdNotificationFilter(id, expiry) :
            new ApplicationNameNotificationFilter(name, expiry);
    }
}
