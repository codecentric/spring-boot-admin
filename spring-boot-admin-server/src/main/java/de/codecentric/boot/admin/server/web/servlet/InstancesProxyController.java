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

import de.codecentric.boot.admin.server.services.InstanceRegistry;
import de.codecentric.boot.admin.server.web.AbstractInstancesProxyController;
import de.codecentric.boot.admin.server.web.AdminController;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Http Handler for proxied requests
 */
@AdminController
public class InstancesProxyController extends AbstractInstancesProxyController {
    private final DataBufferFactory bufferFactory = new DefaultDataBufferFactory();

    public InstancesProxyController(String adminContextPath,
                                    Set<String> ignoredHeaders,
                                    InstanceRegistry registry,
                                    InstanceWebClient instanceWebClient,
                                    Duration readTimeout) {
        super(adminContextPath, ignoredHeaders, registry, instanceWebClient, readTimeout);
    }

    @ResponseBody
    @RequestMapping(path = REQUEST_MAPPING_PATH, method = {RequestMethod.GET, RequestMethod.HEAD, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
    public Mono<Void> endpointProxy(@PathVariable("instanceId") String instanceId,
                                    HttpServletRequest servletRequest,
                                    HttpServletResponse servletResponse) {
        ServerHttpRequest request = new ServletServerHttpRequest(servletRequest);
        ServerHttpResponse response = new ServletServerHttpResponse(servletResponse);

        String pathWithinApplication = servletRequest.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)
                                                     .toString();
        String endpointLocalPath = getEndpointLocalPath(pathWithinApplication);

        URI uri = UriComponentsBuilder.fromPath(endpointLocalPath)
                                      .query(request.getURI().getRawQuery())
                                      .build(true)
                                      .toUri();

        //We need to explicitly block until the headers are recieved.
        //otherwise the FrameworkServlet will add wrong Allow header for OPTIONS request
        ClientResponse clientResponse = super.forward(instanceId, uri, request.getMethod(), request.getHeaders(),
            () -> BodyInserters.fromDataBuffers(
                DataBufferUtils.readInputStream(request::getBody, this.bufferFactory, 16384))).block();

        response.setStatusCode(clientResponse.statusCode());
        response.getHeaders().addAll(filterHeaders(clientResponse.headers().asHttpHeaders()));

        try {
            return DataBufferUtils.write(clientResponse.body(BodyExtractors.toDataBuffers()), response.getBody())
                                  .map(DataBufferUtils::release)
                                  .then();
        } catch (IOException ex) {
            return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, null, ex));
        }
    }
}
