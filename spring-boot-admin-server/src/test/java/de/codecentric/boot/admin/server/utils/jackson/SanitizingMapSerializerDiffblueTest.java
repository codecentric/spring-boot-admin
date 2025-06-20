package de.codecentric.boot.admin.server.utils.jackson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.filter.FilteringGeneratorDelegate;
import com.fasterxml.jackson.core.filter.TokenFilter;
import com.fasterxml.jackson.core.filter.TokenFilter.Inclusion;
import com.fasterxml.jackson.core.filter.TokenFilterContext;
import com.fasterxml.jackson.core.util.JsonGeneratorDelegate;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider.Impl;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.mockito.Mockito;

public class SanitizingMapSerializerDiffblueTest {

	/**
	 * Test {@link SanitizingMapSerializer#SanitizingMapSerializer(String[])}.
	 * <p>
	 * Method under test:
	 * {@link SanitizingMapSerializer#SanitizingMapSerializer(String[])}
	 */
	@Test
	public void testNewSanitizingMapSerializer() {
		// Arrange and Act
		SanitizingMapSerializer actualSanitizingMapSerializer = new SanitizingMapSerializer(
				new String[] { "Patterns" });

		// Assert
		assertNull(actualSanitizingMapSerializer.getDelegatee());
		assertFalse(actualSanitizingMapSerializer.isUnwrappingSerializer());
	}

	/**
	 * Test
	 * {@link SanitizingMapSerializer#serialize(Map, JsonGenerator, SerializerProvider)}
	 * with {@code Map}, {@code JsonGenerator}, {@code SerializerProvider}.
	 * <p>
	 * Method under test:
	 * {@link SanitizingMapSerializer#serialize(Map, JsonGenerator, SerializerProvider)}
	 */
	@Test
	public void testSerializeWithMapJsonGeneratorSerializerProvider() throws IOException {
		// Arrange
		SanitizingMapSerializer sanitizingMapSerializer = new SanitizingMapSerializer(new String[] {});

		HashMap<String, String> value = new HashMap<>();
		value.put("foo", "foo");
		JsonGenerator d = mock(JsonGenerator.class);
		doNothing().when(d).writeFieldName(Mockito.<String>any());
		doNothing().when(d).writeString(Mockito.<String>any());
		doNothing().when(d).writeEndObject();
		doNothing().when(d).writeStartObject();
		JsonGeneratorDelegate gen = new JsonGeneratorDelegate(new JsonGeneratorDelegate(d), true);

		// Act
		sanitizingMapSerializer.serialize(value, gen, new Impl());

		// Assert
		verify(d).writeEndObject();
		verify(d).writeFieldName(eq("foo"));
		verify(d).writeStartObject();
		verify(d).writeString(eq("foo"));
	}

	/**
	 * Test
	 * {@link SanitizingMapSerializer#serialize(Map, JsonGenerator, SerializerProvider)}
	 * with {@code Map}, {@code JsonGenerator}, {@code SerializerProvider}.
	 * <p>
	 * Method under test:
	 * {@link SanitizingMapSerializer#serialize(Map, JsonGenerator, SerializerProvider)}
	 */
	@Test
	public void testSerializeWithMapJsonGeneratorSerializerProvider2() throws IOException {
		// Arrange
		SanitizingMapSerializer sanitizingMapSerializer = new SanitizingMapSerializer(
				new String[] { "Patterns", "java.util.Map" });

		HashMap<String, String> value = new HashMap<>();
		value.put("foo", "foo");
		JsonGenerator d = mock(JsonGenerator.class);
		doNothing().when(d).writeFieldName(Mockito.<String>any());
		doNothing().when(d).writeString(Mockito.<String>any());
		doNothing().when(d).writeEndObject();
		doNothing().when(d).writeStartObject();
		JsonGeneratorDelegate gen = new JsonGeneratorDelegate(new JsonGeneratorDelegate(d), true);

		// Act
		sanitizingMapSerializer.serialize(value, gen, new Impl());

		// Assert
		verify(d).writeEndObject();
		verify(d).writeFieldName(eq("foo"));
		verify(d).writeStartObject();
		verify(d).writeString(eq("foo"));
	}

