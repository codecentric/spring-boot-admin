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

package de.codecentric.boot.admin.server.services.endpoints;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.groupingBy;

public class ProbeEndpointsStrategy implements EndpointDetectionStrategy {

	private static final Logger log = LoggerFactory.getLogger(ProbeEndpointsStrategy.class);

	private final List<EndpointDefinition> endpoints;

	private final InstanceWebClient instanceWebClient;

	public ProbeEndpointsStrategy(InstanceWebClient instanceWebClient, String[] endpoints) {
		Assert.notNull(endpoints, "'endpoints' must not be null.");
		Assert.noNullElements(endpoints, "'endpoints' must not contain null.");
		this.endpoints = Arrays.stream(endpoints).map(EndpointDefinition::create).collect(Collectors.toList());
		this.instanceWebClient = instanceWebClient;
	}

	@Override
	public Mono<Endpoints> detectEndpoints(Instance instance) {
		if (instance.getRegistration().getManagementUrl() == null) {
			log.debug("Endpoint probe for instance {} omitted. No management-url registered.", instance.getId());
			return Mono.empty();
		}

		return Flux.fromIterable(this.endpoints)
			.flatMap((endpoint) -> detectEndpoint(instance, endpoint))
			.collectList()
			.flatMap(this::convert);
	}

	protected Mono<DetectedEndpoint> detectEndpoint(Instance instance, EndpointDefinition endpoint) {
		URI uri = UriComponentsBuilder.fromUriString(instance.getRegistration().getManagementUrl())
			.path("/")
			.path(endpoint.getPath())
			.build()
			.toUri();
		return this.instanceWebClient.instance(instance)
			.options()
			.uri(uri)
			.exchangeToMono(this.convert(instance.getId(), endpoint, uri))
			.onErrorResume((e) -> {
				log.warn("Endpoint probe for instance {} on endpoint '{}' failed: {}", instance.getId(), uri,
						e.getMessage());
				log.debug("Endpoint probe for instance {} on endpoint '{}' failed.", instance.getId(), uri, e);
				return Mono.empty();
			});
	}

	protected Function<ClientResponse, Mono<DetectedEndpoint>> convert(InstanceId instanceId,
			EndpointDefinition endpointDefinition, URI uri) {
		return (response) -> {
			Mono<DetectedEndpoint> endpoint = Mono.empty();
			if (response.statusCode().is2xxSuccessful()) {
				endpoint = Mono.just(DetectedEndpoint.of(endpointDefinition, uri.toString()));
				log.debug("Endpoint probe for instance {} on endpoint '{}' successful.", instanceId, uri);
			}
			else {
				log.debug("Endpoint probe for instance {} on endpoint '{}' failed with status {}.", instanceId, uri,
						response.statusCode().value());
			}
			return response.releaseBody().then(endpoint);
		};
	}

	protected Mono<Endpoints> convert(List<DetectedEndpoint> endpoints) {
		if (endpoints.isEmpty()) {
			return Mono.empty();
		}

		Map<String, List<DetectedEndpoint>> endpointsById = endpoints.stream()
			.collect(groupingBy((e) -> e.getDefinition().getId()));
		List<Endpoint> result = endpointsById.values().stream().map((endpointList) -> {
			endpointList.sort(comparingInt((e) -> this.endpoints.indexOf(e.getDefinition())));
			if (endpointList.size() > 1) {
				log.warn("Duplicate endpoints for id '{}' detected. Omitting: {}",
						endpointList.get(0).getDefinition().getId(), endpointList.subList(1, endpointList.size()));
			}
			return endpointList.get(0).getEndpoint();
		}).collect(Collectors.toList());
		return Mono.just(Endpoints.of(result));
	}

	@Data
	protected static class DetectedEndpoint {

		private final EndpointDefinition definition;

		private final Endpoint endpoint;

		private static DetectedEndpoint of(EndpointDefinition endpointDefinition, String url) {
			return new DetectedEndpoint(endpointDefinition, Endpoint.of(endpointDefinition.getId(), url));
		}

	}

	@Data
	protected static class EndpointDefinition {

		private final String id;

		private final String path;

		private static EndpointDefinition create(String idWithPath) {
			int idxDelimiter = idWithPath.indexOf(':');
			if (idxDelimiter < 0) {
				return new EndpointDefinition(idWithPath, idWithPath);
			}
			else {
				return new EndpointDefinition(idWithPath.substring(0, idxDelimiter),
						idWithPath.substring(idxDelimiter + 1));
			}
		}

	}

}
