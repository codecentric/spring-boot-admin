/*
 * Copyright 2014-2017 the original author or authors.
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

package de.codecentric.boot.admin.client.config;

import wiremock.org.apache.http.HttpStatus;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.moreThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class ClientServletApplicationTest {

    private ConfigurableApplicationContext instance;
    private WireMockServer wiremock;

    @Before
    public void setUp() throws Exception {
        wiremock = new WireMockServer(wireMockConfig().dynamicPort());
        wiremock.start();
        configureFor(wiremock.port());
        ResponseDefinitionBuilder response = aResponse().withStatus(HttpStatus.SC_CREATED)
                                                        .withHeader("Content-Type", "application/json")
                                                        .withHeader("Location", "http://localhost:" +
                                                                                wiremock.port() +
                                                                                "/api/applications/abcdef")
                                                        .withBody("{ \"id\" : \"abcdef\" }");
        stubFor(post(urlEqualTo("/api/applications")).willReturn(response));

        instance = SpringApplication.run(TestClientApplication.class, "--spring.main.web-application-type=servlet",
                "--spring.application.name=Test-Client", "--server.port=0", "--management.port=0",
                "--management.context-path=/mgmt",
                "--spring.boot.admin.client.url=http://localhost:" + wiremock.port());
    }

    @Test
    public void test_context() throws InterruptedException {
        Thread.sleep(1000L);
        String serviceHost = "http://localhost:" + instance.getEnvironment().getProperty("local.server.port");
        String managementHost = "http://localhost:" + instance.getEnvironment().getProperty("local.management.port");
        String body = "{ \"name\" : \"Test-Client\"," + //
                      " \"managementUrl\" : \"" + managementHost + "/mgmt/\"," + //
                      " \"healthUrl\" : \"" + managementHost + "/mgmt/health/\"," + //
                      " \"serviceUrl\" : \"" + serviceHost + "/\", " + //
                      " \"metadata\" : {} }";
        RequestPatternBuilder request = postRequestedFor(urlEqualTo("/api/applications")).withHeader("Content-Type",
                equalTo("application/json")).withRequestBody(equalToJson(body));
        verify(moreThanOrExactly(1), request);
    }

    @After
    public void shutdown() {
        instance.close();
        wiremock.stop();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    public static class TestClientApplication {
    }

}

