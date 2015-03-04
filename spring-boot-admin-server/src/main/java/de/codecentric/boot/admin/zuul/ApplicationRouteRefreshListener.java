package de.codecentric.boot.admin.zuul;

import org.springframework.cloud.netflix.zuul.web.ZuulHandlerMapping;
import org.springframework.context.ApplicationListener;

import de.codecentric.boot.admin.event.ClientApplicationEvent;

public class ApplicationRouteRefreshListener implements ApplicationListener<ClientApplicationEvent> {

	private final ApplicationRouteLocator routeLocator;
	private final ZuulHandlerMapping zuulHandlerMapping;

	public ApplicationRouteRefreshListener(ApplicationRouteLocator routeLocator, ZuulHandlerMapping zuulHandlerMapping) {
		this.routeLocator = routeLocator;
		this.zuulHandlerMapping = zuulHandlerMapping;
	}

	@Override
	public void onApplicationEvent(ClientApplicationEvent event) {
		this.routeLocator.resetRoutes();
		this.zuulHandlerMapping.registerHandlers();
	}

}
