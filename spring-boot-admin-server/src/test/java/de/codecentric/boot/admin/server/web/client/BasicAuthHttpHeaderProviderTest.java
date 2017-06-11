package de.codecentric.boot.admin.server.web.client;

import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.ApplicationId;
import de.codecentric.boot.admin.server.model.Registration;

import org.junit.Test;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicAuthHttpHeaderProviderTest {
    private BasicAuthHttpHeaderProvider headersProvider = new BasicAuthHttpHeaderProvider();

    @Test
    public void test_auth_header() {
        Registration registration = Registration.create("foo", "http://health")
                                                .metadata("user.name", "test")
                                                .metadata("user.password", "drowssap")
                                                .build();
        Application application = Application.create(ApplicationId.of("id"), registration).build();
        assertThat(headersProvider.getHeaders(application).get(HttpHeaders.AUTHORIZATION)).containsOnly(
                "Basic dGVzdDpkcm93c3NhcA==");
    }

    @Test
    public void test_no_header() {
        Registration registration = Registration.create("foo", "http://health").build();
        Application application = Application.create(ApplicationId.of("id"), registration).build();
        assertThat(headersProvider.getHeaders(application)).isEmpty();
    }
}
