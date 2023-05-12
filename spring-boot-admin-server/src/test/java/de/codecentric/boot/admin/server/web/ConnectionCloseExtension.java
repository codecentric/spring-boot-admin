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

package de.codecentric.boot.admin.server.web;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;

// Force the connections to be closed...
// see https://github.com/tomakehurst/wiremock/issues/485
class ConnectionCloseExtension extends ResponseTransformer {

	@Override
	public Response transform(Request request, Response response, FileSource files, Parameters parameters) {
		return Response.Builder.like(response)
			.headers(HttpHeaders.copyOf(response.getHeaders()).plus(new HttpHeader("Connection", "Close")))
			.build();
	}

	@Override
	public String getName() {
		return "ConnectionCloseExtension";
	}

}
