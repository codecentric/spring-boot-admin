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

package de.codecentric.boot.admin.client.config;

import de.codecentric.boot.admin.client.registration.ApplicationFactory;
import de.codecentric.boot.admin.client.registration.CloudFoundryApplicationFactory;
import de.codecentric.boot.admin.client.registration.DefaultApplicationFactory;
import de.codecentric.boot.admin.client.registration.metadata.CloudFoundryMetadataContributor;

import org.junit.After;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.support.TestPropertySourceUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class SpringBootAdminClientCloudFoundryAutoConfigurationTest {
    private ConfigurableApplicationContext context;

    @After
    public void close() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void non_cloud_platform() {
        load("spring.boot.admin.client.url:http://localhost:8081");
        assertThat(context.getBean(ApplicationFactory.class)).isInstanceOf(DefaultApplicationFactory.class);
        assertThat(context.getBeansOfType(CloudFoundryMetadataContributor.class)).isEmpty();
    }

    @Test
    public void cloudfoundry() {
        String vcap = "VCAP_APPLICATION:{\"application_users\":[]," +
                      "\"application_id\":\"9958288f-9842-4ddc-93dd-1ea3c90634cd\"," +
                      "\"instance_id\":\"bb7935245adf3e650dfb7c58a06e9ece\"," +
                      "\"instance_index\":0,\"version\":\"3464e092-1c13-462e-a47c-807c30318a50\"," +
                      "\"name\":\"foo\",\"uris\":[\"foo.cfapps.io\"]," +
                      "\"started_at\":\"2013-05-29 02:37:59 +0000\"," +
                      "\"started_at_timestamp\":1369795079," +
                      "\"host\":\"0.0.0.0\",\"port\":61034," +
                      "\"limits\":{\"mem\":128,\"disk\":1024,\"fds\":16384}," +
                      "\"version\":\"3464e092-1c13-462e-a47c-807c30318a50\"," +
                      "\"name\":\"dsyerenv\",\"uris\":[\"dsyerenv.cfapps.io\"]," +
                      "\"users\":[],\"start\":\"2013-05-29 02:37:59 +0000\"," +
                      "\"state_timestamp\":1369795079}";

        load("spring.boot.admin.client.url:http://localhost:8081", vcap);
        ApplicationFactory factory = context.getBean(ApplicationFactory.class);
        CloudFoundryMetadataContributor contributor = context.getBean(CloudFoundryMetadataContributor.class);
        assertThat(contributor.getMetadata()).containsEntry("applicationId", "9958288f-9842-4ddc-93dd-1ea3c90634cd")
                                             .containsEntry("instanceId", "0");
        assertThat(factory).isInstanceOf(CloudFoundryApplicationFactory.class);
        assertThat(factory.createApplication().getServiceUrl()).isEqualTo("http://dsyerenv.cfapps.io/");
    }

    private void load(final String... envValues) {
        SpringApplication springApplication = new SpringApplication(TestClientApplication.class);
        springApplication.addListeners(new TestEnvListener(envValues));
        this.context = springApplication.run("--server.port=0");
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    private static class TestEnvListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
        private final String[] values;

        private TestEnvListener(String[] values) {
            this.values = values;
        }

        @Override
        public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(event.getEnvironment(), values);
        }
    }

    @Configuration
    @EnableAutoConfiguration
    static class TestClientApplication {
    }
}
