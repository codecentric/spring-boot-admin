package de.codecentric.boot.admin.server.model;

import org.json.JSONObject;
import org.junit.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationTest {
    private ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    @Test
    public void test_1_2_json_format() throws Exception {
        String json = new JSONObject().put("name", "test").put("url", "http://test").toString();
        Application value = objectMapper.readValue(json, Application.class);
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
        Application value = objectMapper.readValue(json, Application.class);
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
        Application value = objectMapper.readValue(json, Application.class);
        assertThat(value.getName()).isEqualTo("test");
        assertThat(value.getManagementUrl()).isEqualTo("http://test");
        assertThat(value.getHealthUrl()).isEqualTo("http://health");
        assertThat(value.getServiceUrl()).isEqualTo("http://service");
        assertThat(value.getMetadata()).isEqualTo(singletonMap("labels", "foo,bar"));
    }

    @Test
    public void test_onlyHealthUrl() throws Exception {
        String json = new JSONObject().put("name", "test").put("healthUrl", "http://test").toString();
        Application value = objectMapper.readValue(json, Application.class);
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
        objectMapper.readValue(json, Application.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_healthUrl_expected() throws Exception {
        String json = new JSONObject().put("name", "test")
                                      .put("managementUrl", "http://test")
                                      .put("healthUrl", "")
                                      .put("serviceUrl", "http://service")
                                      .toString();
        objectMapper.readValue(json, Application.class);
    }

    @Test
    public void test_sanitize_metadata() throws JsonProcessingException {
        Application app = Application.create("test")
                                     .withHealthUrl("http://health")
                                     .addMetadata("password", "qwertz123")
                                     .addMetadata("user", "humptydumpty")
                                     .build();
        String json = objectMapper.writeValueAsString(app);
        assertThat(json).doesNotContain("qwertz123").contains("humptydumpty");
    }

    @Test
    public void test_equals_hashCode() {
        Application a1 = Application.create("foo")
                                    .withHealthUrl("healthUrl")
                                    .withManagementUrl("mgmt")
                                    .withServiceUrl("svc").withId(ApplicationId.of("id"))
                                    .build();
        Application a2 = Application.create("foo")
                                    .withHealthUrl("healthUrl")
                                    .withManagementUrl("mgmt")
                                    .withServiceUrl("svc").withId(ApplicationId.of("id"))
                                    .build();

        assertThat(a1).isEqualTo(a2);
        assertThat(a1.hashCode()).isEqualTo(a2.hashCode());

        Application a3 = Application.create("foo")
                                    .withHealthUrl("healthUrl2")
                                    .withManagementUrl("mgmt")
                                    .withServiceUrl("svc").withId(ApplicationId.of("other"))
                                    .build();

        assertThat(a1).isNotEqualTo(a3);
        assertThat(a2).isNotEqualTo(a3);
    }

    @Test
    public void test_builder_copy() {
        Application app = Application.create("App").withId(ApplicationId.of("id"))
                                     .withHealthUrl("http://health")
                                     .withManagementUrl("http://mgmgt")
                                     .withServiceUrl("http://svc")
                                     .withStatusInfo(StatusInfo.ofUp())
                                     .build();
        Application copy = Application.copyOf(app).build();
        assertThat(app).isEqualTo(copy);
    }
}