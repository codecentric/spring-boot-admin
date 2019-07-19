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

package de.codecentric.boot.admin.server.domain.entities;

import de.codecentric.boot.admin.server.domain.values.BuildVersion;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import static de.codecentric.boot.admin.server.domain.values.StatusInfo.STATUS_UNKNOWN;

@lombok.Data
public class Application {

    private final String name;
    @Nullable
    private BuildVersion buildVersion;
    private String status = STATUS_UNKNOWN;
    private Instant statusTimestamp = Instant.now();
    private List<Instance> instances = new ArrayList<>();

}
