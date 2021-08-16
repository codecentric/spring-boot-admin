/*
 * Copyright 2014-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.InstanceId;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class JdkPerInstanceCookieStoreTest {

	private static final Instance INSTANCE = Instance.create(InstanceId.of("i"));

	private CookieHandler cookieHandler;

	private JdkPerInstanceCookieStore store;

	@BeforeEach
	void setUp() throws Exception {
		store = spy(new JdkPerInstanceCookieStore());
		cookieHandler = mock(CookieHandler.class);

		when(store.createCookieHandler(INSTANCE.getId())).thenReturn(cookieHandler);
	}

	@Test
	void cookies_should_be_fetched_and_converted_from_store() throws IOException {
		MultiValueMap<String, String> storeMap = new LinkedMultiValueMap<>();
		storeMap.add("Cookie", "name=value");
		storeMap.add("Cookie", "name2=tricky=value");

		final URI uri = URI.create("http://localhost/test");
		when(cookieHandler.get(eq(uri), any())).thenReturn(storeMap);

		final MultiValueMap<String, String> cookieMap = store.get(INSTANCE.getId(), uri, new LinkedMultiValueMap<>());

		assertThat(cookieMap).containsEntry("name", singletonList("value"));
		assertThat(cookieMap).containsEntry("name2", singletonList("tricky=value"));
	}

}
