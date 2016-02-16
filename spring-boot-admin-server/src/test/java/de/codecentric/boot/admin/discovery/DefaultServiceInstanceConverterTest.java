package de.codecentric.boot.admin.discovery;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;

import de.codecentric.boot.admin.model.Application;

public class DefaultServiceInstanceConverterTest {

	@Test
	public void convert() {
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

}