	/**
	 * Test
	 * {@link SanitizingMapSerializer#serialize(Map, JsonGenerator, SerializerProvider)}
	 * with {@code Map}, {@code JsonGenerator}, {@code SerializerProvider}.
	 * <p>
	 * Method under test:
	 * {@link SanitizingMapSerializer#serialize(Map, JsonGenerator, SerializerProvider)}
	 */
	@Test
	public void testSerializeWithMapJsonGeneratorSerializerProvider3() throws IOException {
		// Arrange
		SanitizingMapSerializer sanitizingMapSerializer = new SanitizingMapSerializer(new String[] { "Patterns" });

		HashMap<String, String> value = new HashMap<>();
		value.put("foo", "foo");
		JsonGenerator d = mock(JsonGenerator.class);
		doNothing().when(d).writeString(Mockito.<String>any());
		doNothing().when(d).writeEndObject();
		doNothing().when(d).writeStartObject();
		JsonGeneratorDelegate d2 = new JsonGeneratorDelegate(new JsonGeneratorDelegate(d), true);

		TokenFilter tokenFilter = mock(TokenFilter.class);
		when(tokenFilter.includeString(Mockito.<String>any())).thenReturn(true);
		TokenFilter tokenFilter2 = mock(TokenFilter.class);
		when(tokenFilter2.includeEmptyObject(anyBoolean())).thenReturn(true);
		when(tokenFilter2.includeProperty(Mockito.<String>any())).thenReturn(tokenFilter);
		doNothing().when(tokenFilter2).filterFinishObject();
		TokenFilter tokenFilter3 = mock(TokenFilter.class);
		when(tokenFilter3.filterStartObject()).thenReturn(tokenFilter2);
		TokenFilter f = mock(TokenFilter.class);
		when(f.includeRootValue(anyInt())).thenReturn(tokenFilter3);
		FilteringGeneratorDelegate gen = new FilteringGeneratorDelegate(d2, f, Inclusion.ONLY_INCLUDE_ALL, true);

		// Act
		sanitizingMapSerializer.serialize(value, gen, new Impl());

		// Assert
		verify(d).writeEndObject();
		verify(d).writeStartObject();
		verify(d).writeString(eq("foo"));
		verify(tokenFilter2).filterFinishObject();
		verify(tokenFilter3).filterStartObject();
		verify(tokenFilter2).includeEmptyObject(eq(true));
		verify(tokenFilter2).includeProperty(eq("foo"));
		verify(f).includeRootValue(eq(0));
		verify(tokenFilter).includeString(eq("foo"));
		JsonStreamContext filterContext = gen.getFilterContext();
		assertTrue(filterContext instanceof TokenFilterContext);
		assertEquals(1, filterContext.getEntryCount());
		assertEquals(1, gen.getMatchCount());
		assertTrue(filterContext.hasCurrentIndex());
	}

