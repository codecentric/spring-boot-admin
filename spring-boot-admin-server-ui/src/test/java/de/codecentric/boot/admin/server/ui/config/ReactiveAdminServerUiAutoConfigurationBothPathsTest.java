package de.codecentric.boot.admin.server.ui.config;

import de.codecentric.boot.admin.server.config.AdminServerProperties;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;

public class ReactiveAdminServerUiAutoConfigurationBothPathsTest extends ReactiveAdminServerUiAutoConfigurationTest {

	@Override
	protected ReactiveWebApplicationContextRunner getContextRunner() {
		return new ReactiveWebApplicationContextRunner()
				.withPropertyValues("--spring.boot.admin.ui.available-languages=de",
						"--spring.boot.admin.contextPath=different", "--spring.webflux.base-path=test")
				.withBean(AdminServerProperties.class).withBean(WebFluxProperties.class)
				.withConfiguration(AutoConfigurations.of(AdminServerUiAutoConfiguration.class));
	}

}
