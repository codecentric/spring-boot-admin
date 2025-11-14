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

package de.codecentric.boot.admin.server.web.reactive;

import java.lang.reflect.Method;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.result.condition.PatternsRequestCondition;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;

import de.codecentric.boot.admin.server.web.AdminController;
import de.codecentric.boot.admin.server.web.PathUtils;

public class AdminControllerHandlerMapping extends RequestMappingHandlerMapping {

	private final String adminContextPath;

	public AdminControllerHandlerMapping(String adminContextPath) {
		this.adminContextPath = adminContextPath;
	}

	@Override
	protected boolean isHandler(Class<?> beanType) {
		return AnnotatedElementUtils.hasAnnotation(beanType, AdminController.class);
	}

	@Override
	protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
		super.registerHandlerMethod(handler, method, withPrefix(mapping));
	}

	private RequestMappingInfo withPrefix(RequestMappingInfo mapping) {
		if (!StringUtils.hasText(adminContextPath)) {
			return mapping;
		}
		return mapping.mutate().paths(withNewPatterns(mapping.getPatternsCondition())).build();
	}

	private String[] withNewPatterns(PatternsRequestCondition patternsRequestCondition) {
		return patternsRequestCondition.getPatterns().stream()
			.map(pattern -> PathUtils.normalizePath(adminContextPath + pattern))
			.toArray(String[]::new);
	}

}