	/**
	 * Test
	 * {@link SanitizingMapSerializer#serialize(Map, JsonGenerator, SerializerProvider)}
	 * with {@code Map}, {@code JsonGenerator}, {@code SerializerProvider}.
	 * <p>
	 * Method under test:
	 * {@link SanitizingMapSerializer#serialize(Map, JsonGenerator, SerializerProvider)}
	 */
	@Test
	public void testSerializeWithMapJsonGeneratorSerializerProvider4() throws IOException {
		// Arrange
		SanitizingMapSerializer sanitizingMapSerializer = new SanitizingMapSerializer(new String[] { "patterns" });

		HashMap<String, String> value = new HashMap<>();
		value.put("foo", "foo");
		JsonGenerator d = mock(JsonGenerator.class);
		doNothing().when(d).writeEndObject();
		doNothing().when(d).writeStartObject();
		doNothing().when(d).writeString(Mockito.<String>any());
		JsonGeneratorDelegate d2 = new JsonGeneratorDelegate(new JsonGeneratorDelegate(d), true);

		TokenFilter tokenFilter = mock(TokenFilter.class);
		when(tokenFilter.includeEmptyObject(anyBoolean())).thenReturn(true);
		doNothing().when(tokenFilter).filterFinishObject();
		TokenFilter tokenFilter2 = mock(TokenFilter.class);
		when(tokenFilter2.includeString(Mockito.<String>any())).thenReturn(true);
		when(tokenFilter2.filterStartObject()).thenReturn(tokenFilter);
		TokenFilter f = mock(TokenFilter.class);
		when(f.includeRootValue(anyInt())).thenReturn(tokenFilter2);
		JsonGeneratorDelegate d3 = new JsonGeneratorDelegate(
				new FilteringGeneratorDelegate(d2, f, Inclusion.ONLY_INCLUDE_ALL, true), true);

		TokenFilter tokenFilter3 = mock(TokenFilter.class);
		when(tokenFilter3.includeString(Mockito.<String>any())).thenReturn(true);
		TokenFilter tokenFilter4 = mock(TokenFilter.class);
		when(tokenFilter4.includeEmptyObject(anyBoolean())).thenReturn(true);
		when(tokenFilter4.includeProperty(Mockito.<String>any())).thenReturn(tokenFilter3);
		doNothing().when(tokenFilter4).filterFinishObject();
		TokenFilter tokenFilter5 = mock(TokenFilter.class);
		when(tokenFilter5.filterStartObject()).thenReturn(tokenFilter4);
		TokenFilter f2 = mock(TokenFilter.class);
		when(f2.includeRootValue(anyInt())).thenReturn(tokenFilter5);
		FilteringGeneratorDelegate gen = new FilteringGeneratorDelegate(d3, f2, Inclusion.ONLY_INCLUDE_ALL, true);

		// Act
		sanitizingMapSerializer.serialize(value, gen, new Impl());

		// Assert
		verify(d).writeEndObject();
		verify(d).writeStartObject();
		verify(d).writeString(eq("foo"));
		verify(tokenFilter).filterFinishObject();
		verify(tokenFilter4).filterFinishObject();
		verify(tokenFilter2).filterStartObject();
		verify(tokenFilter5).filterStartObject();
		verify(tokenFilter).includeEmptyObject(eq(false));
		verify(tokenFilter4).includeEmptyObject(eq(true));
		verify(tokenFilter4).includeProperty(eq("foo"));
		verify(f, atLeast(1)).includeRootValue(anyInt());
		verify(f2).includeRootValue(eq(0));
		verify(tokenFilter2).includeString(eq("foo"));
		verify(tokenFilter3).includeString(eq("foo"));
		JsonGenerator delegateResult = gen.delegate();
		JsonGenerator delegateResult2 = ((JsonGeneratorDelegate) delegateResult).delegate();
		assertTrue(delegateResult2 instanceof FilteringGeneratorDelegate);
		JsonStreamContext outputContext = delegateResult.getOutputContext();
		assertTrue(outputContext instanceof TokenFilterContext);
		assertTrue(delegateResult instanceof JsonGeneratorDelegate);
		assertEquals(1, outputContext.getCurrentIndex());
		assertEquals(1, ((FilteringGeneratorDelegate) delegateResult2).getMatchCount());
		assertEquals(2, outputContext.getEntryCount());
		assertTrue(outputContext.hasCurrentIndex());
	}

	/**
	 * Test
	 * {@link SanitizingMapSerializer#serialize(Map, JsonGenerator, SerializerProvider)}
	 * with {@code Map}, {@code JsonGenerator}, {@code SerializerProvider}.
	 * <ul>
	 * <li>Given {@code patterns}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link SanitizingMapSerializer#serialize(Map, JsonGenerator, SerializerProvider)}
	 */
	@Test
	public void testSerializeWithMapJsonGeneratorSerializerProvider_givenPatterns() throws IOException {
		// Arrange
		SanitizingMapSerializer sanitizingMapSerializer = new SanitizingMapSerializer(new String[] { "Patterns" });

		HashMap<String, String> value = new HashMap<>();
		value.put("patterns", "foo");
		JsonGenerator d = mock(JsonGenerator.class);
		doNothing().when(d).writeFieldName(Mockito.<String>any());
		doNothing().when(d).writeString(Mockito.<String>any());
		doNothing().when(d).writeEndObject();
		doNothing().when(d).writeStartObject();
		JsonGeneratorDelegate gen = new JsonGeneratorDelegate(new JsonGeneratorDelegate(d), true);

		// Act
		sanitizingMapSerializer.serialize(value, gen, new Impl());

		// Assert
		verify(d).writeEndObject();
		verify(d).writeFieldName(eq("patterns"));
		verify(d).writeStartObject();
		verify(d).writeString(eq("******"));
	}

