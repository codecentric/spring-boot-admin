package de.codecentric.boot.admin.server.domain.values;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class EndpointsDiffblueTest {

	/**
	 * Test {@link Endpoints#get(String)}.
	 * <p>
	 * Method under test: {@link Endpoints#get(String)}
	 */
	@Test
	public void testGet() {
		// Arrange, Act and Assert
		assertFalse(Endpoints.empty().get("42").isPresent());
	}

	/**
	 * Test {@link Endpoints#isPresent(String)}.
	 * <ul>
	 * <li>Given empty.</li>
	 * <li>Then return {@code false}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link Endpoints#isPresent(String)}
	 */
	@Test
	public void testIsPresent_givenEmpty_thenReturnFalse() {
		// Arrange, Act and Assert
		assertFalse(Endpoints.empty().isPresent("42"));
	}

	/**
	 * Test {@link Endpoints#isPresent(String)}.
	 * <ul>
	 * <li>Given single {@code 42} and {@code https://example.org/example}.</li>
	 * <li>Then return {@code true}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link Endpoints#isPresent(String)}
	 */
	@Test
	public void testIsPresent_givenSingle42AndHttpsExampleOrgExample_thenReturnTrue() {
		// Arrange, Act and Assert
		assertTrue(Endpoints.single("42", "https://example.org/example").isPresent("42"));
	}

	/**
	 * Test {@link Endpoints#iterator()}.
	 * <p>
	 * Method under test: {@link Endpoints#iterator()}
	 */
	@Test
	public void testIterator() {
		// Arrange, Act and Assert
		assertFalse(Endpoints.empty().iterator().hasNext());
	}

	/**
	 * Test {@link Endpoints#empty()}.
	 * <p>
	 * Method under test: {@link Endpoints#empty()}
	 */
	@Test
	public void testEmpty() {
		// Arrange and Act
		Endpoints actualEmptyResult = Endpoints.empty();

		// Assert
		assertFalse(actualEmptyResult.iterator().hasNext());
		Stream<Endpoint> streamResult = actualEmptyResult.stream();
		assertTrue(streamResult.limit(5).collect(Collectors.toList()).isEmpty());
	}

	/**
	 * Test {@link Endpoints#single(String, String)}.
	 * <ul>
	 * <li>When {@code 42}.</li>
	 * <li>Then return iterator next Id is {@code 42}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link Endpoints#single(String, String)}
	 */
	@Test
	public void testSingle_when42_thenReturnIteratorNextIdIs42() {
		// Arrange and Act
		Endpoints actualSingleResult = Endpoints.single("42", "https://example.org/example");

		// Assert
		Iterator<Endpoint> iteratorResult = actualSingleResult.iterator();
		Endpoint nextResult = iteratorResult.next();
		assertEquals("42", nextResult.getId());
		assertEquals("https://example.org/example", nextResult.getUrl());
		Stream<Endpoint> streamResult = actualSingleResult.stream();
		List<Endpoint> collectResult = streamResult.limit(5).collect(Collectors.toList());
		assertEquals(1, collectResult.size());
		assertFalse(iteratorResult.hasNext());
		assertSame(nextResult, collectResult.get(0));
	}

	/**
	 * Test {@link Endpoints#of(Collection)}.
	 * <ul>
	 * <li>Then return iterator next Id is {@code 42}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link Endpoints#of(Collection)}
	 */
	@Test
	public void testOf_thenReturnIteratorNextIdIs42() {
		// Arrange
		ArrayList<Endpoint> endpoints = new ArrayList<>();
		Endpoint ofResult = Endpoint.of("42", "https://example.org/example");
		endpoints.add(ofResult);

		// Act
		Endpoints actualOfResult = Endpoints.of(endpoints);

		// Assert
		Iterator<Endpoint> iteratorResult = actualOfResult.iterator();
		Endpoint nextResult = iteratorResult.next();
		assertEquals("42", nextResult.getId());
		Stream<Endpoint> streamResult = actualOfResult.stream();
		List<Endpoint> collectResult = streamResult.limit(5).collect(Collectors.toList());
		assertEquals(1, collectResult.size());
		assertFalse(iteratorResult.hasNext());
		assertSame(ofResult, nextResult);
		assertSame(ofResult, collectResult.get(0));
	}

	/**
	 * Test {@link Endpoints#of(Collection)}.
	 * <ul>
	 * <li>Then return iterator next Id is {@code Id}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link Endpoints#of(Collection)}
	 */
	@Test
	public void testOf_thenReturnIteratorNextIdIsId() {
		// Arrange
		Endpoint ofResult = Endpoint.of("Id", "https://example.org/example");

		ArrayList<Endpoint> endpoints = new ArrayList<>();
		endpoints.add(ofResult);
		Endpoint ofResult2 = Endpoint.of("42", "https://example.org/example");
		endpoints.add(ofResult2);

		// Act
		Endpoints actualOfResult = Endpoints.of(endpoints);

		// Assert
		Iterator<Endpoint> iteratorResult = actualOfResult.iterator();
		Endpoint nextResult = iteratorResult.next();
		Endpoint actualNextResult = iteratorResult.next();
		boolean actualHasNextResult = iteratorResult.hasNext();
		assertEquals("Id", nextResult.getId());
		Stream<Endpoint> streamResult = actualOfResult.stream();
		List<Endpoint> collectResult = streamResult.limit(5).collect(Collectors.toList());
		assertEquals(2, collectResult.size());
		assertFalse(actualHasNextResult);
		assertSame(ofResult2, actualNextResult);
		assertSame(ofResult2, collectResult.get(1));
		assertSame(ofResult, nextResult);
		assertSame(ofResult, collectResult.get(0));
	}

	/**
	 * Test {@link Endpoints#of(Collection)}.
	 * <ul>
	 * <li>When {@link ArrayList#ArrayList()}.</li>
	 * <li>Then return stream limit five collect toList Empty.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link Endpoints#of(Collection)}
	 */
	@Test
	public void testOf_whenArrayList_thenReturnStreamLimitFiveCollectToListEmpty() {
		// Arrange and Act
		Endpoints actualOfResult = Endpoints.of(new ArrayList<>());

		// Assert
		assertFalse(actualOfResult.iterator().hasNext());
		Stream<Endpoint> streamResult = actualOfResult.stream();
		assertTrue(streamResult.limit(5).collect(Collectors.toList()).isEmpty());
	}

	/**
	 * Test {@link Endpoints#of(Collection)}.
	 * <ul>
	 * <li>When {@code null}.</li>
	 * <li>Then return stream limit five collect toList Empty.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link Endpoints#of(Collection)}
	 */
	@Test
	public void testOf_whenNull_thenReturnStreamLimitFiveCollectToListEmpty() {
		// Arrange and Act
		Endpoints actualOfResult = Endpoints.of(null);

		// Assert
		assertFalse(actualOfResult.iterator().hasNext());
		Stream<Endpoint> streamResult = actualOfResult.stream();
		assertTrue(streamResult.limit(5).collect(Collectors.toList()).isEmpty());
	}

	/**
	 * Test {@link Endpoints#withEndpoint(String, String)}.
	 * <ul>
	 * <li>Given empty.</li>
	 * <li>Then return stream limit five collect toList size is one.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link Endpoints#withEndpoint(String, String)}
	 */
	@Test
	public void testWithEndpoint_givenEmpty_thenReturnStreamLimitFiveCollectToListSizeIsOne() {
		// Arrange and Act
		Endpoints actualWithEndpointResult = Endpoints.empty().withEndpoint("42", "https://example.org/example");

		// Assert
		Iterator<Endpoint> iteratorResult = actualWithEndpointResult.iterator();
		Endpoint nextResult = iteratorResult.next();
		assertEquals("42", nextResult.getId());
		Stream<Endpoint> streamResult = actualWithEndpointResult.stream();
		List<Endpoint> collectResult = streamResult.limit(5).collect(Collectors.toList());
		assertEquals(1, collectResult.size());
		assertFalse(iteratorResult.hasNext());
		assertSame(nextResult, collectResult.get(0));
	}

	/**
	 * Test {@link Endpoints#withEndpoint(String, String)}.
	 * <ul>
	 * <li>Then return iterator next Id is {@code Id}.</li>
	 * </ul>
	 * <p>
	 * Method under test: {@link Endpoints#withEndpoint(String, String)}
	 */
	@Test
	public void testWithEndpoint_thenReturnIteratorNextIdIsId() {
		// Arrange and Act
		Endpoints actualWithEndpointResult = Endpoints.single("Id", "https://example.org/example")
			.withEndpoint("42", "https://example.org/example");

		// Assert
		Iterator<Endpoint> iteratorResult = actualWithEndpointResult.iterator();
		Endpoint nextResult = iteratorResult.next();
		Endpoint nextResult2 = iteratorResult.next();
		boolean actualHasNextResult = iteratorResult.hasNext();
		assertEquals("42", nextResult2.getId());
		assertEquals("Id", nextResult.getId());
		assertEquals("https://example.org/example", nextResult2.getUrl());
		Stream<Endpoint> streamResult = actualWithEndpointResult.stream();
		List<Endpoint> collectResult = streamResult.limit(5).collect(Collectors.toList());
		assertEquals(2, collectResult.size());
		assertFalse(actualHasNextResult);
		assertSame(nextResult, collectResult.get(0));
		assertSame(nextResult2, collectResult.get(1));
	}

	/**
	 * Test {@link Endpoints#stream()}.
	 * <p>
	 * Method under test: {@link Endpoints#stream()}
	 */
	@Test
	public void testStream() {
		// Arrange and Act
		Stream<Endpoint> actualStreamResult = Endpoints.empty().stream();

		// Assert
		assertTrue(actualStreamResult.limit(5).collect(Collectors.toList()).isEmpty());
	}

}
