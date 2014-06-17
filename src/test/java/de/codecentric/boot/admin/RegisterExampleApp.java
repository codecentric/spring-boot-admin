package de.codecentric.boot.admin;

import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.model.Application;

public class RegisterExampleApp {

	public static void main(String[] args) {
		RestTemplate template = new RestTemplate();
		Application app = new Application();
		app.setId("app");
		app.setUrl("http://localhost:8081");
		template.postForObject("http://localhost:8080/api/applications", app, String.class);
	}

}
