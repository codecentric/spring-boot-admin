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
package de.codecentric.boot.admin.zuul;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.cloud.netflix.zuul.web.ZuulController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

/**
 * This Controller behaves like the original ZuulController except it forwads also the OPTIONS
 * request to Servlet instead of handling the request itself.
 *
 * @author Johannes Edmeier
 */
public class OptionsDispatchingZuulController extends ZuulController {

	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		// Delegate to WebContentGenerator for checking and preparing.
		checkRequest(request);
		prepareResponse(response);

		// Execute handleRequestInternal in synchronized block if required.
		if (this.isSynchronizeOnSession()) {
			HttpSession session = request.getSession(false);
			if (session != null) {
				Object mutex = WebUtils.getSessionMutex(session);
				synchronized (mutex) {
					return handleRequestInternal(request, response);
				}
			}
		}

		return handleRequestInternal(request, response);
	}

}
