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
package de.codecentric.boot.admin.server.notify.filter;

import de.codecentric.boot.admin.server.event.ClientApplicationEvent;
import de.codecentric.boot.admin.server.model.Application;

public abstract class ExpiringNotificationFilter implements NotificationFilter {
    private final long expiry;

    public ExpiringNotificationFilter(long expiry) {
        this.expiry = expiry;
    }

    public boolean isExpired() {
        return expiry >= 0 && expiry < System.currentTimeMillis();
    }

    @Override
    public boolean filter(ClientApplicationEvent event, Application application) {
        return !isExpired() && doFilter(event, application);
    }

    protected abstract boolean doFilter(ClientApplicationEvent event, Application application);

    public long getExpiry() {
        return expiry;
    }
}