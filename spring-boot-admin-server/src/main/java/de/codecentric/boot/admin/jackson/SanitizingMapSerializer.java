package de.codecentric.boot.admin.jackson;


import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;


public class SanitizingMapSerializer extends StdSerializer<Map<String, String>> {
    private static final long serialVersionUID = 1L;
    private final Pattern[] keysToSanitize;

    @SuppressWarnings("unchecked")
    public SanitizingMapSerializer(String[] patterns) {
        super((Class<Map<String, String>>) (Class<?>) Map.class);
        keysToSanitize = createPatterns(patterns);
    }

    private static Pattern[] createPatterns(String... keys) {
        Pattern[] patterns = new Pattern[keys.length];
        for (int i = 0; i < keys.length; i++) {
            patterns[i] = Pattern.compile(keys[i], Pattern.CASE_INSENSITIVE);
        }
        return patterns;
    }

    @Override
    public void serialize(Map<String, String> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        for (Map.Entry<String, String> entry : value.entrySet()) {
            gen.writeStringField(entry.getKey(), sanitize(entry.getKey(), entry.getValue()));
        }
        gen.writeEndObject();
    }

    private String sanitize(String key, String value) {
        for (Pattern pattern : this.keysToSanitize) {
            if (pattern.matcher(key).matches()) {
                return (value == null ? null : "******");
            }
        }
        return value;
    }
}