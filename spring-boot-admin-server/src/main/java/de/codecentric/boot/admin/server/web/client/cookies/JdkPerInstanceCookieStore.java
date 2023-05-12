/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.web.client.cookies;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.web.client.exception.InstanceWebClientException;

/**
 * A {@link PerInstanceCookieStore} that is using per
 * {@link de.codecentric.boot.admin.server.domain.entities.Instance} a {@link CookieStore}
 * from JDK as back end store.
 *
 * As <code>Cookie2</code> cookies are
 * <a href= "https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Cookie2">not
 * recommended any more</a> only
 * <a href= "https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Cookie">Cookie</a>
 * cookies are supported.
 */
public class JdkPerInstanceCookieStore implements PerInstanceCookieStore {

	private static final String REQ_COOKIE_HEADER_KEY = "Cookie";

	/**
	 * Holds a cookie store per
	 * {@link de.codecentric.boot.admin.server.domain.entities.Instance}.
	 */
	private final Map<InstanceId, CookieHandler> cookieHandlerRegistry = new ConcurrentHashMap<>();

	private final CookiePolicy cookiePolicy;

	/**
	 * Creates a new {@link JdkPerInstanceCookieStore}.
	 *
	 * Same as
	 *
	 * <pre>
	 * new JdkPerInstanceCookieStore(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
	 * </pre>
	 */
	public JdkPerInstanceCookieStore() {
		this(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
	}

	/**
	 * Creates a new {@link JdkPerInstanceCookieStore} using the given
	 * {@link CookiePolicy}.
	 * @param cookiePolicy policy used by created {@link CookieStore}s
	 */
	public JdkPerInstanceCookieStore(final CookiePolicy cookiePolicy) {
		Assert.notNull(cookiePolicy, "'cookiePolicy' must not be null");
		this.cookiePolicy = cookiePolicy;
	}

	@Override
	public MultiValueMap<String, String> get(final InstanceId instanceId, final URI requestUri,
			final MultiValueMap<String, String> requestHeaders) {
		try {
			final List<String> rawCookies = getCookieHandler(instanceId).get(requestUri, requestHeaders)
				.get(REQ_COOKIE_HEADER_KEY);

			// split each rawCookie at first '=' into name/cookieValue and
			// return as MultiValueMap
			return Optional.ofNullable(rawCookies)
				.map((rcList) -> rcList.stream()
					.map((rc) -> rc.split("=", 2))
					.collect(LinkedMultiValueMap<String, String>::new, (map, nv) -> map.add(nv[0], nv[1]),
							(m1, m2) -> m1.addAll(m2)))
				.orElseGet(LinkedMultiValueMap<String, String>::new);
		}
		catch (IOException ioe) {
			throw new InstanceWebClientException("Could not get cookies from store.", ioe);
		}
	}

	@Override
	public void put(final InstanceId instanceId, final URI requestUrl, final MultiValueMap<String, String> headers) {
		try {
			getCookieHandler(instanceId).put(requestUrl, headers);
		}
		catch (IOException ioe) {
			throw new InstanceWebClientException("Could not set cookies to store.", ioe);
		}
	}

	@Override
	public void cleanupInstance(final InstanceId instanceId) {
		cookieHandlerRegistry.computeIfPresent(instanceId, (id, ch) -> null);
	}

	/**
	 * Returns the stored {@link CookieHandler} for the identified
	 * {@link de.codecentric.boot.admin.server.domain.entities.Instance} or creates a new
	 * one, stores and returns it.
	 * @param instanceId identifies the
	 * {@link de.codecentric.boot.admin.server.domain.entities.Instance}
	 * @return {@link CookieHandler} responsible for the given <code>instanceId</code>
	 */
	protected CookieHandler getCookieHandler(final InstanceId instanceId) {
		return cookieHandlerRegistry.computeIfAbsent(instanceId, this::createCookieHandler);
	}

	protected CookieHandler createCookieHandler(final InstanceId instanceId) {
		return new CookieManager(null, cookiePolicy);
	}

}
