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

import java.util.List;

import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.ValueSerializerModifier;

import de.codecentric.boot.admin.server.domain.values.Registration;

public class RegistrationBeanSerializerModifier extends ValueSerializerModifier {

	private final ValueSerializer<Object> metadataSerializer;

	@SuppressWarnings("unchecked")
	public RegistrationBeanSerializerModifier(SanitizingMapSerializer metadataSerializer) {
		this.metadataSerializer = (ValueSerializer<Object>) (ValueSerializer) metadataSerializer;
	}

	@Override
	public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription.Supplier beanDesc,
			List<BeanPropertyWriter> beanProperties) {
		if (!Registration.class.isAssignableFrom(beanDesc.getBeanClass())) {
			return beanProperties;
		}

		beanProperties.stream()
			.filter((beanProperty) -> "metadata".equals(beanProperty.getName()))
			.forEach((beanProperty) -> beanProperty.assignSerializer(metadataSerializer));
		return beanProperties;
	}

}
