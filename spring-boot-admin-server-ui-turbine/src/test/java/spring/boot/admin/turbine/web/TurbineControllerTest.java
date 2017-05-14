package spring.boot.admin.turbine.web;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class TurbineControllerTest {

	private DiscoveryClient discoveryClient = mock(DiscoveryClient.class);

	private MockMvc mvc = MockMvcBuilders
			.standaloneSetup(new TurbineController(discoveryClient))
			.build();

	@Test
	public void test_clusters() throws Exception {
		when(discoveryClient.getServices()).thenReturn(asList("c1", "c2"));
		mvc.perform(get("/api/turbine/clusters")).andExpect(status().isOk())
				.andExpect(jsonPath("$.clusters").value(is(asList("c1", "c2"))));
	}

}
