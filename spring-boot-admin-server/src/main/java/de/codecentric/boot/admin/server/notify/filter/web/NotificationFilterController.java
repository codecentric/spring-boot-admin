/*
 * Copyright 2014-2017 the original author or authors.
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

import java.util.Collections;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
@RequestMapping("/notifications/filters")
public class NotificationFilterController {
    private FilteringNotifier filteringNotifier;

    public NotificationFilterController(FilteringNotifier filteringNotifier) {
        this.filteringNotifier = filteringNotifier;
    }

    @RequestMapping(method = {RequestMethod.GET}, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Map<String, NotificationFilter> getFilters() {
        return filteringNotifier.getNotificationFilters();
    }

    @RequestMapping(method = {RequestMethod.POST}, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addFilter(@RequestParam(name = "id", required = false) String id,
                                       @RequestParam(name = "name", required = false) String name,
                                       @RequestParam(name = "ttl", required = false, defaultValue = "-1") long ttl) {
        if (hasText(id) || hasText(name)) {
            NotificationFilter filter = createFilter(hasText(id) ? InstanceId.of(id) : null, name, ttl);
            String filterId = filteringNotifier.addFilter(filter);
            return ResponseEntity.ok(Collections.singletonMap(filterId, filter));
        } else {
            return ResponseEntity.badRequest().body("Either 'id' or 'name' must be set");
        }
    }

    @RequestMapping(path = "/{id}", method = {RequestMethod.DELETE})
    public ResponseEntity<?> deleteFilter(@PathVariable("id") String id) {
        NotificationFilter deleted = filteringNotifier.removeFilter(id);
        if (deleted != null) {
            return ResponseEntity.ok(deleted);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private NotificationFilter createFilter(InstanceId id, String name, long ttl) {
        long expiry = ttl > 0L ? System.currentTimeMillis() + ttl : ttl;

        return id != null ?
                new InstanceIdNotificationFilter(id, expiry) :
                new ApplicationNameNotificationFilter(name, expiry);
    }
}
