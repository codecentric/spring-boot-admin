package de.codecentric.boot.admin.config;

import de.codecentric.boot.admin.notify.HipchatNotifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jamie Brown
 */
@Configuration
@ConditionalOnProperty("spring.boot.admin.notify.hipchat.url")
public class HipchatNotifierConfiguration
{
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "spring.boot.admin.notify.hipchat", name = "enabled", matchIfMissing = true)
    @ConfigurationProperties("spring.boot.admin.notify.hipchat")
    public HipchatNotifier hipchatNotifier()
    {
        return new HipchatNotifier();
    }
}
