package de.codecentric.boot.admin.server.web.client;

import de.codecentric.boot.admin.server.model.Application;

import org.junit.Test;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicAuthHttpHeaderProviderTest {
    private BasicAuthHttpHeaderProvider headersProvider = new BasicAuthHttpHeaderProvider();

    @Test
    public void test_auth_header() {
        Application app = Application.create("test")
                                     .withHealthUrl("/health")
                                     .addMetadata("user.name", "test")
                                     .addMetadata("user.password", "drowssap")
                                     .build();
        assertThat(headersProvider.getHeaders(app).get(HttpHeaders.AUTHORIZATION)).containsOnly(
                "Basic dGVzdDpkcm93c3NhcA==");
    }

    @Test
    public void test_no_header() {
        Application app = Application.create("test").withHealthUrl("/health").build();
        assertThat(headersProvider.getHeaders(app)).isEmpty();
    }
}
