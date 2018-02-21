package de.codecentric.boot.admin.server.config;

import de.codecentric.boot.admin.server.services.CloudFoundryInstanceIdGenerator;
import de.codecentric.boot.admin.server.services.HashingInstanceUrlIdGenerator;
import de.codecentric.boot.admin.server.services.InstanceIdGenerator;
import de.codecentric.boot.admin.server.web.client.BasicAuthHttpHeaderProvider;
import de.codecentric.boot.admin.server.web.client.CloudFoundryHttpHeaderProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnCloudPlatform(CloudPlatform.CLOUD_FOUNDRY)
@AutoConfigureBefore({AdminServerAutoConfiguration.class})
public class AdminServerCloudFoundryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public InstanceIdGenerator instanceIdGenerator() {
        return new CloudFoundryInstanceIdGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public CloudFoundryHttpHeaderProvider cloudFoundryHttpHeaderProvider() {
        return new CloudFoundryHttpHeaderProvider();
    }
}
