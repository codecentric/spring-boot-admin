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

import java.net.URI;

import org.springframework.util.MultiValueMap;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.InstanceId;

/**
 * A cookie store that stores cookies per {@link Instance}.
 */
public interface PerInstanceCookieStore {

	/**
	 * Gets all the applicable cookies (cookie name =&gt; string representation of cookie)
	 * for the given <code>instanceId</code> and the specified uri in the request header.
	 *
	 * The URI passed as an argument specifies the intended use for the cookies.
	 * @param instanceId identifies the web client instance
	 * @param requestUri a URI representing the intended use for the cookies
	 * @param requestHeaders a Map from request header field names to lists of field
	 * values representing the current request
	 * @return an immutable map from cookie names to text representations of cookies to be
	 * included into a request header
	 */
	MultiValueMap<String, String> get(InstanceId instanceId, URI requestUri,
			MultiValueMap<String, String> requestHeaders);

	/**
	 * Stores all the applicable cookies (examples are response header fields that are
	 * named <code>Set-Cookie</code>) present in the response headers.
	 * @param instanceId identifies the web client instance
	 * @param requestUri an URI where the cookies come from
	 * @param responseHeaders a map from field names to lists of field values representing
	 * the response header fields
	 */
	void put(InstanceId instanceId, URI requestUri, MultiValueMap<String, String> responseHeaders);

	/**
	 * Informs the store that the cookies of the given <code>instanceId</code> could be
	 * removed.
	 * @param instanceId identifies the {@link Instance}
	 */
	void cleanupInstance(InstanceId instanceId);

}
