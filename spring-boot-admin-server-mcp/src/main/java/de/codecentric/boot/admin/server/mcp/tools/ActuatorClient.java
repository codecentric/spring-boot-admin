/*
 * Copyright 2014-2026 the original author or authors.
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

package de.codecentric.boot.admin.server.mcp.tools;

import java.time.Duration;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

/**
 * Shared helper for MCP tool classes that call actuator endpoints on registered Spring
 * Boot applications.
 *
 * <p>
 * Encapsulates the repeated WebClient call patterns and the
 * {@code findByName / switchIfEmpty} lookup so that individual tool classes only contain
 * their {@code @McpTool}-annotated methods and response formatters.
 * </p>
 */
public class ActuatorClient {

	private static final Logger log = LoggerFactory.getLogger(ActuatorClient.class);

	static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE = new ParameterizedTypeReference<>() {
	};

	private final InstanceRepository instanceRepository;

	private final InstanceWebClient instanceWebClient;

	private final Duration timeout;

	/**
	 * Creates a new {@code ActuatorClient} with the given timeout.
	 * @param instanceRepository the repository used to look up registered instances
	 * @param instanceWebClient the client used to call actuator endpoints on instances
	 * @param timeout the default timeout applied to all actuator calls
	 */
	public ActuatorClient(InstanceRepository instanceRepository, InstanceWebClient instanceWebClient,
			Duration timeout) {
		this.instanceRepository = instanceRepository;
		this.instanceWebClient = instanceWebClient;
		this.timeout = timeout;
	}

	/**
	 * Resolves the first registered instance for the given application name, applies
	 * {@code action}, and falls back to a plain-text "not found" message when no instance
	 * is registered.
	 * @param applicationName the registered application name (case-insensitive)
	 * @param action function to execute against the resolved instance
	 * @return the action result, or a plain-text "not found" message
	 */
	public Mono<String> withInstance(String applicationName, Function<Instance, Mono<String>> action) {
		return this.instanceRepository.findByName(applicationName)
			.next()
			.flatMap(action)
			.switchIfEmpty(Mono.just("Application '" + applicationName + "' not found in registry."));
	}

	/**
	 * Resolves the application, performs a GET against {@code managementUrl + urlSuffix},
	 * applies {@code formatter} to the JSON body, and maps any error to a plain-text
	 * message. The endpoint label used in logs and error messages is derived from
	 * {@code urlSuffix} by stripping the leading slash.
	 * @param applicationName the registered application name (case-insensitive)
	 * @param urlSuffix the actuator endpoint path, including the leading slash (e.g.
	 * {@code "/caches"} or {@code "/metrics/jvm.memory.used"})
	 * @param formatter function that converts the application name and parsed response
	 * body into a plain-text result string
	 * @return the formatted result, a plain-text error message, or a "not found" message
	 */
	public Mono<String> query(String applicationName, String urlSuffix,
			BiFunction<String, Map<String, Object>, String> formatter) {
		String label = urlSuffix.startsWith("/") ? urlSuffix.substring(1) : urlSuffix;
		return withInstance(applicationName, (instance) -> {
			String url = instance.getRegistration().getManagementUrl() + urlSuffix;
			return fetch(instance, url, log, label + " for " + applicationName)
				.map((body) -> formatter.apply(applicationName, body))
				.onErrorResume((ex) -> Mono
					.just("Error retrieving " + label + " for " + applicationName + ": " + ex.getMessage()));
		});
	}

	/**
	 * Performs a GET request against the given actuator URL and deserialises the JSON
	 * response body into a {@code Map<String, Object>} using the supplied timeout.
	 * @param instance the target instance
	 * @param url the full actuator endpoint URL
	 * @param timeout the request timeout
	 * @param log the caller's logger
	 * @param errorContext a short description used in the warning log (e.g.
	 * {@code "health for payment-service"})
	 * @return a {@code Mono} of the parsed response body
	 */
	public Mono<Map<String, Object>> fetch(Instance instance, String url, Duration timeout, Logger log,
			String errorContext) {
		return this.instanceWebClient.instance(instance)
			.get()
			.uri(url)
			.retrieve()
			.bodyToMono(MAP_TYPE)
			.timeout(timeout)
			.doOnError((ex) -> log.warn("Failed to get {}", errorContext, ex))
			.onErrorResume(Mono::error);
	}

	/**
	 * Performs a GET request against the given actuator URL and deserialises the JSON
	 * response body into a {@code Map<String, Object>} using the standard timeout.
	 * @param instance the target instance
	 * @param url the full actuator endpoint URL
	 * @param log the caller's logger
	 * @param errorContext a short description used in the warning log
	 * @return a {@code Mono} of the parsed response body
	 */
	public Mono<Map<String, Object>> fetch(Instance instance, String url, Logger log, String errorContext) {
		return fetch(instance, url, this.timeout, log, errorContext);
	}

	/**
	 * Performs a GET request against the given actuator URL and returns the raw response
	 * body as a {@code String}.
	 * @param instance the target instance
	 * @param url the full actuator endpoint URL
	 * @param log the caller's logger
	 * @param errorContext a short description used in the warning log
	 * @return a {@code Mono} of the raw response text; empty string when the body is
	 * absent
	 */
	public Mono<String> fetchText(Instance instance, String url, Logger log, String errorContext) {
		return this.instanceWebClient.instance(instance)
			.get()
			.uri(url)
			.retrieve()
			.bodyToMono(String.class)
			.defaultIfEmpty("")
			.timeout(this.timeout)
			.doOnError((ex) -> log.warn("Failed to get {}", errorContext, ex))
			.onErrorResume(Mono::error);
	}

	/**
	 * Performs a POST request with a JSON string body against the given actuator URL and
	 * returns the raw response body as a {@code String}.
	 * @param instance the target instance
	 * @param url the full actuator endpoint URL
	 * @param jsonBody the JSON string to send as the request body
	 * @param log the caller's logger
	 * @param errorContext a short description used in the warning log
	 * @return a {@code Mono} of the raw response text
	 */
	public Mono<String> post(Instance instance, String url, String jsonBody, Logger log, String errorContext) {
		return this.instanceWebClient.instance(instance)
			.post()
			.uri(url)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(jsonBody)
			.retrieve()
			.bodyToMono(String.class)
			.timeout(this.timeout)
			.doOnError((ex) -> log.warn("Failed to post {}", errorContext, ex))
			.onErrorResume(Mono::error);
	}

	/**
	 * Performs a POST request with a JSON string body against the given actuator URL and
	 * returns the HTTP status code.
	 * @param instance the target instance
	 * @param url the full actuator endpoint URL
	 * @param jsonBody the JSON string to send as the request body; use {@code "{}"} for
	 * an empty body
	 * @param log the caller's logger
	 * @param errorContext a short description used in the warning log
	 * @return a {@code Mono} of the HTTP status code
	 */
	public Mono<Integer> postBodiless(Instance instance, String url, String jsonBody, Logger log, String errorContext) {
		return this.instanceWebClient.instance(instance)
			.post()
			.uri(url)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(jsonBody)
			.retrieve()
			.toBodilessEntity()
			.timeout(this.timeout)
			.map((response) -> response.getStatusCode().value())
			.doOnError((ex) -> log.warn("Failed to post {}", errorContext, ex))
			.onErrorResume(Mono::error);
	}

}
