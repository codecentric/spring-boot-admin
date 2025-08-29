/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.domain.values;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegistrationTest implements WithAssertions {

	@Test
	void invariants() {
		assertThatThrownBy(() -> Registration.create(null, null).build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("'name' must not be empty.");

		assertThatThrownBy(() -> Registration.create("test", null).build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("'healthUrl' must not be empty.");

		assertThatThrownBy(() -> Registration.create("test", "invalid").build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("'healthUrl' is not valid: invalid");

		assertThatThrownBy(() -> Registration.create("test", "https://example.com").managementUrl("invalid").build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("'managementUrl' is not valid: invalid");

		assertThatThrownBy(() -> Registration.create("test", "https://example.com").serviceUrl("invalid").build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("'serviceUrl' is not valid: invalid");
	}

	@Test
	void returnsNull_whenServiceUrlIsNull_evenIfMetadataHasOverride() {
		Registration reg = Registration.builder()
			.name("app")
			.healthUrl("https://example.com/actuator/health")
			.metadata("service-url", "https://override.example.com")
			.build();

		assertThat(reg.getServiceUrl()).isNull();
	}

	@Test
	void usesMetadataOverride_whenValidAbsoluteUrlProvided() {
		Registration reg = Registration.create("app", "https://example.com/actuator/health")
			.serviceUrl("https://base.example.com")
			.metadata("service-url", "https://override.example.com")
			.build();

		assertThat(reg.getServiceUrl()).isEqualTo("https://override.example.com");
	}

	@Test
	void fallsBackToOriginal_whenMetadataOverrideIsInvalidSyntax() {
		Registration reg = Registration.create("app", "https://example.com/actuator/health")
			.serviceUrl("https://base.example.com")
			.metadata("service-url", "http://exa mple.com") // invalide URI (Leerzeichen)
			.build();

		assertThat(reg.getServiceUrl()).isEqualTo("https://base.example.com");
	}

	@Test
	void keepsOriginal_whenNoMetadataOverridePresent() {
		Registration reg = Registration.create("app", "https://example.com/actuator/health")
			.serviceUrl("https://base.example.com")
			.metadata("other", "value")
			.build();

		assertThat(reg.getServiceUrl()).isEqualTo("https://base.example.com");
	}

	@Test
	void acceptsEmptyStringFromMetadata_evenThoughItIsRelative() {
		Registration reg = Registration.create("app", "https://example.com/actuator/health")
			.serviceUrl("https://base.example.com")
			.metadata("service-url", "")
			.build();

		// new URI("") erzeugt eine relative, aber syntaktisch valide URI -> Methode gibt
		// "" zurück
		assertThat(reg.getServiceUrl()).isEqualTo("");
	}

}
