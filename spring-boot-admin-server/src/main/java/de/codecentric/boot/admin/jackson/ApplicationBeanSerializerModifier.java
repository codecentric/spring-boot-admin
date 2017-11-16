package de.codecentric.boot.admin.jackson;

import de.codecentric.boot.admin.model.Application;

import java.util.List;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

public class ApplicationBeanSerializerModifier extends BeanSerializerModifier {

    private final JsonSerializer<Object> metadataSerializer;

    @SuppressWarnings("unchecked")
    public ApplicationBeanSerializerModifier(SanitizingMapSerializer metadataSerializer) {
        this.metadataSerializer = (JsonSerializer<Object>) (JsonSerializer) metadataSerializer;
    }

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        if (!Application.class.isAssignableFrom(beanDesc.getBeanClass())) {
            return beanProperties;
        }

        for (BeanPropertyWriter beanProperty : beanProperties) {
            if ("metadata".equals(beanProperty.getName())) {
                beanProperty.assignSerializer(metadataSerializer);
            }
        }
        return beanProperties;
    }
}
