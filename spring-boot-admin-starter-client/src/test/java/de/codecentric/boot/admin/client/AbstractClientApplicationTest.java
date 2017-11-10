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

package de.codecentric.boot.admin.client;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;

import static com.github.tomakehurst.wiremock.client.WireMock.created;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.moreThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

public abstract class AbstractClientApplicationTest {
    @ClassRule
    public static WireMockClassRule wireMockClassRule = new WireMockClassRule(Options.DYNAMIC_PORT);

    @Rule
    public WireMockClassRule wireMock = wireMockClassRule;


    public void setUp() throws Exception {
        ResponseDefinitionBuilder response = created().withHeader("Content-Type", "application/json")
                                                      .withHeader("Location", wireMock.url("/instances/abcdef"))
                                                      .withBody("{ \"id\" : \"abcdef\" }");
        wireMock.stubFor(post(urlEqualTo("/instances")).willReturn(response));
    }

    @Test
    public void test_context() throws InterruptedException {
        Thread.sleep(1000L);
        String serviceHost = "http://localhost:" + getServerPort();
        String managementHost = "http://localhost:" + getManagementPort();
        String body = "{ \"name\" : \"Test-Client\"," + //
                      " \"managementUrl\" : \"" + managementHost + "/mgmt\"," + //
                      " \"healthUrl\" : \"" + managementHost + "/mgmt/health\"," + //
                      " \"serviceUrl\" : \"" + serviceHost + "/\", " + //
                      " \"metadata\" : {} }";
        RequestPatternBuilder request = postRequestedFor(urlEqualTo("/instances")).withHeader("Content-Type",
                equalTo("application/json")).withRequestBody(equalToJson(body));
        verify(moreThanOrExactly(1), request);
    }

    protected abstract int getServerPort();

    protected abstract int getManagementPort();
}