	/**
	 * Test
	 * {@link SanitizingMapSerializer#serialize(Map, JsonGenerator, SerializerProvider)}
	 * with {@code Map}, {@code JsonGenerator}, {@code SerializerProvider}.
	 * <ul>
	 * <li>Then calls {@link JsonGenerator#writeFieldName(String)}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link SanitizingMapSerializer#serialize(Map, JsonGenerator, SerializerProvider)}
	 */
	@Test
	public void testSerializeWithMapJsonGeneratorSerializerProvider_thenCallsWriteFieldName() throws IOException {
		// Arrange
		SanitizingMapSerializer sanitizingMapSerializer = new SanitizingMapSerializer(new String[] { "Patterns" });

		HashMap<String, String> value = new HashMap<>();
		value.put("foo", "foo");
		JsonGenerator d = mock(JsonGenerator.class);
		doNothing().when(d).writeFieldName(Mockito.<String>any());
		doNothing().when(d).writeString(Mockito.<String>any());
		doNothing().when(d).writeEndObject();
		doNothing().when(d).writeStartObject();
		JsonGeneratorDelegate gen = new JsonGeneratorDelegate(new JsonGeneratorDelegate(d), true);

		// Act
		sanitizingMapSerializer.serialize(value, gen, new Impl());

		// Assert
		verify(d).writeEndObject();
		verify(d).writeFieldName(eq("foo"));
		verify(d).writeStartObject();
		verify(d).writeString(eq("foo"));
	}

	/**
	 * Test
	 * {@link SanitizingMapSerializer#serialize(Map, JsonGenerator, SerializerProvider)}
	 * with {@code Map}, {@code JsonGenerator}, {@code SerializerProvider}.
	 * <ul>
	 * <li>When {@link HashMap#HashMap()}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link SanitizingMapSerializer#serialize(Map, JsonGenerator, SerializerProvider)}
	 */
	@Test
	public void testSerializeWithMapJsonGeneratorSerializerProvider_whenHashMap() throws IOException {
		// Arrange
		SanitizingMapSerializer sanitizingMapSerializer = new SanitizingMapSerializer(new String[] { "Patterns" });
		HashMap<String, String> value = new HashMap<>();
		JsonGenerator d = mock(JsonGenerator.class);
		doNothing().when(d).writeEndObject();
		doNothing().when(d).writeStartObject();
		JsonGeneratorDelegate gen = new JsonGeneratorDelegate(new JsonGeneratorDelegate(d), true);

		// Act
		sanitizingMapSerializer.serialize(value, gen, new Impl());

		// Assert
		verify(d).writeEndObject();
		verify(d).writeStartObject();
	}

	/**
	 * Test
	 * {@link SanitizingMapSerializer#serialize(Map, JsonGenerator, SerializerProvider)}
	 * with {@code Map}, {@code JsonGenerator}, {@code SerializerProvider}.
	 * <ul>
	 * <li>When {@link HashMap#HashMap()} {@code foo} is {@code null}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link SanitizingMapSerializer#serialize(Map, JsonGenerator, SerializerProvider)}
	 */
	@Test
	public void testSerializeWithMapJsonGeneratorSerializerProvider_whenHashMapFooIsNull() throws IOException {
		// Arrange
		SanitizingMapSerializer sanitizingMapSerializer = new SanitizingMapSerializer(new String[] { "Patterns" });

		HashMap<String, String> value = new HashMap<>();
		value.put("foo", null);
		JsonGenerator d = mock(JsonGenerator.class);
		doNothing().when(d).writeFieldName(Mockito.<String>any());
		doNothing().when(d).writeString(Mockito.<String>any());
		doNothing().when(d).writeEndObject();
		doNothing().when(d).writeStartObject();
		JsonGeneratorDelegate gen = new JsonGeneratorDelegate(new JsonGeneratorDelegate(d), true);

		// Act
		sanitizingMapSerializer.serialize(value, gen, new Impl());

		// Assert
		verify(d).writeEndObject();
		verify(d).writeFieldName(eq("foo"));
		verify(d).writeStartObject();
		verify(d).writeString((String) isNull());
	}

}
