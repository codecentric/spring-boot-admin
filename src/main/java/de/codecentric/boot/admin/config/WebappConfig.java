package de.codecentric.boot.admin.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebappConfig extends WebMvcConfigurerAdapter {

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(new MappingJackson2HttpMessageConverter());
	}

	@Controller
	static class Routes {
		@RequestMapping({ "/applications", "/applications/{id:\\w+}" })
		public String index() {
			return "/index.html";
		}
	}

}
