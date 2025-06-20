package de.codecentric.boot.admin.server.web.client;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class RefreshInstancesEventDiffblueTest {

	/**
	 * Test {@link RefreshInstancesEvent#RefreshInstancesEvent(Object)}.
	 * <p>
	 * Method under test: {@link RefreshInstancesEvent#RefreshInstancesEvent(Object)}
	 */
	@Test
	public void testNewRefreshInstancesEvent() {
		// Arrange, Act and Assert
		assertEquals("Source", new RefreshInstancesEvent("Source").getSource());
	}

}
