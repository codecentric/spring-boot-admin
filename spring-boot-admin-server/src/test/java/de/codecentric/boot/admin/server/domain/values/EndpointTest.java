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

package de.codecentric.boot.admin.server.domain.values;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class EndpointTest {

    @Test
    public void invariants() {
        assertThatThrownBy(() -> Endpoint.of("", "")).isInstanceOf(IllegalArgumentException.class)
                                                     .hasMessage("'id' must not be empty.");
        assertThatThrownBy(() -> Endpoint.of("id", "")).isInstanceOf(IllegalArgumentException.class)
                                                       .hasMessage("'url' must not be empty.");
    }
}
