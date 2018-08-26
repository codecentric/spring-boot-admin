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

package de.codecentric.boot.admin.server.web.servlet;

import de.codecentric.boot.admin.server.web.HomepageForwardingMatcher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;

/**
 * An interceptor that forwards matching HTML requests to the home page.
 * Further routing will is done in the browser by VueRouter using history mode.
 * @see <a href="https://router.vuejs.org/guide/essentials/history-mode.html">VueRouter history mode</a>
 */
public class HomepageForwardingHandlerInterceptor extends HandlerInterceptorAdapter {
    private final HomepageForwardingMatcher homepageForwardingMatcher;

    public HomepageForwardingHandlerInterceptor(HomepageForwardingMatcher homepageForwardingMatcher) {
        this.homepageForwardingMatcher = homepageForwardingMatcher;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (match(request)) {
            request.getRequestDispatcher(homepageForwardingMatcher.getHomepagePath()).forward(request, response);
            return false;
        }

        // Normal processing
        return true;
    }

    private boolean match(HttpServletRequest request) {
        return homepageForwardingMatcher.match(HttpMethod.valueOf(request.getMethod()),
            URI.create(request.getRequestURI()).getPath(),
            MediaType.parseMediaTypes(request.getHeader(HttpHeaders.ACCEPT)));
    }
}
