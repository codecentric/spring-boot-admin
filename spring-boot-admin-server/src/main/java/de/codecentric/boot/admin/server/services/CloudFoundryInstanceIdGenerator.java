package de.codecentric.boot.admin.server.services;

import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Generates InstanceId "cfApplicationGuid:cfInstanceIndex" for CloudFoundry instance
 * Generates an SHA-1 Hash based on the instance health url for non-CloudFoundry instance
 */
public class CloudFoundryInstanceIdGenerator extends HashingInstanceUrlIdGenerator {

    @Override
    public InstanceId generateId(Registration registration) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            String cfApplicationGuid = registration.getMetadata().get("cf_application_guid");
            String cfInstanceIndex = registration.getMetadata().get("cf_instance_index");

            // CloudFoundry instance set InstanceId as "{cfApplicationGuid}:{cfInstanceIndex}"
            if (StringUtils.hasText(cfApplicationGuid) && StringUtils.hasText(cfInstanceIndex)) {
                return InstanceId.of(String.format("%s:%s", cfApplicationGuid, cfInstanceIndex));
            }
            byte[] bytes = digest.digest(registration.getHealthUrl().getBytes(StandardCharsets.UTF_8));
            return InstanceId.of(new String(encodeHex(bytes, 0, 12)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
