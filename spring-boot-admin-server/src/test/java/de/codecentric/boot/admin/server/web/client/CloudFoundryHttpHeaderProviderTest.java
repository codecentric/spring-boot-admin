package de.codecentric.boot.admin.server.web.client;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;

public class CloudFoundryHttpHeaderProviderTest {
    private CloudFoundryHttpHeaderProvider headersProvider = new CloudFoundryHttpHeaderProvider();

    @Test
    public void test_auth_header() {
        Registration registration = Registration.create("foo", "http://health")
            .metadata("user.name", "test")
            .metadata("user.password", "drowssap")
            .build();
        Instance instance = Instance.create(InstanceId.of("id")).register(registration);
        assertThat(headersProvider.getHeaders(instance).get(HttpHeaders.AUTHORIZATION)).containsOnly(
            "Basic dGVzdDpkcm93c3NhcA==");
    }

    @Test
    public void test_cloud_foundry_header() {
        Registration registration = Registration.create("foo", "http://health")
            .metadata("cf_application_guid", "549e64cf-a478-423d-9d6d-02d803a028a8")
            .metadata("cf_instance_index", "0")
            .build();
        Instance instance = Instance.create(InstanceId.of("id")).register(registration);
        assertThat(headersProvider.getHeaders(instance).get("X-CF-APP-INSTANCE")).containsOnly(
            "549e64cf-a478-423d-9d6d-02d803a028a8:0");
    }

    @Test
    public void test_no_header() {
        Registration registration = Registration.create("foo", "http://health").build();
        Instance instance = Instance.create(InstanceId.of("id")).register(registration);
        assertThat(headersProvider.getHeaders(instance)).isEmpty();
    }
}
