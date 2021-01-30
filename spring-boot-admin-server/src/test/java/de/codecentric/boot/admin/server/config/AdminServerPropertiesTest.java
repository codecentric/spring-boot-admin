package de.codecentric.boot.admin.server.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = AdminServerProperties.class)
@TestPropertySource("classpath:server-config-test.properties")
public class AdminServerPropertiesTest {

    @Autowired
    private AdminServerProperties serverConfig;

    @Test
    void testLoadConfigurationProperties() {
    	
    	assertEquals("/admin", serverConfig.getContextPath());

    	assertEquals("admin", serverConfig.getInstanceAuth().getDefaultUserName());
    	assertEquals("topsecret", serverConfig.getInstanceAuth().getDefaultPassword());

    	assertEquals("me", serverConfig.getInstanceAuth().getServiceMap().get("my-service").getUserName());
    	assertEquals("secret", serverConfig.getInstanceAuth().getServiceMap().get("my-service").getUserPassword());
    }
   
}
