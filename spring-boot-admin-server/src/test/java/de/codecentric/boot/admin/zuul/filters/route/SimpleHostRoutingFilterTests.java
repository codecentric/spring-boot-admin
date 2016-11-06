/*
 * Copyright 2013-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin.zuul.filters.route;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.util.EnvironmentTestUtils.addEnvironment;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.After;
import org.junit.Test;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Copy from sprig cloud netflix which is part of a fix for
 * https://github.com/spring-cloud/spring-cloud-netflix/pull/1372
 *
 * @author Andreas Kluth
 * @author Spencer Gibb
 */
public class SimpleHostRoutingFilterTests {

	private AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

	@After
	public void clear() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void connectionPropertiesAreApplied() {
		addEnvironment(this.context, "zuul.host.maxTotalConnections=100", "zuul.host.maxPerRouteConnections=10");
		setupContext();
		PoolingHttpClientConnectionManager connMgr = getFilter().newConnectionManager();
		assertEquals(100, connMgr.getMaxTotal());
		assertEquals(10, connMgr.getDefaultMaxPerRoute());
	}

	@Test
	public void validateSslHostnamesByDefault() {
		setupContext();
		assertTrue("Hostname verification should be enabled by default",
				getFilter().isSslHostnameValidationEnabled());
	}

	@Test
	public void validationOfSslHostnamesCanBeDisabledViaProperty() {
		addEnvironment(this.context, "zuul.sslHostnameValidationEnabled=false");
		setupContext();
		assertFalse("Hostname verification should be disabled via property",
				getFilter().isSslHostnameValidationEnabled());
	}

	@Test
	public void defaultPropertiesAreApplied() {
		setupContext();
		PoolingHttpClientConnectionManager connMgr = getFilter().newConnectionManager();

		assertEquals(200, connMgr.getMaxTotal());
		assertEquals(20, connMgr.getDefaultMaxPerRoute());
	}

	private void setupContext() {
		this.context.register(PropertyPlaceholderAutoConfiguration.class,
				TestConfiguration.class);
		this.context.refresh();
	}

	private SimpleHostRoutingFilter getFilter() {
		return this.context.getBean(SimpleHostRoutingFilter.class);
	}

	@Configuration
	@EnableConfigurationProperties(ZuulProperties.class)
	protected static class TestConfiguration {
		@Bean
		SimpleHostRoutingFilter simpleHostRoutingFilter(ZuulProperties zuulProperties) {
			return new SimpleHostRoutingFilter(new ProxyRequestHelper(), zuulProperties);
		}
	}
}
