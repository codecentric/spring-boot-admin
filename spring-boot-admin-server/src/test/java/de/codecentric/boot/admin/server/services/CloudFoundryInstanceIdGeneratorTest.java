package de.codecentric.boot.admin.server.services;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CloudFoundryInstanceIdGeneratorTest {
    private CloudFoundryInstanceIdGenerator instance = new CloudFoundryInstanceIdGenerator();

    @Test
    public void test_cloud_foundry_instance_id() {
        Registration registration = Registration.create("foo", "http://health")
            .metadata("cf_application_guid", "549e64cf-a478-423d-9d6d-02d803a028a8")
            .metadata("cf_instance_index", "0")
            .build();
        assertThat(instance.generateId(registration).toString()).containsOnlyOnce("549e64cf-a478-423d-9d6d-02d803a028a8:0");
    }

    @Test
    public void test_health_url_instance_id() {
        Registration registration = Registration.create("foo", "http://health")
            .build();
        assertThat(instance.generateId(registration).toString()).containsOnlyOnce("8f9960c48139");
    }
}
