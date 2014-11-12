package de.codecentric.boot.admin.security.annotation;

/**
 *
 * @author Robert Winkler
 *
 */

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import java.lang.annotation.*;

/**
 * This annotation should be applied to the Spring Boot Admin Server application to enable security.
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Documented
@EnableWebSecurity
public @interface EnableAdminServerSecurity {
}
