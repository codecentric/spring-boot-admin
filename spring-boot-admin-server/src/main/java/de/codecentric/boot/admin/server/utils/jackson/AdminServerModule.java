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

package de.codecentric.boot.admin.server.utils.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;

import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEndpointsDetectedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceInfoChangedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegistrationUpdatedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.BuildVersion;
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.domain.values.Tags;

/**
 * Jackson module for Spring Boot Admin Server. <br>
 * In order to use this module just add this modules into your ObjectMapper configuration.
 * <pre>
 *     ObjectMapper mapper = new ObjectMapper();
 *     mapper.registerModule(new AdminServerModule());
 *     mapper.registerModule(new JavaTimeModule());
 * </pre>
 *
 * @author Stefan Rempfer
 */
public class AdminServerModule extends SimpleModule {

	/**
	 * Construct the module with a pattern for registration metadata keys. The values of
	 * the matched metadata keys will be sanitized before serializing to json.
	 * @param metadataKeyPatterns pattern for metadata keys which should be sanitized
	 */
	public AdminServerModule(String[] metadataKeyPatterns) {
		super(AdminServerModule.class.getName());

		addDeserializer(Registration.class, new RegistrationDeserializer());
		setSerializerModifier(new RegistrationBeanSerializerModifier(new SanitizingMapSerializer(metadataKeyPatterns)));

		setMixInAnnotation(InstanceDeregisteredEvent.class, InstanceDeregisteredEventMixin.class);
		setMixInAnnotation(InstanceEndpointsDetectedEvent.class, InstanceEndpointsDetectedEventMixin.class);
		setMixInAnnotation(InstanceEvent.class, InstanceEventMixin.class);
		setMixInAnnotation(InstanceInfoChangedEvent.class, InstanceInfoChangedEventMixin.class);
		setMixInAnnotation(InstanceRegisteredEvent.class, InstanceRegisteredEventMixin.class);
		setMixInAnnotation(InstanceRegistrationUpdatedEvent.class, InstanceRegistrationUpdatedEventMixin.class);
		setMixInAnnotation(InstanceStatusChangedEvent.class, InstanceStatusChangedEventMixin.class);

		setMixInAnnotation(BuildVersion.class, BuildVersionMixin.class);
		setMixInAnnotation(Endpoint.class, EndpointMixin.class);
		setMixInAnnotation(Endpoints.class, EndpointsMixin.class);
		setMixInAnnotation(Info.class, InfoMixin.class);
		setMixInAnnotation(InstanceId.class, InstanceIdMixin.class);
		setMixInAnnotation(StatusInfo.class, StatusInfoMixin.class);
		setMixInAnnotation(Tags.class, TagsMixin.class);
	}

}
