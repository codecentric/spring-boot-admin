package de.codecentric.boot.admin.discovery.eureka;

import de.codecentric.boot.admin.config.EnableAdminServer;
import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.model.Application;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.concurrent.ExecutionException;

/**
 * Created by alexf on 11-Feb-16.
 */

public class EurekaDiscoveryListenerTest {

    private EmbeddedWebApplicationContext eurekaServer;

    @Before
    public void setup() {
        eurekaServer = (EmbeddedWebApplicationContext) new SpringApplicationBuilder(TestEurekaAdminServerApplication.class)
                .profiles("eurekaServer")
                .build().run();

    }

    @After
    public void shutdown() {

        eurekaServer.close();

    }

    @Test
    public void testGetDiscoveryClientListener() throws ExecutionException, InterruptedException {


        final String configuredHealthCheckUrl = eurekaServer.getEnvironment().getProperty("eureka.instance.health-check-url");
        final String configuredHomePageUrl = eurekaServer.getEnvironment().getProperty("eureka.instance.home-page-url");
        Application application = eurekaServer.getBean(TestEurekaAdminServerApplication.RegistrationMonitor.class).getApplication();
        Assert.assertEquals("Discovered health-check-url should be properly propagated.", configuredHealthCheckUrl, application.getHealthUrl());
        Assert.assertEquals("Discovered home-page-url should be properly propagated.", configuredHomePageUrl, application.getManagementUrl());

    }


    @EnableAdminServer
    @EnableEurekaServer
    @SpringBootApplication
    @EnableEurekaClient
    public static class TestEurekaAdminServerApplication {
        public static class RegistrationMonitor {

            private SettableListenableFuture<ClientApplicationRegisteredEvent> registeredApp = new SettableListenableFuture<>();

            @EventListener
            public void onAppRegistered(ClientApplicationRegisteredEvent clientApplicationEvent) {
                registeredApp.set(clientApplicationEvent);
            }

            public Application getApplication() throws ExecutionException, InterruptedException {
                return registeredApp.get().getApplication();
            }

        }

        @Bean
        public RegistrationMonitor registrationMonitor() {
            return new RegistrationMonitor();
        }

    }


}
