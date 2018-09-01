package de.codecentric.boot.admin.server;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * This is the entry point for spring boot admin server application.
 * There are lots of customizations available.
 *
 * @author Anand Varkey Philips
 * @version 2.0.2.RELEASE
 */
@Configuration
@EnableAutoConfiguration
@EnableAdminServer
public class SpringBootAdminServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootAdminServerApplication.class, args);
    }
}