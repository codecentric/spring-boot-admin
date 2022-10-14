package de.codecentric.boot.admin.server.ui.config;

import de.codecentric.boot.admin.server.config.AdminServerProperties;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;

public class ReactiveAdminServerUiAutoConfigurationAdminContextPathTest extends ReactiveAdminServerUiAutoConfigurationTest{
	@Override
	protected ReactiveWebApplicationContextRunner getContextRunner() {
		 return new ReactiveWebApplicationContextRunner()
			.withPropertyValues("--spring.boot.admin.ui.available-languages=de", "--spring.boot.admin.contextPath=test")
			.withBean(AdminServerProperties.class).withBean(WebFluxProperties.class)
			.withConfiguration(AutoConfigurations.of(AdminServerUiAutoConfiguration.class));
	}
}
