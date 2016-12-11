package de.codecentric.boot.admin.discovery;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;

import de.codecentric.boot.admin.model.Application;

public class DefaultServiceInstanceConverterTest {

	@Test
	public void test_convert_with_defaults() {
		ServiceInstance service = new DefaultServiceInstance("test", "localhost", 80, false);
		Application application = new DefaultServiceInstanceConverter().convert(service);

		assertThat(application.getId(), nullValue());
		assertThat(application.getName(), is("test"));
		assertThat(application.getServiceUrl(), is("http://localhost:80"));
		assertThat(application.getManagementUrl(), is("http://localhost:80"));
		assertThat(application.getHealthUrl(), is("http://localhost:80/health"));
	}

	@Test
	public void test_convert_with_custom_defaults() {
		DefaultServiceInstanceConverter converter = new DefaultServiceInstanceConverter();
		converter.setHealthEndpointPath("ping");
		converter.setManagementContextPath("mgmt");

		ServiceInstance service = new DefaultServiceInstance("test", "localhost", 80, false);
		Application application = converter.convert(service);

		assertThat(application.getId(), nullValue());
		assertThat(application.getName(), is("test"));
		assertThat(application.getServiceUrl(), is("http://localhost:80"));
		assertThat(application.getManagementUrl(), is("http://localhost:80/mgmt"));
		assertThat(application.getHealthUrl(), is("http://localhost:80/mgmt/ping"));
	}

	@Test
	public void test_convert_with_metadata() {
		ServiceInstance service = new DefaultServiceInstance("test", "localhost", 80, false);
		Map<String, String> metadata = new HashMap<>();
		metadata.put("health.path", "ping");
		metadata.put("management.context-path", "mgmt");
		metadata.put("management.port", "1234");
		service.getMetadata().putAll(metadata);

		Application application = new DefaultServiceInstanceConverter().convert(service);

		assertThat(application.getId(), nullValue());
		assertThat(application.getName(), is("test"));
		assertThat(application.getServiceUrl(), is("http://localhost:80"));
		assertThat(application.getManagementUrl(), is("http://localhost:1234/mgmt"));
		assertThat(application.getHealthUrl(), is("http://localhost:1234/mgmt/ping"));
		assertThat(application.getMetadata(), is(metadata));
	}

}
