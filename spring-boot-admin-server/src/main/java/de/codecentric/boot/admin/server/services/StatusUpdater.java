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

package de.codecentric.boot.admin.server.services;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

import static java.util.Collections.emptyMap;

/**
 * The StatusUpdater is responsible for updating the status of all or a single application
 * querying the healthUrl.
 *
 * @author Johannes Edmeier
 */
@Slf4j
@RequiredArgsConstructor
public class StatusUpdater {

	private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE = new ParameterizedTypeReference<>() {
	};

	private final InstanceRepository repository;

	private final InstanceWebClient instanceWebClient;

	private final ApiMediaTypeHandler apiMediaTypeHandler;

	private Duration timeout = Duration.ofSeconds(10);

	public StatusUpdater timeout(Duration timeout) {
		this.timeout = timeout;
		return this;
	}

	public Mono<Void> updateStatus(InstanceId id) {
		return this.repository.computeIfPresent(id, (key, instance) -> this.doUpdateStatus(instance)).then();
	}

	protected Mono<Instance> doUpdateStatus(Instance instance) {
		if (!instance.isRegistered()) {
			return Mono.empty();
		}

		log.debug("Update status for {}", instance);
		return this.instanceWebClient.instance(instance)
			.get()
			.uri(Endpoint.HEALTH)
			.exchangeToMono(this::convertStatusInfo)
			.log(log.getName(), Level.FINEST)
			.timeout(getTimeoutWithMargin())
			.doOnError((ex) -> logError(instance, ex))
			.onErrorResume(this::handleError)
			.map(instance::withStatusInfo);
	}

	/*
	 * return a timeout less than the given one to prevent backdrops in concurrent get
	 * request. This prevents flakyness of health checks.
	 */
	private Duration getTimeoutWithMargin() {
		return this.timeout.minusSeconds(1).abs();
	}

	protected Mono<StatusInfo> convertStatusInfo(ClientResponse response) {
		boolean hasCompatibleContentType = response.headers()
			.contentType()
			.filter((mt) -> mt.isCompatibleWith(MediaType.APPLICATION_JSON)
					|| this.apiMediaTypeHandler.isApiMediaType(mt))
			.isPresent();

		StatusInfo statusInfoFromStatus = this.getStatusInfoFromStatus(response.statusCode(), emptyMap());
		if (hasCompatibleContentType) {
			return response.bodyToMono(RESPONSE_TYPE).map((body) -> {
				if (body.get("status") instanceof String) {
					return StatusInfo.from(body);
				}
				return getStatusInfoFromStatus(response.statusCode(), body);
			}).defaultIfEmpty(statusInfoFromStatus);
		}
		return response.releaseBody().then(Mono.just(statusInfoFromStatus));
	}

	@SuppressWarnings("unchecked")
	protected StatusInfo getStatusInfoFromStatus(HttpStatusCode httpStatus, Map<String, ?> body) {
		if (httpStatus.is2xxSuccessful()) {
			return StatusInfo.ofUp();
		}
		Map<String, Object> details = new LinkedHashMap<>();
		details.put("status", httpStatus.value());
		details.put("error", Objects.requireNonNull(HttpStatus.resolve(httpStatus.value())).getReasonPhrase());
		if (body.get("details") instanceof Map) {
			details.putAll((Map<? extends String, ?>) body.get("details"));
		}
		else {
			details.putAll(body);
		}
		return StatusInfo.ofDown(details);
	}

	protected Mono<StatusInfo> handleError(Throwable ex) {
		Map<String, Object> details = new HashMap<>();
		details.put("message", ex.getMessage());
		details.put("exception", ex.getClass().getName());
		return Mono.just(StatusInfo.ofOffline(details));
	}

	protected void logError(Instance instance, Throwable ex) {
		if (instance.getStatusInfo().isOffline()) {
			log.debug("Couldn't retrieve status for {}", instance, ex);
		}
		else {
			log.info("Couldn't retrieve status for {}", instance, ex);
		}
	}

}
