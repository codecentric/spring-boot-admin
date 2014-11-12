package de.codecentric.boot.admin.service.impl;

import de.codecentric.boot.admin.config.AdminServerConfig;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.service.RegistryService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URL;

import static de.codecentric.boot.admin.TestData.createApplication;
import static de.codecentric.boot.admin.model.Application.ApplicationBuilder;
import static org.junit.Assert.*;

@SpringApplicationConfiguration(classes = { AdminServerConfig.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class CachedApplicationRegistryTest {

	@Autowired
	private RegistryService registry;

    @Before
    public void clearRegistry(){
        registry.removeAllApplications();
    }

	@Test(expected = IllegalArgumentException.class)
    public void registerApplicationWithoutMandatoryFields() throws Exception {
        // Test application without all mandatory fields
        registry.registerApplication(new ApplicationBuilder("abc", null).build());
    }

	@Test
	public void registerApplication() throws Exception {
        Application app = createApplication("abc");
        Long id = registry.registerApplication(app);
        assertNotNull("ID must not be null!", id);
	}

    @Test
    public void updateApplication() throws Exception {
        Application app = createApplication("abc");
        Long id = registry.registerApplication(app);
        assertNotNull("ID must not be null!", id);

        Application updatedApp = new ApplicationBuilder("abc2", new URL("http://localhost:8080")).withId(id).build();
        Application returnedApp = registry.updateApplication(updatedApp);

        assertEquals("Application name is wrong!", "abc2", returnedApp.getName());

    }

    @Test
    public void registerSameApplicationTwice() throws Exception {
        Application app = createApplication("abc");
        Long id = registry.registerApplication(app);
        assertNotNull("ID must not be null!", id);

        // Register the same application
        Long id2 = registry.registerApplication(app);
        assertNotEquals("ID must not be the same!", id, id2);
    }


    @Test
    public void unregisterApplicationById() throws Exception {
        Application app = createApplication("abc");
        Long id = registry.registerApplication(app);
        assertNotNull("ID must not be null!", id);
        registry.unregisterApplicationById(id);
        assertFalse("Application is not unregistered", registry.isRegistered(id));
    }

	@Test
	public void isRegistered() throws Exception {
        Application app = createApplication("abc");
        Long id = registry.registerApplication(app);

		assertFalse(registry.isRegistered(123L));
		assertTrue(registry.isRegistered(id));
	}

	@Test
	public void getApplicationById() throws Exception {
        Application app = createApplication("abc");
        Long id = registry.registerApplication(app);
		assertEquals(new ApplicationBuilder(app).withId(id).build(), registry.getApplicationById(id));
	}

	@Test
	public void getAllApplications() throws Exception {
        Application app = createApplication("abc");
		Long id = registry.registerApplication(app);

		assertEquals(1, registry.getAllApplications().size());
		assertEquals(new ApplicationBuilder(app).withId(id).build(), registry.getAllApplications().get(0));
	}

    @Test
    public void removeAllApplications() throws Exception {
        Application app = createApplication("abc");
        registry.registerApplication(app);
        assertEquals(1, registry.getAllApplications().size());
        registry.removeAllApplications();
        assertEquals(0, registry.getAllApplications().size());
    }

}
