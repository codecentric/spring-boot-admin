/*
 * Copyright 2014-2019 the original author or authors.
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

package de.codecentric.boot.admin.sample.oauth2.web;

import de.codecentric.boot.admin.server.ui.web.UiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Hook to shadow {@link UiController#login()}, as login page is provided by external provider.
 */
@Controller
@RequestMapping("/login")
public class LoginRedirectController {

    @GetMapping
    public void redirectToIndex(HttpServletResponse response) throws IOException {
        response.sendRedirect("/");
    }

    @GetMapping(params = "redirectTo")
    public void redirectTo(@RequestParam String redirectTo, HttpServletResponse response) throws IOException {
        response.sendRedirect(redirectTo);
    }

}
