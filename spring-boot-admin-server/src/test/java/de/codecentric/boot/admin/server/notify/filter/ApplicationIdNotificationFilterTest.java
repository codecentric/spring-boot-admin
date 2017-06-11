package de.codecentric.boot.admin.server.notify.filter;

import de.codecentric.boot.admin.server.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.ApplicationId;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationIdNotificationFilterTest {
    @Test
    public void test_filterByName() {
        NotificationFilter filter = new ApplicationIdNotificationFilter(ApplicationId.of("cafebabe"), -1L);
        
        Application filteredApplication = Application.create("foo")
                                                     .withId(ApplicationId.of("cafebabe"))
                                                     .withHealthUrl("http://health")
                                                     .build();
        ClientApplicationRegisteredEvent filteredEvent = new ClientApplicationRegisteredEvent(filteredApplication);
        assertThat(filter.filter(filteredEvent)).isTrue();

        Application ignoredApplication = Application.create("foo")
                                                    .withId(ApplicationId.of("id-2"))
                                                    .withHealthUrl("http://health")
                                                    .build();
        ClientApplicationRegisteredEvent ignoredEvent = new ClientApplicationRegisteredEvent(ignoredApplication);
        assertThat(filter.filter(ignoredEvent)).isFalse();
    }
}
