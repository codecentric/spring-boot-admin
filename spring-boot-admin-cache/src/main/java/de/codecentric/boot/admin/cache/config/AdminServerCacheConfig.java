package de.codecentric.boot.admin.cache.config;


import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IdGenerator;
import com.hazelcast.instance.HazelcastInstanceFactory;
import de.codecentric.boot.admin.cache.impl.HazelcastConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.concurrent.ConcurrentMap;

import static de.codecentric.boot.admin.cache.CacheName.REGISTERED_APPLICATIONS;

/**
 * @author Robert Winkler
 */
//@EnableCaching
@Configuration
@EnableConfigurationProperties()
public class AdminServerCacheConfig {

    @Autowired
    private Environment environment;

    /*
    @Bean
    public CacheManager cacheManager() {
        return new HazelcastCacheManager(hazelcastInstance());
    }
    */

    @Bean(destroyMethod="shutdown")
    public HazelcastInstance hazelcastInstance() {
        return HazelcastInstanceFactory.getOrCreateHazelcastInstance(HazelcastConfigFactory.newHazelcastConfig(cacheProperties(), environment));
    }

    @Bean
    public CacheProperties cacheProperties(){
        return new CacheProperties();
    }

    @Bean
    public ConcurrentMap applicationRegistry(){
        return hazelcastInstance().getMap(REGISTERED_APPLICATIONS.name());
    }

    @Bean
    public IdGenerator applicationIdGenerator(){
        return hazelcastInstance().getIdGenerator("applicationIds");
    }

}
