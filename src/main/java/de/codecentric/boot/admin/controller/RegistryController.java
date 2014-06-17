package de.codecentric.boot.admin.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.service.ApplicationRegistry;

@Controller
public class RegistryController {

	@Autowired
	private ApplicationRegistry registry;

	@RequestMapping(value = "/api/applications", method = RequestMethod.POST)
	@ResponseBody
	public void register(@RequestBody Application app) {
		System.out.println("register " + app);
		registry.register(app);
	}

	@RequestMapping(value = "/api/applications", method = RequestMethod.GET)
	@ResponseBody
	public List<Application> applications() {
		System.out.println("get all");
		return registry.getApplications();
	}

}
