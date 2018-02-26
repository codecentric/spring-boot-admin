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

package de.codecentric.boot.admin.client.registration;

import de.codecentric.boot.admin.client.config.InstanceProperties;
import de.codecentric.boot.admin.client.registration.metadata.MetadataContributor;

import javax.servlet.ServletContext;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.web.util.UriComponentsBuilder;

public class ServletApplicationFactory extends DefaultApplicationFactory {
    private final ServletContext servletContext;
    private final ServerProperties.Servlet servlet;
    private final ManagementServerProperties.Servlet managementServlet;

    public ServletApplicationFactory(InstanceProperties instance,
                                     ManagementServerProperties management,
                                     ServerProperties server,
                                     ServletContext servletContext,
                                     PathMappedEndpoints pathMappedEndpoints,
                                     WebEndpointProperties webEndpoint,
                                     MetadataContributor metadataContributor) {
        super(instance, management, server, pathMappedEndpoints, webEndpoint, metadataContributor);
        this.servletContext = servletContext;
        this.servlet = server.getServlet();
        this.managementServlet = management.getServlet();
    }

    @Override
    protected String getManagementBaseUrl() {
        return UriComponentsBuilder.fromHttpUrl(super.getManagementBaseUrl())
                                   .path("/")
                                   .path(getManagementContextPath())
                                   .toUriString();
    }

    protected String getManagementContextPath() {
        return managementServlet.getContextPath();
    }

    @Override
    protected String getServerContextPath() {
        return servletContext.getContextPath();
    }

    @Override
    protected String getDispatcherServletPrefix() {
        return servlet.getServletPrefix();
    }
}
