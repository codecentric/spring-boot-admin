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

package de.codecentric.boot.admin.sample.oauth2.config;

import com.nimbusds.jose.util.Base64;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.web.client.HttpHeadersProvider;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthenticationHeaderProvider implements HttpHeadersProvider {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final OAuth2AuthorizedClientServiceImpl clientService;
    private final Environment environment;

    public AuthenticationHeaderProvider(OAuth2AuthorizedClientServiceImpl clientService, Environment environment) {
        this.clientService = clientService;
        this.environment = environment;
    }

    @NonNull
    @Override
    public HttpHeaders getHeaders(@NonNull Instance instance) {
        HttpHeaders headers = new HttpHeaders();

        Optional<OAuth2AuthorizedClient> clientOptional = clientService.loadAuthorizedClientByScope("openid");

        if (clientOptional.isPresent()) {
            OAuth2AccessToken accessToken = clientOptional.get().getAccessToken();
            headers.set(AUTHORIZATION_HEADER,
                    String.join(" ", accessToken.getTokenType().getValue(), accessToken.getTokenValue()));
        } else {
            String username = environment.getRequiredProperty("security.client.healthcheck.username");
            String password = environment.getRequiredProperty("security.client.healthcheck.password");
            headers.set(AUTHORIZATION_HEADER, "Basic " + Base64.encode(username + ":" + password));
        }

        return headers;
    }

}
