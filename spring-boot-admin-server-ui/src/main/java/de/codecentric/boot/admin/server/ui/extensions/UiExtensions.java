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

package de.codecentric.boot.admin.server.ui.extensions;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@lombok.Data
public class UiExtensions implements Iterable<UiExtension> {

	public static final UiExtensions EMPTY = new UiExtensions(Collections.emptyList());

	private final List<UiExtension> extensions;

	public UiExtensions(List<UiExtension> extensions) {
		this.extensions = Collections.unmodifiableList(extensions);
	}

	@Override
	public Iterator<UiExtension> iterator() {
		return this.extensions.iterator();
	}

	public List<UiExtension> getCssExtensions() {
		return this.extensions.stream()
			.filter((e) -> e.getResourcePath().endsWith(".css"))
			.collect(Collectors.toList());
	}

	public List<UiExtension> getJsExtensions() {
		return this.extensions.stream().filter((e) -> e.getResourcePath().endsWith(".js")).collect(Collectors.toList());
	}

}
