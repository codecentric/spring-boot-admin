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

package de.codecentric.boot.admin.server.utils.jackson;

import de.codecentric.boot.admin.server.domain.values.Registration;

import java.util.List;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

public class RegistrationBeanSerializerModifier extends BeanSerializerModifier {

    private final JsonSerializer<Object> metadataSerializer;

    @SuppressWarnings("unchecked")
    public RegistrationBeanSerializerModifier(SanitizingMapSerializer metadataSerializer) {
        this.metadataSerializer = (JsonSerializer<Object>) (JsonSerializer) metadataSerializer;
    }

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                     BeanDescription beanDesc,
                                                     List<BeanPropertyWriter> beanProperties) {
        if (!Registration.class.isAssignableFrom(beanDesc.getBeanClass())) {
            return beanProperties;
        }

        beanProperties.stream()
                      .filter(beanProperty -> "metadata".equals(beanProperty.getName()))
                      .forEach(beanProperty -> beanProperty.assignSerializer(metadataSerializer));
        return beanProperties;
    }
}
