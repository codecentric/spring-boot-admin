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

package de.codecentric.boot.admin.server.utils.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;

import de.codecentric.boot.admin.server.domain.values.Registration;

/**
 * Jackson module for Spring Boot Admin Server.
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
	}

}
