/*
 * Copyright 2014-2018 the original author or authors.
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

package de.codecentric.boot.admin.server.web.reactive;

import de.codecentric.boot.admin.server.web.AdminController;
import de.codecentric.boot.admin.server.web.PathUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.result.condition.PatternsRequestCondition;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import static java.util.stream.Collectors.toList;


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
        PatternsRequestCondition patternsCondition = new PatternsRequestCondition(
            withNewPatterns(mapping.getPatternsCondition().getPatterns()));
        return new RequestMappingInfo(patternsCondition, mapping.getMethodsCondition(), mapping.getParamsCondition(),
            mapping.getHeadersCondition(), mapping.getConsumesCondition(), mapping.getProducesCondition(),
            mapping.getCustomCondition());
    }

    private List<PathPattern> withNewPatterns(Set<PathPattern> patterns) {
        return patterns.stream()
                       .map(
                           pattern -> getPathPatternParser().parse(PathUtils.normalizePath(adminContextPath + pattern)))
                       .collect(toList());
    }
}
