package de.codecentric.boot.admin.jackson;

import de.codecentric.boot.admin.model.Application;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class ApplicationDeserializer extends StdDeserializer<Application> {
    private static final long serialVersionUID = 1L;

    public ApplicationDeserializer() {
        super(Application.class);
    }

    @Override
    public Application deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.readValueAsTree();

        Application.Builder builder = Application.create(node.get("name").asText());

        if (node.has("url")) {
            String url = node.get("url").asText();
            builder.withHealthUrl(url.replaceFirst("/+$", "") + "/health").withManagementUrl(url);
        } else {
            if (node.has("healthUrl")) {
                builder.withHealthUrl(node.get("healthUrl").asText());
            }
            if (node.has("managementUrl")) {
                builder.withManagementUrl(node.get("managementUrl").asText());
            }
            if (node.has("serviceUrl")) {
                builder.withServiceUrl(node.get("serviceUrl").asText());
            }
        }

        if (node.has("metadata")) {
            Iterator<Map.Entry<String, JsonNode>> it = node.get("metadata").fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> entry = it.next();
                builder.addMetadata(entry.getKey(), entry.getValue().asText());
            }
        }
        return builder.build();
    }
}
