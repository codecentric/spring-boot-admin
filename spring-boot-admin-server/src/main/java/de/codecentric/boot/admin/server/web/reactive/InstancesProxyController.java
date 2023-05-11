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

import java.net.URI;
import java.util.Set;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.services.InstanceRegistry;
import de.codecentric.boot.admin.server.web.AdminController;
import de.codecentric.boot.admin.server.web.HttpHeaderFilter;
import de.codecentric.boot.admin.server.web.InstanceWebProxy;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

/**
 * Http Handler for proxied requests
 */
@AdminController
public class InstancesProxyController {

	private static final String INSTANCE_MAPPED_PATH = "/instances/{instanceId}/actuator/**";

	private static final String APPLICATION_MAPPED_PATH = "/applications/{applicationName}/actuator/**";

	private final PathMatcher pathMatcher = new AntPathMatcher();

	private final DataBufferFactory bufferFactory = new DefaultDataBufferFactory();

	private final InstanceRegistry registry;

	private final InstanceWebProxy instanceWebProxy;

	private final String adminContextPath;

	private final HttpHeaderFilter httpHeadersFilter;

	public InstancesProxyController(String adminContextPath, Set<String> ignoredHeaders, InstanceRegistry registry,
			InstanceWebClient instanceWebClient) {
		this.adminContextPath = adminContextPath;
		this.registry = registry;
		this.httpHeadersFilter = new HttpHeaderFilter(ignoredHeaders);
		this.instanceWebProxy = new InstanceWebProxy(instanceWebClient);
	}

	@RequestMapping(path = INSTANCE_MAPPED_PATH, method = { RequestMethod.GET, RequestMethod.HEAD, RequestMethod.POST,
			RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS })
	public Mono<Void> endpointProxy(@PathVariable("instanceId") String instanceId, ServerHttpRequest request,
			ServerHttpResponse response) {
		InstanceWebProxy.ForwardRequest fwdRequest = createForwardRequest(request, request.getBody(),
				this.adminContextPath + INSTANCE_MAPPED_PATH);

		return this.instanceWebProxy.forward(this.registry.getInstance(InstanceId.of(instanceId)), fwdRequest,
				(clientResponse) -> {
					response.setStatusCode(clientResponse.statusCode());
					response.getHeaders()
						.addAll(this.httpHeadersFilter.filterHeaders(clientResponse.headers().asHttpHeaders()));
					return response.writeAndFlushWith(clientResponse.body(BodyExtractors.toDataBuffers()).window(1));
				});
	}

	@ResponseBody
	@RequestMapping(path = APPLICATION_MAPPED_PATH, method = { RequestMethod.GET, RequestMethod.HEAD,
			RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS })
	public Flux<InstanceWebProxy.InstanceResponse> endpointProxy(
			@PathVariable("applicationName") String applicationName, ServerHttpRequest request) {

		Flux<DataBuffer> cachedBody = request.getBody().map((b) -> {
			int readableByteCount = b.readableByteCount();
			DataBuffer dataBuffer = this.bufferFactory.allocateBuffer(readableByteCount);
			dataBuffer.write(b.asByteBuffer());
			DataBufferUtils.release(b);
			return dataBuffer;
		}).cache();

		InstanceWebProxy.ForwardRequest fwdRequest = createForwardRequest(request, cachedBody,
				this.adminContextPath + APPLICATION_MAPPED_PATH);

		return this.instanceWebProxy.forward(this.registry.getInstances(applicationName), fwdRequest);
	}

	private InstanceWebProxy.ForwardRequest createForwardRequest(ServerHttpRequest request, Flux<DataBuffer> cachedBody,
			String pathPattern) {
		String localPath = this.getLocalPath(pathPattern, request);
		URI uri = UriComponentsBuilder.fromPath(localPath).query(request.getURI().getRawQuery()).build(true).toUri();
		return InstanceWebProxy.ForwardRequest.builder()
			.uri(uri)
			.method(request.getMethod())
			.headers(this.httpHeadersFilter.filterHeaders(request.getHeaders()))
			.body(BodyInserters.fromDataBuffers(cachedBody))
			.build();
	}

	private String getLocalPath(String pathPattern, ServerHttpRequest request) {
		String pathWithinApplication = request.getPath().pathWithinApplication().value();
		return this.pathMatcher.extractPathWithinPattern(pathPattern, pathWithinApplication);
	}

}
