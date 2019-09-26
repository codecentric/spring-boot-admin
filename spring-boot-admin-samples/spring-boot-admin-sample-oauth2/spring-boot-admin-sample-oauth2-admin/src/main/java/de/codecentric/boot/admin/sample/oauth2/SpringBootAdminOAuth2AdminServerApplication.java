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

import com.nimbusds.jose.util.Base64;
import de.codecentric.boot.admin.sample.oauth2.config.CustomOAuth2AuthorizedClientService;
import de.codecentric.boot.admin.server.config.EnableAdminServer;
import de.codecentric.boot.admin.server.web.client.HttpHeadersProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.util.Optional;

@EnableAdminServer
@SpringBootApplication
public class SpringBootAdminOAuth2AdminServerApplication {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    public static void main(String[] args) {
        SpringApplication.run(SpringBootAdminOAuth2AdminServerApplication.class, args);
    }

    @Bean
    public HttpHeadersProvider provider(CustomOAuth2AuthorizedClientService clientService, Environment environment) {
        return instance -> {
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
        };
    }

    @Bean
    public OAuth2AuthorizedClientService clientService(ClientRegistrationRepository clientRegistrationRepository) {
        return new CustomOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

}
