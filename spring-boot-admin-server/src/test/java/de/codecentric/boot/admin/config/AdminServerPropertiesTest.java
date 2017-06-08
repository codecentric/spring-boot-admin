package de.codecentric.boot.admin.config;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AdminServerPropertiesTest {

	@Test(expected = IllegalArgumentException.class)
	public void setContextPathSingleSlash() {
		new AdminServerProperties().setContextPath("/");
	}

	@Test(expected = IllegalArgumentException.class)
	public void setContextPathDoubleSlash() {
		new AdminServerProperties().setContextPath("/test/");
	}

	@Test(expected = IllegalArgumentException.class)
	public void setContextPathNoBeginSlash() {
		new AdminServerProperties().setContextPath("test/");
	}

	@Test
	public void setContextPathHappyPath() {
		AdminServerProperties adminServerProperties = new AdminServerProperties();
		adminServerProperties.setContextPath("/test");
		assertTrue(adminServerProperties.getContextPath().equalsIgnoreCase("/test"));
	}

	@Test
	public void setEndpoints(){
		String[] endpoints = {"one", "two"};
		AdminServerProperties adminServerProperties = new AdminServerProperties();
		adminServerProperties.getRoutes().setEndpoints(endpoints);
		assertFalse(adminServerProperties.getRoutes().getEndpoints().equals(endpoints));
		assertTrue(Arrays.equals(adminServerProperties.getRoutes().getEndpoints(), endpoints));
	}

	@Test
	public void setPeriod() {
		AdminServerProperties adminServerProperties = new AdminServerProperties();
		adminServerProperties.getMonitor().setPeriod(1l);
		assertTrue(adminServerProperties.getMonitor().getPeriod() == 1l);
	}

	@Test
	public void getStatusLifetime() {
		AdminServerProperties adminServerProperties = new AdminServerProperties();
		adminServerProperties.getMonitor().setStatusLifetime(1l);
		assertTrue(adminServerProperties.getMonitor().getStatusLifetime() == 1l);
	}

	@Test
	public void getConnectTimeout() {
		AdminServerProperties adminServerProperties = new AdminServerProperties();
		adminServerProperties.getMonitor().setConnectTimeout(1);
		assertTrue(adminServerProperties.getMonitor().getConnectTimeout() == 1);
	}

	@Test
	public void getReadTimeout() {
		AdminServerProperties adminServerProperties = new AdminServerProperties();
		adminServerProperties.getMonitor().setReadTimeout(1);
		assertTrue(adminServerProperties.getMonitor().getReadTimeout() == 1);
	}
}