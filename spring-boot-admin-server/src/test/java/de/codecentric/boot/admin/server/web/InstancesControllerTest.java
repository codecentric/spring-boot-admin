/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin.server.web;

import de.codecentric.boot.admin.server.domain.entities.EventSourcingInstanceRepository;
import de.codecentric.boot.admin.server.eventstore.ConcurrentMapEventStore;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.services.HashingInstanceUrlIdGenerator;
import de.codecentric.boot.admin.server.services.InstanceRegistry;

import java.io.UnsupportedEncodingException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.jayway.jsonpath.JsonPath;

import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class InstancesControllerTest {
    private static final String APPLICATION_TEST_JSON;
    private static final String APPLICATION_TWICE_JSON;

    static {
        try {
            APPLICATION_TEST_JSON = new JSONObject().put("name", "test")
                                                    .put("healthUrl", "http://localhost/mgmt/health")
                                                    .toString();
            APPLICATION_TWICE_JSON = new JSONObject().put("name", "twice")
                                                     .put("healthUrl", "http://localhost/mgmt/health")
                                                     .toString();
        } catch (JSONException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private MockMvc mvc;

    @Before
    public void setup() {
        ConcurrentMapEventStore eventStore = new InMemoryEventStore();
        EventSourcingInstanceRepository repository = new EventSourcingInstanceRepository(eventStore);
        repository.start();
        InstanceRegistry registry = new InstanceRegistry(repository, new HashingInstanceUrlIdGenerator());
        mvc = MockMvcBuilders.standaloneSetup(new InstancesController(registry, eventStore)).build();
    }

    @Test
    @Ignore("MockMvc seems not to correctly handle Flux/Mono responses")
    public void test_register_twice_get_and_remove() throws Exception {
        MvcResult result = mvc.perform(
                post("/instances").contentType(MediaType.APPLICATION_JSON).content(APPLICATION_TEST_JSON))
                              .andExpect(status().isCreated())
                              .andExpect(header().string("location", startsWith("http://localhost")))
                              .andExpect(jsonPath("$.id").isNotEmpty())
                              .andReturn();

        String id = extractId(result);

        mvc.perform(post("/instances").contentType(MediaType.APPLICATION_JSON).content(APPLICATION_TWICE_JSON))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.id").value(id));

        mvc.perform(get("/instances")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));

        mvc.perform(get("/instances?name=twice")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));

        mvc.perform(get("/instances/{id}", id)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));

        mvc.perform(delete("/instances/{id}", id)).andExpect(status().isNoContent());

        mvc.perform(get("/instances/{id}", id)).andExpect(status().isNotFound());

        MvcResult eventResult = mvc.perform(get("/instances/events")).andExpect(request().asyncStarted()).andReturn();

        mvc.perform(asyncDispatch(eventResult))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
           .andExpect(jsonPath("$[0].application").value(id))
           .andExpect(jsonPath("$[0].version").value(0L))
           .andExpect(jsonPath("$[0].type").value("REGISTERED"))
           .andExpect(jsonPath("$[1].application").value(id))
           .andExpect(jsonPath("$[1].version").value(1L))
           .andExpect(jsonPath("$[1].type").value("REGISTRATION_UPDATED"))
           .andExpect(jsonPath("$[2].application").value(id))
           .andExpect(jsonPath("$[2].version").value(2L))
           .andExpect(jsonPath("$[2].type").value("DEREGISTERED"));
    }

    private String extractId(MvcResult result) throws UnsupportedEncodingException {
        return JsonPath.compile("$.id").read(result.getResponse().getContentAsString());
    }

    @Test
    @Ignore("MockMvc seems not to correctly handle Flux/Mono responses")
    public void test_get_notFound() throws Exception {
        mvc.perform(get("/instances/unknown")).andExpect(status().isNotFound());
        mvc.perform(get("/instances?name=unknown")).andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Ignore("MockMvc seems not to correctly handle Flux/Mono responses")
    public void test_delete_notFound() throws Exception {
        mvc.perform(delete("/instances/unknown")).andExpect(status().isNotFound());
    }

}
