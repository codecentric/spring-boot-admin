package de.codecentric.boot.admin.server.model;

import org.json.JSONObject;
import org.junit.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RegistrationTest {
    private ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

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

    @Test
    public void test_name_expected() throws Exception {
        String json = new JSONObject().put("name", "")
                                      .put("managementUrl", "http://test")
                                      .put("healthUrl", "http://health")
                                      .put("serviceUrl", "http://service")
                                      .toString();
        assertThatThrownBy(() -> objectMapper.readValue(json, Registration.class)).isInstanceOf(
                IllegalArgumentException.class).hasMessageContaining("Name");
    }

    @Test
    public void test_healthUrl_expected() throws Exception {
        String json = new JSONObject().put("name", "test")
                                      .put("managementUrl", "http://test")
                                      .put("healthUrl", "")
                                      .put("serviceUrl", "http://service")
                                      .toString();
        assertThatThrownBy(() -> objectMapper.readValue(json, Registration.class)).isInstanceOf(
                IllegalArgumentException.class).hasMessageContaining("Health-URL");
    }

    @Test
    public void test_valid_Urls_expected() throws Exception {
        String json = new JSONObject().put("name", "test")
                                      .put("managementUrl", "http://test")
                                      .put("healthUrl", "http://health")
                                      .put("serviceUrl", "http://service")
                                      .toString();

        String invalidHealth = new JSONObject(json).put("healthUrl", "invalid").toString();
        assertThatThrownBy(() -> objectMapper.readValue(invalidHealth, Registration.class)).isInstanceOf(
                IllegalArgumentException.class).hasMessageContaining("Health-URL");

        String invalidMgmt = new JSONObject(json).put("managementUrl", "invalid").toString();
        assertThatThrownBy(() -> objectMapper.readValue(invalidMgmt, Registration.class)).isInstanceOf(
                IllegalArgumentException.class).hasMessageContaining("Management-URL");

        String invalidService = new JSONObject(json).put("serviceUrl", "invalid").toString();
        assertThatThrownBy(() -> objectMapper.readValue(invalidService, Registration.class)).isInstanceOf(
                IllegalArgumentException.class).hasMessageContaining("Service-URL");
    }


    @Test
    public void test_sanitize_metadata() throws JsonProcessingException {
        Registration registration = Registration.builder()
                                                .name("test")
                                                .healthUrl("http://health")
                                                .metadata("password", "qwertz123")
                                                .metadata("user", "humptydumpty")
                                                .build();
        String json = objectMapper.writeValueAsString(registration);
        assertThat(json).doesNotContain("qwertz123").contains("humptydumpty");
    }
}