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
package de.codecentric.boot.admin.server;

import de.codecentric.boot.admin.server.config.EnableAdminServer;

import org.junit.After;
import org.junit.Before;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.TcpIpConfig;

import static java.util.Collections.singletonList;

/**
 * Integration test to verify the correct functionality of the REST API with Hazelcast
 *
 * @author Dennis Schulte
 */
public class AdminApplicationHazelcastTest extends AbstractAdminApplicationTest {
    private ServletWebServerApplicationContext instance1;
    private ServletWebServerApplicationContext instance2;

    @Before
    public void setUp() throws Exception {
        System.setProperty("hazelcast.wait.seconds.before.join", "0");
        instance1 = (ServletWebServerApplicationContext) SpringApplication.run(TestAdminApplication.class,
                "--server.port=0", "--spring.jmx.enabled=false", "--management.context-path=/mgmt",
                "--management.security.enabled=false", "--info.test=foobar");
        instance2 = (ServletWebServerApplicationContext) SpringApplication.run(TestAdminApplication.class,
                "--server.port=0", "--spring.jmx.enabled=false", "--management.context-path=/mgmt",
                "--management.security.enabled=false", "--info.test=foobar");

        super.setUp(instance1.getWebServer().getPort());
    }

    @After
    public void shutdown() {
        instance1.close();
        instance2.close();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableAdminServer
    public static class TestAdminApplication {
        @Bean
        public Config hazelcastConfig() {
            Config config = new Config();

            config.addMapConfig(new MapConfig("spring-boot-admin-event-store").setInMemoryFormat(InMemoryFormat.OBJECT)
                                                                              .setBackupCount(1)
                                                                              .setEvictionPolicy(EvictionPolicy.NONE));

            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            TcpIpConfig tcpIpConfig = config.getNetworkConfig().getJoin().getTcpIpConfig();
            tcpIpConfig.setEnabled(true);
            tcpIpConfig.setMembers(singletonList("127.0.0.1"));
            return config;
        }
    }
}
