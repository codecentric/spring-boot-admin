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

import java.io.Serializable;
import java.nio.charset.Charset;
import java.security.MessageDigest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The domain model for all registered application at the spring boot admin application.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Application implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String id;
	private final String url;
	private final String name;

	@JsonCreator
	public Application(@JsonProperty("url") String url, @JsonProperty("name") String name) {
		this(url.replaceFirst("/+$", ""), name, generateId(url.replaceFirst("/+$", "")));
	}

	protected Application(String url, String name, String id) {
		this.url = url;
		this.name = name;
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getUrl() {
		return url;
	}

	@Override
	public String toString() {
		return "[id=" + id + ", url=" + url + ", name=" + name + "]";
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Application other = (Application) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		}
		else if (!name.equals(other.name)) {
			return false;
		}
		if (url == null) {
			if (other.url != null) {
				return false;
			}
		}
		else if (!url.equals(other.url)) {
			return false;
		}
		return true;
	}

	private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
		'e', 'f' };

	private static String generateId(String url) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte[] bytes = digest.digest(url.getBytes(Charset.forName("UTF-8")));
			return new String(encodeHex(bytes, 0, 8));
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private static char[] encodeHex(byte[] bytes, int offset, int length) {
		char chars[] = new char[length];
		for (int i = 0; i < length; i = i + 2) {
			byte b = bytes[offset + (i / 2)];
			chars[i] = HEX_CHARS[(b >>> 0x4) & 0xf];
			chars[i + 1] = HEX_CHARS[b & 0xf];
		}
		return chars;
	}
}
