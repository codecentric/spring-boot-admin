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

package de.codecentric.boot.admin.sample.oauth2;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ResourceServerIntegrationTest {

    private static WireMockServer mockAuthorizationServer = new WireMockServer(options().port(8081));

    @BeforeAll
    static void beforeAll() {
        mockAuthorizationServer.start();
        configureFor(mockAuthorizationServer.port());

        stubFor(post("/uaa/oauth/token_keys").willReturn(
                aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("token-keys.json")));
    }

    @AfterAll
    static void afterAll() {
        mockAuthorizationServer.stop();
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void should_not_be_able_to_access_actuator_endpoint_without_authentication() throws Exception {
        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/actuator/health"));

        // then
        resultActions
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "sba_server")
    void should_be_able_to_access_actuator_endpoint_with_authentication() throws Exception {
        // when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/actuator/health"));

        // then
        resultActions
                .andExpect(status().isOk());
    }

}
