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

package de.codecentric.boot.admin.server.web.client;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

public class LegacyEndpointConvertersTest {

	private final DataBufferFactory bufferFactory = new DefaultDataBufferFactory();

	private final Jackson2JsonDecoder decoder = new Jackson2JsonDecoder();

	private final ResolvableType type = ResolvableType.forType(new ParameterizedTypeReference<Map<String, Object>>() {
	});

	@Test
	public void should_convert_health() {
		LegacyEndpointConverter converter = LegacyEndpointConverters.health();
		assertThat(converter.canConvert("health")).isTrue();
		assertThat(converter.canConvert("foo")).isFalse();

		Flux<DataBuffer> legacyInput = this.read("health-legacy.json");

		Flux<Object> converted = converter.convert(legacyInput).transform(this::unmarshal);
		Flux<Object> expected = this.read("health-expected.json").transform(this::unmarshal);

		StepVerifier.create(Flux.zip(converted, expected)).assertNext((t) -> assertThat(t.getT1()).isEqualTo(t.getT2()))
				.verifyComplete();
	}

	@Test
	public void should_convert_env() {
		LegacyEndpointConverter converter = LegacyEndpointConverters.env();
		assertThat(converter.canConvert("env")).isTrue();
		assertThat(converter.canConvert("foo")).isFalse();

		Flux<DataBuffer> legacyInput = this.read("env-legacy.json");

		Flux<Object> converted = converter.convert(legacyInput).transform(this::unmarshal);
		Flux<Object> expected = this.read("env-expected.json").transform(this::unmarshal);

		StepVerifier.create(Flux.zip(converted, expected)).assertNext((t) -> assertThat(t.getT1()).isEqualTo(t.getT2()))
				.verifyComplete();
	}

	@Test
	public void should_convert_trace() {
		LegacyEndpointConverter converter = LegacyEndpointConverters.httptrace();
		assertThat(converter.canConvert("httptrace")).isTrue();
		assertThat(converter.canConvert("foo")).isFalse();

		Flux<DataBuffer> legacyInput = this.read("httptrace-legacy.json");

		Flux<Object> converted = converter.convert(legacyInput).transform(this::unmarshal);
		Flux<Object> expected = this.read("httptrace-expected.json").transform(this::unmarshal);

		StepVerifier.create(Flux.zip(converted, expected)).assertNext((t) -> assertThat(t.getT1()).isEqualTo(t.getT2()))
				.verifyComplete();
	}

	@Test
	public void should_convert_threaddump() {
		LegacyEndpointConverter converter = LegacyEndpointConverters.threaddump();
		assertThat(converter.canConvert("threaddump")).isTrue();
		assertThat(converter.canConvert("foo")).isFalse();

		Flux<DataBuffer> legacyInput = this.read("threaddump-legacy.json");

		Flux<Object> converted = converter.convert(legacyInput).transform(this::unmarshal);
		Flux<Object> expected = this.read("threaddump-expected.json").transform(this::unmarshal);

		StepVerifier.create(Flux.zip(converted, expected)).assertNext((t) -> assertThat(t.getT1()).isEqualTo(t.getT2()))
				.verifyComplete();
	}

	@Test
	public void should_convert_liquibase() {
		LegacyEndpointConverter converter = LegacyEndpointConverters.liquibase();
		assertThat(converter.canConvert("liquibase")).isTrue();
		assertThat(converter.canConvert("foo")).isFalse();

		Flux<DataBuffer> legacyInput = this.read("liquibase-legacy.json");

		Flux<Object> converted = converter.convert(legacyInput).transform(this::unmarshal);
		Flux<Object> expected = this.read("liquibase-expected.json").transform(this::unmarshal);

		StepVerifier.create(Flux.zip(converted, expected)).assertNext((t) -> assertThat(t.getT1()).isEqualTo(t.getT2()))
				.verifyComplete();
	}

	@Test
	public void should_convert_flyway() {
		LegacyEndpointConverter converter = LegacyEndpointConverters.flyway();
		assertThat(converter.canConvert("flyway")).isTrue();
		assertThat(converter.canConvert("foo")).isFalse();

		Flux<DataBuffer> legacyInput = this.read("flyway-legacy.json");

		Flux<Object> converted = converter.convert(legacyInput).transform(this::unmarshal);
		Flux<Object> expected = this.read("flyway-expected.json").transform(this::unmarshal);

		StepVerifier.create(Flux.zip(converted, expected)).assertNext((t) -> assertThat(t.getT1()).isEqualTo(t.getT2()))
				.verifyComplete();
	}

	@Test
	public void should_convert_beans() {
		LegacyEndpointConverter converter = LegacyEndpointConverters.beans();
		assertThat(converter.canConvert("beans")).isTrue();
		assertThat(converter.canConvert("foo")).isFalse();

		Flux<DataBuffer> legacyInput = this.read("beans-legacy.json");

		Flux<Object> converted = converter.convert(legacyInput).transform(this::unmarshal);
		Flux<Object> expected = this.read("beans-expected.json").transform(this::unmarshal);

		StepVerifier.create(Flux.zip(converted, expected)).assertNext((t) -> assertThat(t.getT1()).isEqualTo(t.getT2()))
				.verifyComplete();
	}

	@Test
	public void should_convert_configprops() {
		LegacyEndpointConverter converter = LegacyEndpointConverters.configprops();
		assertThat(converter.canConvert("configprops")).isTrue();
		assertThat(converter.canConvert("foo")).isFalse();

		Flux<DataBuffer> legacyInput = this.read("configprops-legacy.json");

		Flux<Object> converted = converter.convert(legacyInput).transform(this::unmarshal);
		Flux<Object> expected = this.read("configprops-expected.json").transform(this::unmarshal);

		StepVerifier.create(Flux.zip(converted, expected)).assertNext((t) -> assertThat(t.getT1()).isEqualTo(t.getT2()))
				.verifyComplete();
	}

	@Test
	public void should_convert_mappings() {
		LegacyEndpointConverter converter = LegacyEndpointConverters.mappings();
		assertThat(converter.canConvert("mappings")).isTrue();
		assertThat(converter.canConvert("foo")).isFalse();

		Flux<DataBuffer> legacyInput = this.read("mappings-legacy.json");

		Flux<Object> converted = converter.convert(legacyInput).transform(this::unmarshal);
		Flux<Object> expected = this.read("mappings-expected.json").transform(this::unmarshal);

		StepVerifier.create(Flux.zip(converted, expected)).assertNext((t) -> assertThat(t.getT1()).isEqualTo(t.getT2()))
				.verifyComplete();
	}

	private Flux<Object> unmarshal(Flux<DataBuffer> buffer) {
		return decoder.decode(buffer, type, null, null);
	}

	private Flux<DataBuffer> read(String resourceName) {
		return DataBufferUtils.readInputStream(
				() -> LegacyEndpointConvertersTest.class.getResourceAsStream(resourceName), bufferFactory, 10);
	}

}
