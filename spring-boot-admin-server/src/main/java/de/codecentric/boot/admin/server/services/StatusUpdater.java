/*
 * Copyright 2014-2020 the original author or authors.
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

package de.codecentric.boot.admin.server.services;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

import static de.codecentric.boot.admin.server.utils.MediaType.ACTUATOR_V2_MEDIATYPE;
import static java.util.Collections.emptyMap;

/**
 * The StatusUpdater is responsible for updating the status of all or a single application
 * querying the healthUrl.
 *
 * @author Johannes Edmeier
 */
public class StatusUpdater {

	private static final Logger log = LoggerFactory.getLogger(StatusUpdater.class);

	private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE = new ParameterizedTypeReference<Map<String, Object>>() {
	};

	private final InstanceRepository repository;

	private final InstanceWebClient instanceWebClient;

	public StatusUpdater(InstanceRepository repository, InstanceWebClient instanceWebClient) {
		this.repository = repository;
		this.instanceWebClient = instanceWebClient;
	}

	public Mono<Void> updateStatus(InstanceId id) {
		return this.repository.computeIfPresent(id, (key, instance) -> this.doUpdateStatus(instance)).then();

	}

	protected Mono<Instance> doUpdateStatus(Instance instance) {
		if (!instance.isRegistered()) {
			return Mono.empty();
		}

		log.debug("Update status for {}", instance);
		return this.instanceWebClient.instance(instance).get().uri(Endpoint.HEALTH)
				.exchangeToMono(this::convertStatusInfo).log(log.getName(), Level.FINEST)
				.retryWhen(Retry.max(5)
						.doBeforeRetry((s) -> log.info("RETRYING: Couldn't retrieve status for {}", instance)))
				.doOnError((ex) -> logError(instance, ex)).onErrorResume(this::handleError)
				.map(instance::withStatusInfo);
	}

	protected Mono<StatusInfo> convertStatusInfo(ClientResponse response) {
		Boolean hasCompatibleContentType = response.headers().contentType().map(
				(mt) -> mt.isCompatibleWith(MediaType.APPLICATION_JSON) || mt.isCompatibleWith(ACTUATOR_V2_MEDIATYPE))
				.orElse(false);

		if (hasCompatibleContentType) {
			return response.bodyToMono(RESPONSE_TYPE).map((body) -> {
				if (body.get("status") instanceof String) {
					return StatusInfo.from(body);
				}
				return getStatusInfoFromStatus(response.statusCode(), body);
			}).switchIfEmpty(Mono.fromSupplier(() -> this.getStatusInfoFromStatus(response.statusCode(), emptyMap())));
		}
		return response.releaseBody().then(Mono.just(this.getStatusInfoFromStatus(response.statusCode(), emptyMap())));
	}

	@SuppressWarnings("unchecked")
	protected StatusInfo getStatusInfoFromStatus(HttpStatus httpStatus, Map<String, ?> body) {
		if (httpStatus.is2xxSuccessful()) {
			return StatusInfo.ofUp();
		}

		throw new StatusInfoNotOkException(httpStatus, body);
	}

	protected Mono<StatusInfo> handleError(Throwable ex) {
		if (ex.getCause() instanceof StatusInfoNotOkException) {
			StatusInfoNotOkException statusInfoNotOkException = (StatusInfoNotOkException) ex.getCause();
			Map<String, Object> details = statusInfoNotOkException.getDetails();
			return Mono.just(StatusInfo.ofDown(details));
		}

		Map<String, Object> details = new HashMap<>();
		details.put("exception", ex.getClass().getName());
		details.put("message", ex.getMessage());
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
