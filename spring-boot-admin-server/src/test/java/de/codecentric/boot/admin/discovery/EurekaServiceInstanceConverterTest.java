package de.codecentric.boot.admin.discovery;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.Test;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient.EurekaServiceInstance;

import com.netflix.appinfo.InstanceInfo;

import de.codecentric.boot.admin.model.Application;

public class EurekaServiceInstanceConverterTest {

	@Test
	public void convert_secure() {
		InstanceInfo instanceInfo = mock(InstanceInfo.class);
		when(instanceInfo.getSecureHealthCheckUrl()).thenReturn("");
		when(instanceInfo.getHealthCheckUrl()).thenReturn("http://localhost:80/mgmt/ping");
		EurekaServiceInstance service = mock(EurekaServiceInstance.class);
		when(service.getInstanceInfo()).thenReturn(instanceInfo);
		when(service.getUri()).thenReturn(URI.create("http://localhost:80"));
		when(service.getServiceId()).thenReturn("test");
		when(service.getMetadata()).thenReturn(singletonMap("management.context-path", "/mgmt"));

		Application application = new EurekaServiceInstanceConverter().convert(service);

		assertThat(application.getId(), nullValue());
		assertThat(application.getName(), is("test"));
		assertThat(application.getServiceUrl(), is("http://localhost:80"));
		assertThat(application.getManagementUrl(), is("http://localhost:80/mgmt"));
		assertThat(application.getHealthUrl(), is("http://localhost:80/mgmt/ping"));
	}

	@Test
	public void convert_missing_mgmtpath() {
		InstanceInfo instanceInfo = mock(InstanceInfo.class);
		when(instanceInfo.getHealthCheckUrl()).thenReturn("http://localhost:80/mgmt/ping");
		EurekaServiceInstance service = mock(EurekaServiceInstance.class);
		when(service.getInstanceInfo()).thenReturn(instanceInfo);
		when(service.getUri()).thenReturn(URI.create("http://localhost:80"));
		when(service.getServiceId()).thenReturn("test");

		Application application = new EurekaServiceInstanceConverter().convert(service);

		assertThat(application.getManagementUrl(), is("http://localhost:80"));
	}

	@Test
	public void convert_secure_healthUrl() {
		InstanceInfo instanceInfo = mock(InstanceInfo.class);
		when(instanceInfo.getSecureHealthCheckUrl()).thenReturn("https://localhost:80/health");
		EurekaServiceInstance service = mock(EurekaServiceInstance.class);
		when(service.getInstanceInfo()).thenReturn(instanceInfo);
		when(service.getUri()).thenReturn(URI.create("http://localhost:80"));
		when(service.getServiceId()).thenReturn("test");

		Application application = new EurekaServiceInstanceConverter().convert(service);

		assertThat(application.getHealthUrl(), is("https://localhost:80/health"));
	}
}
