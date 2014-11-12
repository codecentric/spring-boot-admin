/*
 * Copyright 2014 the original author or authors.
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
package de.codecentric.boot.admin.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class ApplicationTest {

	@Test
	public void url_id() {
		Application a = new Application("http://localhost:8080/", null);
		Application b = new Application("http://localhost:8080", null);
		assertEquals("same urls must have same id", a.getId(), b.getId());

		Application z = new Application("http://127.0.0.1:8080", null);
		assertFalse("different urls must have diffenrent Id", a.getId().equals(z.getId()));
	}

	public void equals() {
		Application a = new Application("http://localhost:8080/", "FOO");
		Application b = new Application("http://localhost:8080", "FOO");

		assertEquals("same url and same name must be equals", a, b);
		assertEquals("hashcode should be equals", a.hashCode(), b.hashCode());

		Application z = new Application("http://127.0.0.1:8080", "FOO");
		assertFalse("different urls same name must not be equals", a.equals(z));

		Application y = new Application("http://localhost:8080", "BAR");
		assertFalse("same urls different name must not be equals", a.getId().equals(y.getId()));
	}
}
