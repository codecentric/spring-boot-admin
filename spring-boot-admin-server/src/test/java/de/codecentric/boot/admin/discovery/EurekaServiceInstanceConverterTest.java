package de.codecentric.boot.admin.discovery;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient.EurekaServiceInstance;

import com.netflix.appinfo.InstanceInfo;

import de.codecentric.boot.admin.model.Application;

public class EurekaServiceInstanceConverterTest {

	@Test
	public void convert() {
		EurekaServiceInstanceConverter converter = new EurekaServiceInstanceConverter();
		InstanceInfo instanceInfo = mock(InstanceInfo.class);
		when(instanceInfo.getAppName()).thenReturn("test");
		when(instanceInfo.getHomePageUrl()).thenReturn("http://localhost:80");
		when(instanceInfo.getHealthCheckUrl()).thenReturn("http://localhost:80/mgmt/ping");
		when(instanceInfo.getMetadata())
				.thenReturn(singletonMap("management.context-path", "/mgmt"));
		EurekaServiceInstance service = mock(EurekaServiceInstance.class);
		when(service.getInstanceInfo()).thenReturn(instanceInfo);

		Application application = converter.convert(service);

		assertThat(application.getId(), nullValue());
		assertThat(application.getName(), is("test"));
		assertThat(application.getServiceUrl(), is("http://localhost:80"));
		assertThat(application.getManagementUrl(), is("http://localhost:80/mgmt"));
		assertThat(application.getHealthUrl(), is("http://localhost:80/mgmt/ping"));

		// no management url in metadata
		when(instanceInfo.getMetadata()).thenReturn(Collections.<String, String> emptyMap());
		application = converter.convert(service);
		assertThat(application.getManagementUrl(), is("http://localhost:80"));
	}
}
