package net.bull.javamelody;

import de.codecentric.boot.admin.event.ClientApplicationDeregisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.model.Application;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class JavaMelodyServiceTest {

    private JavaMelodyService javaMelodyService;

    @Mock
    private Application mockApplication;
    @Mock
    private ClientApplicationRegisteredEvent registerEvent;
    @Mock
    private ClientApplicationDeregisteredEvent deregisteredEvent;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        javaMelodyService = new JavaMelodyService();
    }

    /**
     * Create one test in which registration and deregistration is tested. Normally you
     * would create separate tests for this, but in this case it is hard to mock the {@link Parameters}
     * object. Since tests could run in arbitrary order, one test case is used.
     */
    @Test
    public void testRegisterAndDeregister() throws Exception {
        assertThat(Parameters.getCollectorUrlsByApplications().size(), is(0));

        when(mockApplication.getName()).thenReturn("testName");
        when(mockApplication.getId()).thenReturn("testId");
        when(mockApplication.getManagementUrl()).thenReturn("http://management-test.url");

        when(registerEvent.getType()).thenReturn("testRegister");
        when(registerEvent.getApplication()).thenReturn(mockApplication);

        when(deregisteredEvent.getType()).thenReturn("testDeregister");
        when(deregisteredEvent.getApplication()).thenReturn(mockApplication);

        javaMelodyService.onClientApplicationRegistered(registerEvent);

        final Map<String, List<URL>> collectorUrlsByApplications = Parameters.getCollectorUrlsByApplications();
        assertThat(collectorUrlsByApplications.size(), is(1));
        assertTrue("Javamelody should contain an application with a key 'testName-testId'", collectorUrlsByApplications.containsKey("testName-testId"));

        final URL url = collectorUrlsByApplications.get("testName-testId").get(0);
        assertThat(url, is(new URL("http://management-test.url/monitoring?collector=stop&format=serialized")));

        javaMelodyService.onClientApplicationDeregistered(deregisteredEvent);
        assertThat(Parameters.getCollectorUrlsByApplications().size(), is(0));
    }

}