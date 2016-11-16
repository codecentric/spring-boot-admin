package de.codecentric.boot.admin.services;

import de.codecentric.boot.admin.config.AdminClientProperties;
import de.codecentric.boot.admin.model.Application;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.springframework.util.StringUtils.trimLeadingCharacter;

/**
 * A factory for creating instances of {@link Application}
 *
 * @author rfelgentraeger
 */
public class ApplicationFactory {


    private AdminClientProperties client;

    private ManagementServerProperties management;

    private ServerProperties server;

    private Integer serverPort;

    private Integer managementPort;

    @Value("${endpoints.health.path:/${endpoints.health.id:health}}")
    private String healthEndpointPath;

    public ApplicationFactory(AdminClientProperties client, ManagementServerProperties management, ServerProperties server) {
        this.client = client;
        this.management = management;
        this.server = server;
    }

    public Application createApplication() {
        //Remember, that not all information are set by the factory, e.g. id and status is set by ApplicationRegistry
        //TODO: rethink about the co-existence of builder and factory pattern for the domain class Application (Appliation.Builder and ApplicationFactory)!
        return Application.create(doName()).withHealthUrl(doHealthUrl())
                .withManagementUrl(doManagementUrl()).withServiceUrl(doServiceUrl())
                .build();
    }

    @EventListener
    protected void onApplicationReady(ApplicationReadyEvent event) {
        if (event.getApplicationContext() instanceof WebApplicationContext) {
            serverPort = event.getApplicationContext().getEnvironment()
                    .getProperty("local.server.port", Integer.class);
            managementPort = event.getApplicationContext().getEnvironment()
                    .getProperty("local.management.port", Integer.class, serverPort);
        }
    }

    protected String doName() {
        return client.getName();
    }

    protected String doServiceUrl() {
        if (client.getServiceUrl() != null) {
            return client.getServiceUrl();
        }

        if (serverPort == null) {
            throw new IllegalStateException(
                    "serviceUrl must be set when deployed to servlet-container");
        }

        return UriComponentsBuilder.newInstance().scheme(getScheme()).host(doServiceHost())
                .port(serverPort).path(server.getContextPath()).toUriString();
    }

    protected String doManagementUrl() {
        if (client.getManagementUrl() != null) {
            return client.getManagementUrl();
        }

        if (managementPort == null || managementPort.equals(serverPort)) {
            return UriComponentsBuilder.fromHttpUrl(doServiceUrl())
                    .pathSegment(server.getServletPrefix().split("/"))
                    .pathSegment(trimLeadingCharacter(management.getContextPath(), '/').split("/"))
                    .toUriString();
        }

        return UriComponentsBuilder.newInstance().scheme(getScheme()).host(doManagementHost())
                .port(managementPort).path(management.getContextPath()).toUriString();
    }

    protected String doHealthUrl() {
        if (client.getHealthUrl() != null) {
            return client.getHealthUrl();
        }
        return UriComponentsBuilder.fromHttpUrl(doManagementUrl())
                .pathSegment(trimLeadingCharacter(healthEndpointPath, '/').split("/"))
                .toUriString();
    }

    protected String doServiceHost() {
        InetAddress address = server.getAddress();
        if (address == null) {
            address = getLocalHost();
        }
        return getHost(address);
    }

    protected String doManagementHost() {
        InetAddress address = management.getAddress();
        if (address != null) {
            return getHost(address);
        }
        return doServiceHost();
    }

    private String getScheme() {
        return server.getSsl() != null && server.getSsl().isEnabled() ? "https" : "http";
    }

    private String getHost(InetAddress address) {
        return client.isPreferIp() ? address.getHostAddress() : address.getCanonicalHostName();
    }

    private InetAddress getLocalHost() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }
}
