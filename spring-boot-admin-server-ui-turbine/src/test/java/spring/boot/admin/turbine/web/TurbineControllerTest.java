package spring.boot.admin.turbine.web;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class TurbineControllerTest {

	private MockMvc mvc = MockMvcBuilders
			.standaloneSetup(new TurbineController(new String[] { "c1", "c2" }))
			.build();

	@Test
	public void test_clusters() throws Exception {
		mvc.perform(get("/api/turbine/clusters")).andExpect(status().isOk())
				.andExpect(jsonPath("$.clusters").value(is(asList("c1", "c2"))));
	}

}
