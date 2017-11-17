package de.codecentric.boot.admin.jackson;

import de.codecentric.boot.admin.model.Application;

import java.util.Collections;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class ApplicationJacksonTest {
    private final SimpleModule module = new SimpleModule()//
                                                          .addDeserializer(Application.class,
                                                                  new ApplicationDeserializer())
                                                          .setSerializerModifier(new ApplicationBeanSerializerModifier(
                                                                  new SanitizingMapSerializer(
                                                                          new String[]{".*password$"})));
    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().modules(module).build();

    @Test
    public void test_1_2_json_format() throws Exception {
        String json = new JSONObject().put("name", "test").put("url", "http://test").toString();
        Application value = objectMapper.readValue(json, Application.class);
        Assert.assertThat(value.getName(), is("test"));
        Assert.assertThat(value.getManagementUrl(), is("http://test"));
        Assert.assertThat(value.getHealthUrl(), is("http://test/health"));
        Assert.assertThat(value.getServiceUrl(), nullValue());
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
        Assert.assertThat(value.getName(), is("test"));
        Assert.assertThat(value.getManagementUrl(), is("http://test"));
        Assert.assertThat(value.getHealthUrl(), is("http://health"));
        Assert.assertThat(value.getServiceUrl(), is("http://service"));
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
        Assert.assertThat(value.getName(), is("test"));
        Assert.assertThat(value.getManagementUrl(), is("http://test"));
        Assert.assertThat(value.getHealthUrl(), is("http://health"));
        Assert.assertThat(value.getServiceUrl(), is("http://service"));
        Assert.assertThat(value.getMetadata(), is(Collections.singletonMap("labels", "foo,bar")));
    }

    @Test
    public void test_onlyHealthUrl() throws Exception {
        String json = new JSONObject().put("name", "test").put("healthUrl", "http://test").toString();
        Application value = objectMapper.readValue(json, Application.class);
        Assert.assertThat(value.getName(), is("test"));
        Assert.assertThat(value.getHealthUrl(), is("http://test"));
        Assert.assertThat(value.getManagementUrl(), nullValue());
        Assert.assertThat(value.getServiceUrl(), nullValue());
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
                                     .addMetadata("PASSWORD", "qwertz123")
                                     .addMetadata("user", "humptydumpty")
                                     .build();
        String json = objectMapper.writeValueAsString(app);
        Assert.assertThat(json, not(containsString("qwertz123")));
        Assert.assertThat(json, containsString("humptydumpty"));
    }


}