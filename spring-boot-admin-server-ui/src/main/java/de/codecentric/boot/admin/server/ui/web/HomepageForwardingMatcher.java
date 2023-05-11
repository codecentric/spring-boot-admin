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

package de.codecentric.boot.admin.server.ui.web;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

public class HomepageForwardingMatcher<T> implements Predicate<T> {

	private final List<Pattern> includeRoutes;

	private final List<Pattern> excludeRoutes;

	private final Function<T, String> methodAccessor;

	private final Function<T, String> pathAccessor;

	private final Function<T, List<MediaType>> acceptsAccessor;

	public HomepageForwardingMatcher(List<String> includeRoutes, List<String> excludeRoutes,
			Function<T, String> methodAccessor, Function<T, String> pathAccessor,
			Function<T, List<MediaType>> acceptsAccessor) {
		this.includeRoutes = toPatterns(includeRoutes);
		this.excludeRoutes = toPatterns(excludeRoutes);
		this.methodAccessor = methodAccessor;
		this.pathAccessor = pathAccessor;
		this.acceptsAccessor = acceptsAccessor;
	}

	public boolean test(T request) {
		if (!HttpMethod.GET.matches(this.methodAccessor.apply(request))) {
			return false;
		}

		String path = this.pathAccessor.apply(request);
		boolean isExcludedRoute = this.excludeRoutes.stream().anyMatch((p) -> p.matcher(path).matches());
		boolean isIncludedRoute = this.includeRoutes.stream().anyMatch((p) -> p.matcher(path).matches());
		if (isExcludedRoute || !isIncludedRoute) {
			return false;
		}

		return this.acceptsAccessor.apply(request).stream().anyMatch((t) -> t.includes(MediaType.TEXT_HTML));
	}

	private List<Pattern> toPatterns(List<String> routes) {
		return routes.stream()
			.map((r) -> "^" + r.replaceAll("/[*][*]", "(/.*)?").replaceAll("/[*]/", "/[^/]+/") + "$")
			.map(Pattern::compile)
			.collect(Collectors.toList());
	}

}
