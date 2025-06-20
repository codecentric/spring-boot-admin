package de.codecentric.boot.admin.server.eventstore;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.MapListener;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

public class HazelcastEventStoreDiffblueTest {

	/**
	 * Test {@link HazelcastEventStore#HazelcastEventStore(int, IMap)}.
	 * <ul>
	 * <li>Given randomUUID.</li>
	 * <li>Then calls {@link IMap#addEntryListener(MapListener, boolean)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link HazelcastEventStore#HazelcastEventStore(int, IMap)}
	 */
	@Test
	public void testNewHazelcastEventStore_givenRandomUUID_thenCallsAddEntryListener() throws AssertionError {
		// Arrange
		IMap<InstanceId, List<InstanceEvent>> eventLog = mock(IMap.class);
		when(eventLog.addEntryListener(Mockito.<MapListener>any(), anyBoolean())).thenReturn(UUID.randomUUID());

		// Act and Assert
		FirstStep<InstanceEvent> createResult = StepVerifier.create(new HazelcastEventStore(3, eventLog).findAll());
		createResult.expectComplete().verify();
		verify(eventLog).addEntryListener(isA(MapListener.class), eq(true));
	}

	/**
	 * Test {@link HazelcastEventStore#HazelcastEventStore(IMap)}.
	 * <ul>
	 * <li>Given randomUUID.</li>
	 * <li>Then calls {@link IMap#addEntryListener(MapListener, boolean)}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link HazelcastEventStore#HazelcastEventStore(IMap)}
	 */
	@Test
	public void testNewHazelcastEventStore_givenRandomUUID_thenCallsAddEntryListener2() throws AssertionError {
		// Arrange
		IMap<InstanceId, List<InstanceEvent>> eventLogs = mock(IMap.class);
		when(eventLogs.addEntryListener(Mockito.<MapListener>any(), anyBoolean())).thenReturn(UUID.randomUUID());

		// Act and Assert
		FirstStep<InstanceEvent> createResult = StepVerifier.create(new HazelcastEventStore(eventLogs).findAll());
		createResult.expectComplete().verify();
		verify(eventLogs).addEntryListener(isA(MapListener.class), eq(true));
	}

}
