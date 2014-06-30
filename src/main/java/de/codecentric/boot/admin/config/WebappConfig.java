package de.codecentric.boot.admin.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import de.codecentric.boot.admin.controller.RegistryController;
import de.codecentric.boot.admin.service.ApplicationRegistry;

@Configuration
public class WebappConfig extends WebMvcConfigurerAdapter {

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(new MappingJackson2HttpMessageConverter());
	}

	@Bean
	public RegistryController registryController() {
		return new RegistryController();
	}

	@Bean
	public ApplicationRegistry applicationRegistry() {
		return new ApplicationRegistry();
	}

}
