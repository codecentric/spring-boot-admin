/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin;

import de.codecentric.boot.admin.config.EnableAdminServer;
import de.codecentric.boot.admin.model.Application;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static de.codecentric.boot.admin.TestData.createApplication;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AdminApplicationTest.TestConfiguration.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class AdminApplicationTest {

    @Value("${local.server.port}")
    int port;

    private RestTemplate template = new TestRestTemplate();
    private String baseUrl;
    private final String appName = "spring-boot-admin-test";
    private UriComponentsBuilder uriBuilder;

    @Before
    public void setUp() {
        baseUrl = "http://localhost:" + port;
        uriBuilder = UriComponentsBuilder.newInstance()
                .scheme("http").host("localhost").port(port);
    }

    @Test
    public void testRegisterAndGetApplicationById() {
        Application app = createApplication(appName);

        // Register application and retrieve location header
        final URI locationHeader = registerApplication(app).getRight();
        // Retrieve registered application by ID via the location header
        ResponseEntity<Application> getResponse = template.getForEntity(locationHeader, Application.class);
        Assert.assertNotNull("Application must not be null!", getResponse.getBody());
        Assert.assertEquals("Application name is wrong!", appName, getResponse.getBody().getName());
        Assert.assertEquals(HttpStatus.OK, getResponse.getStatusCode());
    }

    private Pair<Long, URI> registerApplication(Application app) {
        ResponseEntity<Long> postResponse = template.postForEntity(baseUrl + "/api/applications", app, Long.class);
        Long id = postResponse.getBody();
        Assert.assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
        Assert.assertNotNull("Application ID must not be null!", id);
        URI locationHeader = postResponse.getHeaders().getLocation();
        Assert.assertEquals(baseUrl + "/api/application/" + id, locationHeader.toString());
        return Pair.of(id, locationHeader);
    }

    @Test
    public void testRemoveApplicationById() {
        // Register application
        Application app = createApplication("spring-boot-admin-test");
        Long id = registerApplication(app).getLeft();
        // Delete application
        URI url = uriBuilder.path("/api/application/{id}").buildAndExpand(id).toUri();
        template.delete(url);
        // Check that application was deleted
        ResponseEntity<Application> getResponse = template.getForEntity(url, Application.class);
        Assert.assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    public void testGetAllApplications() {
        Application app = createApplication(appName);
        final Pair<Long, URI> idAndLocationPair = registerApplication(app);

        app = createApplication("appId2");
        final Pair<Long, URI> idAndLocationPair2 = registerApplication(app);

        ResponseEntity<Application[]> getResponse = template.getForEntity(baseUrl + "/api/applications", Application[].class);
        Assert.assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        Assert.assertNotNull("Application list must not be null!", getResponse.getBody());
        Application[] applicationList =  getResponse.getBody();
        Assert.assertEquals("Application list has  wrong size!", 2, applicationList.length);
        for(Application application : applicationList){
            if(application.getId().equals(idAndLocationPair.getLeft())){
                Assert.assertEquals("Application name is wrong!", appName, application.getName());
            }else if(application.getId().equals(idAndLocationPair2.getLeft())){
                Assert.assertEquals("Application name is wrong!", "appId2", application.getName());
            }
            else{
                fail("Should never happen!");
            }
        }
    }

    @Test
    public void testRemoveAllApplications() {
        // Register two applications
        Application app = createApplication("abc");
        Long id = registerApplication(app).getLeft();
        app = createApplication("appId2");
        registerApplication(app);

        // Remove all applications
        template.delete(baseUrl + "/api/applications");

        // Check that all applications have been removed
        URI url = uriBuilder.path("/api/application/{id}").buildAndExpand(id).toUri();
        ResponseEntity<Application> getResponse = template.getForEntity(url, Application.class);
        Assert.assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Configuration
    @EnableAdminServer
    public static class TestConfiguration {
    }
}