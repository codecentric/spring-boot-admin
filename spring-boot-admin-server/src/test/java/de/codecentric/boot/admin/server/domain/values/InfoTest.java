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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class InfoTest {
    private ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    @Test
    public void should_serialize_json() throws Exception {
        Map<String, Object> values = new HashMap<>();
        values.put("foo", "bar");
        values.put("build", singletonMap("version", "1.0.0"));
        Info info = Info.from(values);
        String json = objectMapper.writeValueAsString(info);

        DocumentContext doc = JsonPath.parse(json);

        assertThat(doc.read("$.foo", String.class)).isEqualTo("bar");
        assertThat(doc.read("$.build.version", String.class)).isEqualTo("1.0.0");
    }

    @Test
    public void should_keep_order() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("z", "1");
        map.put("x", "2");

        Iterator<?> iter = Info.from(map).getValues().entrySet().iterator();

        assertThat(iter.next()).hasFieldOrPropertyWithValue("key", "z").hasFieldOrPropertyWithValue("value", "1");
        assertThat(iter.next()).hasFieldOrPropertyWithValue("key", "x").hasFieldOrPropertyWithValue("value", "2");
    }

}
