package spring.boot.admin.javamelody.listener;

import de.codecentric.boot.admin.event.ClientApplicationDeregisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.model.Application;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class JavaMelodyListenerTest {

    private JavaMelodyListener javaMelodyListener;

    @Mock
    private Application mockApplication;
    @Mock
    private ClientApplicationRegisteredEvent registerEvent;
    @Mock
    private ClientApplicationDeregisteredEvent deregisteredEvent;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        javaMelodyListener = new JavaMelodyListener();
    }

    /**
     * Create one test in which registration and deregistration is tested.
     */
    @Test
    public void testRegisterAndDeregister() throws Exception {
        when(mockApplication.getName()).thenReturn("testName");
        when(mockApplication.getId()).thenReturn("testId");
        when(mockApplication.getServiceUrl()).thenReturn("http://service-test.url");

        when(registerEvent.getType()).thenReturn("testRegister");
        when(registerEvent.getApplication()).thenReturn(mockApplication);

        when(deregisteredEvent.getType()).thenReturn("testDeregister");
        when(deregisteredEvent.getApplication()).thenReturn(mockApplication);

        javaMelodyListener.onClientApplicationRegistered(registerEvent);

        javaMelodyListener.onClientApplicationDeregistered(deregisteredEvent);
    }

}