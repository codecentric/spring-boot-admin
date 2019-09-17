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

import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PasswordGrantTypeContextAttributesMapper implements Function<OAuth2AuthorizeRequest, Map<String, Object>> {

    @Override
    public Map<String, Object> apply(OAuth2AuthorizeRequest authorizeRequest) {
        Map<String, Object> contextAttributes = new HashMap<>();
        String scope = authorizeRequest.getAttribute(OAuth2ParameterNames.SCOPE);
        if (StringUtils.hasText(scope)) {
            contextAttributes.put(OAuth2AuthorizationContext.REQUEST_SCOPE_ATTRIBUTE_NAME,
                    StringUtils.delimitedListToStringArray(scope, " "));
        }
        contextAttributes.put(
                OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME,
                authorizeRequest.getAttribute(OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME));
        contextAttributes.put(
                OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME,
                authorizeRequest.getAttribute(OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME));
        return contextAttributes;
    }

}
