package spring.boot.admin.javamelody.listener;

import net.bull.javamelody.CollectorServlet;
import de.codecentric.boot.admin.event.ClientApplicationDeregisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.model.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

import java.io.IOException;

/**
 * Listener class to register and unregister applications. Using the {@link EventListener annotations} we receive notifications from service discovery.
 */
public class JavaMelodyListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaMelodyListener.class);

    @EventListener
    public void onClientApplicationRegistered(ClientApplicationRegisteredEvent event) {
        final Application application = event.getApplication();
        final String applicationName = application.getName() + "-" + application.getId();

        try {
            CollectorServlet.addCollectorApplication(applicationName, application.getServiceUrl());
            LOGGER.info("Added application {} to javamelody", applicationName);
        } catch (IOException e) {
            LOGGER.warn("Failed to register application {} to javamelody" + applicationName);
        }
    }

    @EventListener
    public void onClientApplicationDeregistered(ClientApplicationDeregisteredEvent event) {
        final Application application = event.getApplication();
        final String applicationName = application.getName() + "-" + application.getId();

        try {
            CollectorServlet.removeCollectorApplication(applicationName);
            LOGGER.info("Removed application {} from javamelody", applicationName);
        } catch (IOException e) {
            LOGGER.warn("Failed to unregister application {} from javamelody" + applicationName);
        }
    }

}
