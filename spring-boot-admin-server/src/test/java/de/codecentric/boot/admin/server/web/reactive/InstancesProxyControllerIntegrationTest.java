/*
 * Copyright 2014-2019 the original author or authors.
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

package de.codecentric.boot.admin.server.web.reactive;

import de.codecentric.boot.admin.server.AdminReactiveApplicationTest;
import de.codecentric.boot.admin.server.web.AbstractInstancesProxyControllerIntegrationTest;

import javax.annotation.Nullable;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class InstancesProxyControllerIntegrationTest extends AbstractInstancesProxyControllerIntegrationTest {
    @Nullable
    private static ConfigurableApplicationContext context;

    @BeforeClass
    public static void setUpContext() {
        context = new SpringApplicationBuilder().sources(AdminReactiveApplicationTest.TestAdminApplication.class)
                                                .web(WebApplicationType.REACTIVE)
                                                .run(
                                                    "--server.port=0",
                                                    "--spring.boot.admin.monitor.default-timeout=2500"
                                                );
    }

    @Before
    public void setUpClient() {
        super.setUpClient(context);
    }

    @AfterClass
    public static void tearDownContext() {
        if (context != null) {
            context.close();
        }
    }
}
