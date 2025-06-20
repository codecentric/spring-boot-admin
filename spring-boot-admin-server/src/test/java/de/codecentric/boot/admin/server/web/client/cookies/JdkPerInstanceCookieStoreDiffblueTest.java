package de.codecentric.boot.admin.server.web.client.cookies;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.notify.PagerdutyNotifier;
import java.net.CookieManager;
import java.net.CookiePolicy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(classes = { JdkPerInstanceCookieStore.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@RunWith(SpringJUnit4ClassRunner.class)
public class JdkPerInstanceCookieStoreDiffblueTest {

	@Autowired
	private JdkPerInstanceCookieStore jdkPerInstanceCookieStore;

	/**
	 * Test {@link JdkPerInstanceCookieStore#JdkPerInstanceCookieStore()}.
	 * <p>
	 * Method under test: {@link JdkPerInstanceCookieStore#JdkPerInstanceCookieStore()}
	 */
	@Test
	public void testNewJdkPerInstanceCookieStore() {
		// Diffblue Cover was unable to create a Spring-specific test for this Spring
		// method.
		// Run dcover create --keep-partial-tests to gain insights into why
		// a non-Spring test was created.

		// Arrange, Act and Assert
		assertTrue(new JdkPerInstanceCookieStore().createCookieHandler(null) instanceof CookieManager);
	}

	/**
	 * Test {@link JdkPerInstanceCookieStore#JdkPerInstanceCookieStore(CookiePolicy)}.
	 * <ul>
	 * <li>Then createCookieHandler {@code null} return {@link CookieManager}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link JdkPerInstanceCookieStore#JdkPerInstanceCookieStore(CookiePolicy)}
	 */
	@Test
	public void testNewJdkPerInstanceCookieStore_thenCreateCookieHandlerNullReturnCookieManager() {
		// Diffblue Cover was unable to create a Spring-specific test for this Spring
		// method.
		// Run dcover create --keep-partial-tests to gain insights into why
		// a non-Spring test was created.

		// Arrange, Act and Assert
		assertTrue(new JdkPerInstanceCookieStore(mock(CookiePolicy.class))
			.createCookieHandler(null) instanceof CookieManager);
	}

	/**
	 * Test {@link JdkPerInstanceCookieStore#getCookieHandler(InstanceId)}.
	 * <ul>
	 * <li>Given {@link JdkPerInstanceCookieStore}.</li>
	 * <li>When {@link InstanceId} with {@code Value}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link JdkPerInstanceCookieStore#getCookieHandler(InstanceId)}
	 */
	@Test
	public void testGetCookieHandler_givenJdkPerInstanceCookieStore_whenInstanceIdWithValue() {
		// Arrange, Act and Assert
		assertTrue(jdkPerInstanceCookieStore.getCookieHandler(InstanceId.of("Value")) instanceof CookieManager);
	}

	/**
	 * Test {@link JdkPerInstanceCookieStore#getCookieHandler(InstanceId)}.
	 * <ul>
	 * <li>Given {@link JdkPerInstanceCookieStore}.</li>
	 * <li>When {@link InstanceId} with value is {@code 42}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link JdkPerInstanceCookieStore#getCookieHandler(InstanceId)}
	 */
	@Test
	public void testGetCookieHandler_givenJdkPerInstanceCookieStore_whenInstanceIdWithValueIs42() {
		// Arrange, Act and Assert
		assertTrue(jdkPerInstanceCookieStore.getCookieHandler(InstanceId.of("42")) instanceof CookieManager);
	}

	/**
	 * Test {@link JdkPerInstanceCookieStore#getCookieHandler(InstanceId)}.
	 * <ul>
	 * <li>Given {@link JdkPerInstanceCookieStore#JdkPerInstanceCookieStore()}.</li>
	 * <li>When {@link InstanceId} with value is {@code 42}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link JdkPerInstanceCookieStore#getCookieHandler(InstanceId)}
	 */
	@Test
	public void testGetCookieHandler_givenJdkPerInstanceCookieStore_whenInstanceIdWithValueIs422() {
		// Arrange
		JdkPerInstanceCookieStore jdkPerInstanceCookieStore = new JdkPerInstanceCookieStore();
		InstanceId instanceId = InstanceId.of("42");
		jdkPerInstanceCookieStore.put(instanceId, PagerdutyNotifier.DEFAULT_URI, new HttpHeaders());

		// Act and Assert
		assertTrue(jdkPerInstanceCookieStore.getCookieHandler(InstanceId.of("42")) instanceof CookieManager);
	}

	/**
	 * Test {@link JdkPerInstanceCookieStore#createCookieHandler(InstanceId)}.
	 * <p>
	 * Method under test:
	 * {@link JdkPerInstanceCookieStore#createCookieHandler(InstanceId)}
	 */
	@Test
	public void testCreateCookieHandler() {
		// Arrange, Act and Assert
		assertTrue(jdkPerInstanceCookieStore.createCookieHandler(InstanceId.of("42")) instanceof CookieManager);
	}

}
