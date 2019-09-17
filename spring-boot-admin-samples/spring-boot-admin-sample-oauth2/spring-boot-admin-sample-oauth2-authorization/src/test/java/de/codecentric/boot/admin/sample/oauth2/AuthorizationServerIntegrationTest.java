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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationServerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void should_authenticate_and_return_token() throws Exception {
        // given
        String grantType = "password";
        String username = "subject";
        String password = "password";

        // when
        ResultActions resultActions = mockMvc.perform(post("/oauth/token")
                .param("grant_type", grantType)
                .param("username", username)
                .param("password", password)
                .header("Authorization", "Basic cmVhZGVyOnNlY3JldA=="));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.token_type").value("bearer"))
                .andExpect(jsonPath("$.expires_in").value(both(lessThan(36000)).and(greaterThan(35990))))
                .andExpect(jsonPath("$.scope").value("message:read"))
                .andExpect(jsonPath("$.jti").isNotEmpty());
    }

    @Test
    void should_return_well_known_jwks() throws Exception {
        // when
        ResultActions resultActions = mockMvc.perform(get("/.well-known/jwks.json"));

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys").value(hasSize(1)))
                .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
                .andExpect(jsonPath("$.keys[0].e").value("AQAB"))
                .andExpect(jsonPath("$.keys[0].n").isNotEmpty());
    }

}
