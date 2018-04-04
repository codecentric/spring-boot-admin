/*
 * Copyright 2014-2018 the original author or authors.
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

package de.codecentric.boot.admin.server.notify.filter.web;

import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.notify.LoggingNotifier;
import de.codecentric.boot.admin.server.notify.filter.FilteringNotifier;

import java.io.IOException;
import java.util.Map;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class NotificationFilterControllerTest {

    private final InstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());
    private final NotificationFilterController controller = new NotificationFilterController(
        new FilteringNotifier(new LoggingNotifier(repository), repository));
    private MockMvc mvc = MockMvcBuilders.standaloneSetup(controller)
                                         .setCustomHandlerMapping(
                                             () -> new de.codecentric.boot.admin.server.web.servlet.AdminControllerHandlerMapping(
                                                 "/"))
                                         .build();

    @Test
    public void test_missing_parameters() throws Exception {
        mvc.perform(post("/notifications/filters")).andExpect(status().isBadRequest());
    }

    @Test
    public void test_delete_notfound() throws Exception {
        mvc.perform(delete("/notifications/filters/abcdef")).andExpect(status().isNotFound());
    }

    @Test
    public void test_post_delete() throws Exception {
        String response = mvc.perform(post("/notifications/filters?instanceId=1337&ttl=10000"))
                             .andExpect(status().isOk())
                             .andExpect(content().string(not(isEmptyString())))
                             .andReturn()
                             .getResponse()
                             .getContentAsString();
        String id = extractId(response);

        mvc.perform(get("/notifications/filters")).andExpect(status().isOk());

        mvc.perform(delete("/notifications/filters/{id}", id)).andExpect(status().isOk());

        mvc.perform(get("/notifications/filters")).andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
    }

    private String extractId(String response) throws IOException {
        Map<?, ?> map = new ObjectMapper().readerFor(Map.class).readValue(response);
        return map.get("id").toString();
    }
}
