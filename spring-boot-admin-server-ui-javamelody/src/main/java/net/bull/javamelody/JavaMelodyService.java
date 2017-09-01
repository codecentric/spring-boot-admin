package net.bull.javamelody;

import de.codecentric.boot.admin.event.ClientApplicationDeregisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.model.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

import java.io.IOException;

/**
 * Service class in the javamelody package, so we can access the javamelody Parameters class in which we can
 * register and unregister applications. Using the {@link EventListener annotations} we receive notifications from service discovery.
 */
public class JavaMelodyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaMelodyService.class);

    @EventListener
    public void onClientApplicationRegistered(ClientApplicationRegisteredEvent event) {
        final Application application = event.getApplication();
        final String applicationName = application.getName() + "-" + application.getId();

        try {
            Parameters.addCollectorApplication(applicationName, Parameters.parseUrl(application.getServiceUrl()));
            LOGGER.info("Added application {} to java melody", applicationName);
        } catch (IOException e) {
            LOGGER.warn("Failed to register application {} to java melody" + applicationName);
        }
    }

    @EventListener
    public void onClientApplicationDeregistered(ClientApplicationDeregisteredEvent event) {
        final Application application = event.getApplication();
        final String applicationName = application.getName() + "-" + application.getId();

        try {
            Parameters.removeCollectorApplication(applicationName);
            LOGGER.info("Removed application {} from java melody", applicationName);
        } catch (IOException e) {
            LOGGER.warn("Failed to unregister application {} from java melody" + applicationName);
        }
    }

}
