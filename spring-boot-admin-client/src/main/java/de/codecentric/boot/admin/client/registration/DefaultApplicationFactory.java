package de.codecentric.boot.admin.client.registration;

import de.codecentric.boot.admin.client.config.AdminClientProperties;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import javax.servlet.ServletContext;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.embedded.Ssl;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Default implementation for creating the {@link Application} instance which gets registered at the
 * admin server.
 *
 * @author Johannes Edmeier
 * @author Rene Felgenträger
 */
public class DefaultApplicationFactory implements ApplicationFactory {

    private AdminClientProperties client;
    private ServerProperties server;
    private ManagementServerProperties management;
    private Integer localServerPort;
    private Integer localManagementPort;
    private final ServletContext servletContext;
    private String healthEndpointPath;

    public DefaultApplicationFactory(AdminClientProperties client,
                                     ManagementServerProperties management,
                                     ServerProperties server,
                                     ServletContext servletContext,
                                     String healthEndpointPath) {
        this.client = client;
        this.management = management;
        this.server = server;
        this.servletContext = servletContext;
        this.healthEndpointPath = healthEndpointPath;
    }

    @Override
    public Application createApplication() {
        return Application.create(getName())
                          .withHealthUrl(getHealthUrl())
                          .withManagementUrl(getManagementUrl())
                          .withServiceUrl(getServiceUrl())
                          .withMetadata(getMetadata())
                          .build();
    }

    protected String getName() {
        return client.getName();
    }

    protected String getServiceUrl() {
        if (client.getServiceUrl() != null) {
            return UriComponentsBuilder.fromUriString(client.getServiceUrl()).toUriString();
        }

        String baseUrl = client.getServiceBaseUrl();
        if (getLocalServerPort() == null && StringUtils.isEmpty(baseUrl)) {
            throw new IllegalStateException("service-base-url must be set when deployed to servlet-container");
        }

        UriComponentsBuilder builder;
        if (!StringUtils.isEmpty(baseUrl)) {
            builder = UriComponentsBuilder.fromUriString(baseUrl);
        } else {
            builder = UriComponentsBuilder.newInstance()
                                          .scheme(getScheme(server.getSsl()))
                                          .host(getServiceHost())
                                          .port(getLocalServerPort());
        }

        return builder.path("/").path(servletContext.getContextPath()).path("/").toUriString();
    }

    protected String getManagementUrl() {
        if (client.getManagementUrl() != null) {
            return client.getManagementUrl();
        }

        String baseUrl = client.getManagementBaseUrl();

        UriComponentsBuilder builder;
        if (!StringUtils.isEmpty(baseUrl)) {
            builder = UriComponentsBuilder.fromUriString(baseUrl);
        } else if (isManagementPortEqual()) {
            builder = UriComponentsBuilder.fromHttpUrl(getServiceUrl()).path("/").path(server.getServletPrefix());
        } else {
            Ssl ssl = management.getSsl() != null ? management.getSsl() : server.getSsl();
            builder = UriComponentsBuilder.newInstance()
                                          .scheme(getScheme(ssl))
                                          .host(getManagementHost())
                                          .port(getLocalManagementPort());
        }

        return builder.path("/").path(management.getContextPath()).path("/").toUriString();
    }

    protected boolean isManagementPortEqual() {
        return getLocalManagementPort() == null || getLocalManagementPort().equals(getLocalServerPort());
    }

    protected String getHealthUrl() {
        if (client.getHealthUrl() != null) {
            return client.getHealthUrl();
        }
        return UriComponentsBuilder.fromHttpUrl(getManagementUrl())
                                   .path("/")
                                   .path(getHealthEndpointPath())
                                   .path("/")
                                   .toUriString();
    }

    protected Map<String, String> getMetadata() {
        return client.getMetadata();
    }

    protected String getServiceHost() {
        InetAddress address = server.getAddress();
        if (address == null) {
            address = getLocalHost();
        }
        return getHost(address);
    }

    protected String getManagementHost() {
        InetAddress address = management.getAddress();
        if (address != null) {
            return getHost(address);
        }
        return getServiceHost();
    }

    protected InetAddress getLocalHost() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    protected Integer getLocalServerPort() {
        return localServerPort;
    }

    protected Integer getLocalManagementPort() {
        return localManagementPort;
    }

    protected String getHealthEndpointPath() {
        return healthEndpointPath;
    }

    protected String getScheme(Ssl ssl) {
        return ssl != null && ssl.isEnabled() ? "https" : "http";
    }

    protected String getHost(InetAddress address) {
        return client.isPreferIp() ? address.getHostAddress() : address.getCanonicalHostName();
    }

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        if (event.getApplicationContext() instanceof WebApplicationContext) {
            localServerPort = event.getApplicationContext()
                                   .getEnvironment()
                                   .getProperty("local.server.port", Integer.class);
            localManagementPort = event.getApplicationContext()
                                       .getEnvironment()
                                       .getProperty("local.management.port", Integer.class, localServerPort);
        }
    }
}
