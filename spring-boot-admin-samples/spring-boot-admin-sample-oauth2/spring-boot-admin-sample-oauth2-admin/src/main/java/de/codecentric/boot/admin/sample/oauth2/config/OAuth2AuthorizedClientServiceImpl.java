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

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientId;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mostly clones {@link org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService}, but
 * provides an ability to load authorized client without concrete principal.
 */
// @Service
public class OAuth2AuthorizedClientServiceImpl implements OAuth2AuthorizedClientService {

    private final Map<OAuth2AuthorizedClientId, OAuth2AuthorizedClient> authorizedClients;
    private final ClientRegistrationRepository clientRegistrationRepository;

    public OAuth2AuthorizedClientServiceImpl(ClientRegistrationRepository clientRegistrationRepository) {
        Assert.notNull(clientRegistrationRepository, "clientRegistrationRepository cannot be null");
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.authorizedClients = new ConcurrentHashMap<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId, String principalName) {
        Assert.hasText(clientRegistrationId, "clientRegistrationId cannot be empty");
        Assert.hasText(principalName, "principalName cannot be empty");
        ClientRegistration registration = this.clientRegistrationRepository.findByRegistrationId(clientRegistrationId);
        if (registration == null) {
            return null;
        }
        return (T) this.authorizedClients.get(new OAuth2AuthorizedClientId(clientRegistrationId, principalName));
    }

    @SuppressWarnings("unchecked")
    public <T extends OAuth2AuthorizedClient> Optional<T> loadAuthorizedClientByScope(String scope) {
        return (Optional<T>) authorizedClients.values().stream()
                .filter(client -> client.getClientRegistration().getScopes().contains(scope))
                .findAny();
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal) {
        Assert.notNull(authorizedClient, "authorizedClient cannot be null");
        Assert.notNull(principal, "principal cannot be null");
        this.authorizedClients.put(
                new OAuth2AuthorizedClientId(
                        authorizedClient.getClientRegistration().getRegistrationId(),
                        principal.getName()),
                authorizedClient);
    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId, String principalName) {
        Assert.hasText(clientRegistrationId, "clientRegistrationId cannot be empty");
        Assert.hasText(principalName, "principalName cannot be empty");
        ClientRegistration registration = this.clientRegistrationRepository.findByRegistrationId(clientRegistrationId);
        if (registration != null) {
            this.authorizedClients.remove(new OAuth2AuthorizedClientId(clientRegistrationId, principalName));
        }
    }

}
