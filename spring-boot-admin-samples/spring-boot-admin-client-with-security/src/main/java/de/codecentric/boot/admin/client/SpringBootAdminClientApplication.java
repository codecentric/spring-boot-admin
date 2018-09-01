package de.codecentric.boot.admin.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * This is the entry point for spring boot admin client application.
 * There are lots of customizations available.
 *
 * @author Anand Varkey Philips
 * @version 2.0.2.RELEASE
 */
@Configuration
@EnableAutoConfiguration
public class SpringBootAdminClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootAdminClientApplication.class, args);
    }

}