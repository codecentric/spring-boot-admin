package de.codecentric.boot.admin.zuul;

import java.util.LinkedHashMap;

import org.springframework.cloud.netflix.zuul.filters.ProxyRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;

import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.registry.ApplicationRegistry;

public class ApplicationRouteLocator extends ProxyRouteLocator {

	private ApplicationRegistry registry;
	private String prefix;

	public ApplicationRouteLocator(String servletPath, ApplicationRegistry registry, ZuulProperties properties,
			String prefix) {
		super(servletPath, null, properties);
		this.registry = registry;
		this.prefix = prefix;
	}

	@Override
	protected LinkedHashMap<String, ZuulRoute> locateRoutes() {
		LinkedHashMap<String, ZuulRoute> locateRoutes = super.locateRoutes();

		if (registry != null) {
			for (Application application : registry.getApplications()) {
				String key = prefix + "/" + application.getId() + "/*/**";
				locateRoutes.put(key, new ZuulRoute(key, application.getUrl()));
			}
		}

		return locateRoutes;
	}

}
