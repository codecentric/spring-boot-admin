package de.codecentric.boot.admin.server.web.client;

import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.ApplicationId;
import de.codecentric.boot.admin.server.model.Registration;

import java.io.Serializable;
import java.net.URI;
import java.util.Map;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApplicationOperationsTest {

    private RestTemplate restTemplate = mock(RestTemplate.class);
    private HttpHeadersProvider headersProvider = mock(HttpHeadersProvider.class);
    private ApplicationOperations ops = new ApplicationOperations(restTemplate, headersProvider);
    private final Application application = Application.create(ApplicationId.of("id"),
            Registration.create("test", "http://health").managementUrl("http://mgmt").build()).build();

    @Test
    public void test_getInfo() {

        ArgumentCaptor<HttpEntity> requestEntity = ArgumentCaptor.forClass(HttpEntity.class);

        HttpHeaders headers = new HttpHeaders();
        headers.add("auth", "foo:bar");
        when(headersProvider.getHeaders(eq(application))).thenReturn(headers);

        when(restTemplate.exchange(eq(URI.create("http://mgmt/info")), eq(HttpMethod.GET), requestEntity.capture(),
                eq(Map.class))).thenReturn(ResponseEntity.ok().body(singletonMap("foo", "bar")));

        ResponseEntity<Map<String, Serializable>> response = ops.getInfo(application);

        assertThat(response.getBody()).containsEntry("foo", "bar");
        assertThat(requestEntity.getValue().getHeaders()).containsEntry("auth", singletonList("foo:bar"));
    }

    @Test
    public void test_getHealth() {
        ArgumentCaptor<HttpEntity> requestEntity = ArgumentCaptor.forClass(HttpEntity.class);

        HttpHeaders headers = new HttpHeaders();
        headers.add("auth", "foo:bar");
        when(headersProvider.getHeaders(eq(application))).thenReturn(headers);

        when(restTemplate.exchange(eq(URI.create("http://health")), eq(HttpMethod.GET), requestEntity.capture(),
                eq(Map.class))).thenReturn(ResponseEntity.ok().body(singletonMap("foo", "bar")));

        ResponseEntity<Map<String, Serializable>> response = ops.getHealth(application);

        assertThat(response.getBody()).containsEntry("foo", "bar");
        assertThat(requestEntity.getValue().getHeaders()).containsEntry("auth", singletonList("foo:bar"));
    }

}
