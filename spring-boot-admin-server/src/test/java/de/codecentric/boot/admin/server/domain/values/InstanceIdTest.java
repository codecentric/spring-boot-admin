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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class InstanceIdTest {
    @Test
    public void invariants() {
        assertThatThrownBy(() -> InstanceId.of(null)).isInstanceOf(IllegalArgumentException.class)
                                                     .hasMessage("'value' must have text");
        assertThatThrownBy(() -> InstanceId.of("")).isInstanceOf(IllegalArgumentException.class)
                                                   .hasMessage("'value' must have text");
    }

    @Test
    public void json() throws JsonProcessingException {
        assertThat(new ObjectMapper().writeValueAsString(InstanceId.of("abc"))).isEqualTo("\"abc\"");
    }
}
