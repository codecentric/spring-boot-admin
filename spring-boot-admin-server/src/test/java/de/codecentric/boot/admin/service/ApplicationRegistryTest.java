package de.codecentric.boot.admin.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.codecentric.boot.admin.config.WebappConfig;
import de.codecentric.boot.admin.model.Application;

@ContextConfiguration(classes = { WebappConfig.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class ApplicationRegistryTest {

	@Autowired
	private ApplicationRegistry registry;

	@Test(expected = NullPointerException.class)
	public void registerFailed1() throws Exception {
		registry.register(new Application());
	}

	@Test(expected = NullPointerException.class)
	public void registerFailed2() throws Exception {
		Application app = new Application();
		app.setId("abc");
		registry.register(app);
	}

	@Test(expected = IllegalArgumentException.class)
	public void registerFailed3() throws Exception {
		Application app = new Application();
		app.setId("abc");
		app.setUrl("not-an-url");
		registry.register(app);
	}

	@Test
	public void register() throws Exception {
		Application app = new Application();
		app.setId("abc");
		app.setUrl("http://localhost:8080");
		registry.register(app);
	}

	@Test
	public void isRegistered() throws Exception {
		Application app = new Application();
		app.setId("abc");
		app.setUrl("http://localhost:8080");
		registry.register(app);

		assertFalse(registry.isRegistered("xyz"));
		assertTrue(registry.isRegistered("abc"));
	}

	@Test
	public void getApplication() throws Exception {
		Application app = new Application();
		app.setId("abc");
		app.setUrl("http://localhost:8080");
		registry.register(app);

		assertEquals(app, registry.getApplication("abc"));
	}

	@Test
	public void getApplications() throws Exception {
		Application app = new Application();
		app.setId("abc");
		app.setUrl("http://localhost:8080");
		registry.register(app);

		assertEquals(1, registry.getApplications().size());
		assertEquals(app, registry.getApplications().get(0));
	}

}
