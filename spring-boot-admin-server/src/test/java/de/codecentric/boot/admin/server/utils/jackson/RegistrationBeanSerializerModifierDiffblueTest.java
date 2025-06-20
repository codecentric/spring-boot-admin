package de.codecentric.boot.admin.server.utils.jackson;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTypeResolverBuilder;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.BaseSettings;
import com.fasterxml.jackson.databind.cfg.ConfigOverrides;
import com.fasterxml.jackson.databind.introspect.BasicClassIntrospector;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector.MixInResolver;
import com.fasterxml.jackson.databind.introspect.DefaultAccessorNamingStrategy;
import com.fasterxml.jackson.databind.introspect.DefaultAccessorNamingStrategy.Provider;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.SimpleMixInResolver;
import com.fasterxml.jackson.databind.jsontype.DefaultBaseTypeLimitingValidator;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.RootNameLookup;
import de.codecentric.boot.admin.server.domain.values.Registration;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.junit.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.http.converter.json.SpringHandlerInstantiator;

public class RegistrationBeanSerializerModifierDiffblueTest {

	/**
	 * Test
	 * {@link RegistrationBeanSerializerModifier#changeProperties(SerializationConfig, BeanDescription, List)}.
	 * <ul>
	 * <li>Given {@code Registration}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link RegistrationBeanSerializerModifier#changeProperties(SerializationConfig, BeanDescription, List)}
	 */
	@Test
	public void testChangeProperties_givenDeCodecentricBootAdminServerDomainValuesRegistration() {
		// Arrange
		RegistrationBeanSerializerModifier registrationBeanSerializerModifier = new RegistrationBeanSerializerModifier(
				new SanitizingMapSerializer(new String[] { "Patterns" }));
		BasicClassIntrospector ci = new BasicClassIntrospector();
		JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
		PropertyNamingStrategy pns = new PropertyNamingStrategy();
		TypeFactory tf = TypeFactory.createDefaultInstance();
		DefaultTypeResolverBuilder typer = new DefaultTypeResolverBuilder(DefaultTyping.JAVA_LANG_OBJECT);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/mm/dd");
		SpringHandlerInstantiator hi = new SpringHandlerInstantiator(new DefaultListableBeanFactory());
		Locale locale = Locale.getDefault();
		TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
		Base64Variant defaultBase64 = Base64Variants.getDefaultVariant();
		DefaultBaseTypeLimitingValidator ptv = new DefaultBaseTypeLimitingValidator();
		BaseSettings base = new BaseSettings(ci, ai, pns, tf, typer, dateFormat, hi, locale, tz, defaultBase64, ptv,
				new Provider());

		StdSubtypeResolver str = new StdSubtypeResolver();
		SimpleMixInResolver mixins = new SimpleMixInResolver(mock(MixInResolver.class));
		RootNameLookup rootNames = new RootNameLookup();
		SerializationConfig config = new SerializationConfig(base, str, mixins, rootNames, new ConfigOverrides());

		BeanDescription beanDesc = mock(BeanDescription.class);
		Class<Registration> forNameResult = Registration.class;
		org.mockito.Mockito.<Class<?>>when(beanDesc.getBeanClass()).thenReturn(forNameResult);

		// Act
		List<BeanPropertyWriter> actualChangePropertiesResult = registrationBeanSerializerModifier
			.changeProperties(config, beanDesc, new ArrayList<>());

		// Assert
		verify(beanDesc).getBeanClass();
		assertTrue(actualChangePropertiesResult.isEmpty());
	}

	/**
	 * Test
	 * {@link RegistrationBeanSerializerModifier#changeProperties(SerializationConfig, BeanDescription, List)}.
	 * <ul>
	 * <li>Given {@code Object}.</li>
	 * </ul>
	 * <p>
	 * Method under test:
	 * {@link RegistrationBeanSerializerModifier#changeProperties(SerializationConfig, BeanDescription, List)}
	 */
	@Test
	public void testChangeProperties_givenJavaLangObject() {
		// Arrange
		RegistrationBeanSerializerModifier registrationBeanSerializerModifier = new RegistrationBeanSerializerModifier(
				new SanitizingMapSerializer(new String[] { "Patterns" }));
		BasicClassIntrospector ci = new BasicClassIntrospector();
		JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
		PropertyNamingStrategy pns = new PropertyNamingStrategy();
		TypeFactory tf = TypeFactory.createDefaultInstance();
		DefaultTypeResolverBuilder typer = new DefaultTypeResolverBuilder(DefaultTyping.JAVA_LANG_OBJECT);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/mm/dd");
		SpringHandlerInstantiator hi = new SpringHandlerInstantiator(new DefaultListableBeanFactory());
		Locale locale = Locale.getDefault();
		TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
		Base64Variant defaultBase64 = Base64Variants.getDefaultVariant();
		DefaultBaseTypeLimitingValidator ptv = new DefaultBaseTypeLimitingValidator();
		BaseSettings base = new BaseSettings(ci, ai, pns, tf, typer, dateFormat, hi, locale, tz, defaultBase64, ptv,
				new Provider());

		StdSubtypeResolver str = new StdSubtypeResolver();
		SimpleMixInResolver mixins = new SimpleMixInResolver(mock(MixInResolver.class));
		RootNameLookup rootNames = new RootNameLookup();
		SerializationConfig config = new SerializationConfig(base, str, mixins, rootNames, new ConfigOverrides());

		BeanDescription beanDesc = mock(BeanDescription.class);
		Class<Object> forNameResult = Object.class;
		org.mockito.Mockito.<Class<?>>when(beanDesc.getBeanClass()).thenReturn(forNameResult);

		// Act
		List<BeanPropertyWriter> actualChangePropertiesResult = registrationBeanSerializerModifier
			.changeProperties(config, beanDesc, new ArrayList<>());

		// Assert
		verify(beanDesc).getBeanClass();
		assertTrue(actualChangePropertiesResult.isEmpty());
	}

}
