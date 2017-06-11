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
package de.codecentric.boot.admin.server.model;

import java.io.Serializable;
import org.springframework.util.Assert;

/**
 * The domain model for all registered application at the spring boot admin application.
 */
@lombok.Data
public class Application implements Serializable {
    private final ApplicationId id;
    private final Registration registration;
    private final StatusInfo statusInfo;
    private final Info info;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    private Application(ApplicationId id, Registration registration, StatusInfo statusInfo, Info info) {
        Assert.notNull(id, "'id' must not be null");
        Assert.notNull(registration, "'registration' must not be null");
        this.id = id;
        this.registration = registration;
        this.statusInfo = statusInfo != null ? statusInfo : StatusInfo.ofUnknown();
        this.info = info != null ? info : Info.empty();
    }

    public static Application.Builder create(ApplicationId id, Registration registration) {
        return builder().id(id).registration(registration);
    }

    public static Application.Builder copyOf(Application application) {
        return application.toBuilder();
    }
}
