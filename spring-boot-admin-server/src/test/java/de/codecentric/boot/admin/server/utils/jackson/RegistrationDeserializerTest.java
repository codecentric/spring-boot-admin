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

package de.codecentric.boot.admin.server.utils.jackson;

import de.codecentric.boot.admin.server.domain.values.Registration;

import org.json.JSONObject;
import org.junit.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class RegistrationDeserializerTest {
    private final ObjectMapper objectMapper;

    public RegistrationDeserializerTest() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Registration.class, new RegistrationDeserializer());
        module.setSerializerModifier(
            new RegistrationBeanSerializerModifier(new SanitizingMapSerializer(new String[]{".*password$"})));
        objectMapper = Jackson2ObjectMapperBuilder.json().modules(module).build();
    }

    @Test
    public void test_1_2_json_format() throws Exception {
        String json = new JSONObject().put("name", "test").put("url", "http://test").toString();
        Registration value = objectMapper.readValue(json, Registration.class);
        assertThat(value.getName()).isEqualTo("test");
        assertThat(value.getManagementUrl()).isEqualTo("http://test");
        assertThat(value.getHealthUrl()).isEqualTo("http://test/health");
        assertThat(value.getServiceUrl()).isNull();
    }

    @Test
    public void test_1_4_json_format() throws Exception {
        String json = new JSONObject().put("name", "test")
                                      .put("managementUrl", "http://test")
                                      .put("healthUrl", "http://health")
                                      .put("serviceUrl", "http://service")
                                      .put("statusInfo", new JSONObject().put("status", "UNKNOWN"))
                                      .toString();
        Registration value = objectMapper.readValue(json, Registration.class);
        assertThat(value.getName()).isEqualTo("test");
        assertThat(value.getManagementUrl()).isEqualTo("http://test");
        assertThat(value.getHealthUrl()).isEqualTo("http://health");
        assertThat(value.getServiceUrl()).isEqualTo("http://service");
    }

    @Test
    public void test_1_5_json_format() throws Exception {
        String json = new JSONObject().put("name", "test")
                                      .put("managementUrl", "http://test")
                                      .put("healthUrl", "http://health")
                                      .put("serviceUrl", "http://service")
                                      .put("metadata", new JSONObject().put("labels", "foo,bar"))
                                      .toString();
        Registration value = objectMapper.readValue(json, Registration.class);
        assertThat(value.getName()).isEqualTo("test");
        assertThat(value.getManagementUrl()).isEqualTo("http://test");
        assertThat(value.getHealthUrl()).isEqualTo("http://health");
        assertThat(value.getServiceUrl()).isEqualTo("http://service");
        assertThat(value.getMetadata()).isEqualTo(singletonMap("labels", "foo,bar"));
    }

    @Test
    public void test_onlyHealthUrl() throws Exception {
        String json = new JSONObject().put("name", "test").put("healthUrl", "http://test").toString();
        Registration value = objectMapper.readValue(json, Registration.class);
        assertThat(value.getName()).isEqualTo("test");
        assertThat(value.getHealthUrl()).isEqualTo("http://test");
        assertThat(value.getManagementUrl()).isNull();
        assertThat(value.getServiceUrl()).isNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_name_expected() throws Exception {
        String json = new JSONObject().put("name", "")
                                      .put("managementUrl", "http://test")
                                      .put("healthUrl", "http://health")
                                      .put("serviceUrl", "http://service")
                                      .toString();
        objectMapper.readValue(json, Registration.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_healthUrl_expected() throws Exception {
        String json = new JSONObject().put("name", "test")
                                      .put("managementUrl", "http://test")
                                      .put("healthUrl", "")
                                      .put("serviceUrl", "http://service")
                                      .toString();
        objectMapper.readValue(json, Registration.class);
    }

    @Test
    public void test_sanitize_metadata() throws JsonProcessingException {
        Registration app = Registration.create("test", "http://health")
                                       .metadata("PASSWORD", "qwertz123")
                                       .metadata("user", "humptydumpty")
                                       .build();
        String json = objectMapper.writeValueAsString(app);

        assertThat(json).doesNotContain("qwertz123");
        assertThat(json).contains("humptydumpty");
    }
}
