package de.codecentric.boot.admin.server.eventstore;

import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@ContextConfiguration(classes = { InMemoryEventStore.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class InMemoryEventStoreDiffblueTest {

	@Autowired
	private InMemoryEventStore inMemoryEventStore;

	/**
	 * Test {@link InMemoryEventStore#InMemoryEventStore()}.
	 * <p>
	 * Method under test: {@link InMemoryEventStore#InMemoryEventStore()}
	 */
	@Test
	public void testNewInMemoryEventStore() throws AssertionError {
		// Arrange, Act and Assert
		FirstStep<InstanceEvent> createResult = StepVerifier.create(new InMemoryEventStore().findAll());
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link InMemoryEventStore#InMemoryEventStore(int)}.
	 * <p>
	 * Method under test: {@link InMemoryEventStore#InMemoryEventStore(int)}
	 */
	@Test
	public void testNewInMemoryEventStore2() throws AssertionError {
		// Arrange, Act and Assert
		FirstStep<InstanceEvent> createResult = StepVerifier.create(new InMemoryEventStore(3).findAll());
		createResult.expectComplete().verify();
	}

	/**
	 * Test {@link InMemoryEventStore#append(List)}.
	 * <ul>
	 * <li>Given {@link InstanceId} with value is {@code 42}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InMemoryEventStore#append(List)}
	 */
	@Test
	public void testAppend_givenInstanceIdWithValueIs42() throws AssertionError {
		// Arrange
		ArrayList<InstanceEvent> events = new ArrayList<>();
		events.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));

		// Act
		inMemoryEventStore.append(events);

		// Assert
		FirstStep<InstanceEvent> createResult = StepVerifier.create(inMemoryEventStore.findAll());
		createResult.assertNext(i -> {
		}).expectComplete().verify();
	}

	/**
	 * Test {@link InMemoryEventStore#append(List)}.
	 * <ul>
	 * <li>Given {@link InstanceId} with value is {@code 42}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InMemoryEventStore#append(List)}
	 */
	@Test
	public void testAppend_givenInstanceIdWithValueIs422() throws AssertionError {
		// Arrange
		ArrayList<InstanceEvent> events = new ArrayList<>();
		events.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));
		events.add(new InstanceDeregisteredEvent(InstanceId.of("42"), 1L));

		// Act and Assert
		FirstStep<Void> createResult = StepVerifier.create(inMemoryEventStore.append(events));
		createResult.expectError().verify();
		FirstStep<InstanceEvent> createResult2 = StepVerifier.create(inMemoryEventStore.findAll());
		createResult2.assertNext(i -> {
		}).expectComplete().verify();
	}

	/**
	 * Test {@link InMemoryEventStore#append(List)}.
	 * <ul>
	 * <li>When {@link ArrayList#ArrayList()}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link InMemoryEventStore#append(List)}
	 */
	@Test
	public void testAppend_whenArrayList() throws AssertionError {
		// Arrange, Act and Assert
		FirstStep<Void> createResult = StepVerifier.create(inMemoryEventStore.append(new ArrayList<>()));
		createResult.expectComplete().verify();
	}

}
