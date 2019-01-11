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

package de.codecentric.boot.admin.server;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MergePolicyConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.map.merge.PutIfAbsentMapMergePolicy;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test to verify the correct functionality of the REST API with Hazelcast
 *
 * @author Dennis Schulte
 */
public class AdminApplicationHazelcastTest extends AbstractAdminApplicationTest {
    private ConfigurableApplicationContext instance1;
    private ConfigurableApplicationContext instance2;
    private WebTestClient webClient2;

    @Before
    public void setUp() {
        System.setProperty("hazelcast.wait.seconds.before.join", "0");
        instance1 = new SpringApplicationBuilder().sources(TestAdminApplication.class)
                                                  .web(WebApplicationType.REACTIVE)
                                                  .run(
                                                      "--server.port=0",
                                                      "--management.endpoints.web.base-path=/mgmt",
                                                      "--endpoints.health.enabled=true",
                                                      "--info.test=foobar",
                                                      "--spring.jmx.enabled=false",
                                                      "--eureka.client.enabled=false"
                                                  );

        instance2 = new SpringApplicationBuilder().sources(TestAdminApplication.class)
                                                  .web(WebApplicationType.REACTIVE)
                                                  .run(
                                                      "--server.port=0",
                                                      "--management.endpoints.web.base-path=/mgmt",
                                                      "--endpoints.health.enabled=true",
                                                      "--info.test=foobar",
                                                      "--spring.jmx.enabled=false",
                                                      "--eureka.client.enabled=false"
                                                  );

        super.setUp(instance1.getEnvironment().getProperty("local.server.port", Integer.class, 0));
        this.webClient2 = createWebClient(instance2.getEnvironment()
                                                   .getProperty("local.server.port", Integer.class, 0));
    }


    @Test
    @Override
    public void lifecycle() {
        super.lifecycle();

        Mono<String> events1 = getWebClient().get()
                                             .uri("/instances/events")
                                             .accept(MediaType.APPLICATION_JSON)
                                             .exchange()
                                             .expectStatus()
                                             .isOk()
                                             .returnResult(String.class)
                                             .getResponseBody()
                                             .collect(Collectors.joining());

        Mono<String> events2 = webClient2.get()
                                         .uri("/instances/events")
                                         .accept(MediaType.APPLICATION_JSON)
                                         .exchange()
                                         .expectStatus()
                                         .isOk()
                                         .returnResult(String.class)
                                         .getResponseBody()
                                         .collect(Collectors.joining());

        StepVerifier.create(events1.zipWith(events2))
                    .assertNext(t -> assertThat(t.getT1()).isEqualTo(t.getT2()))
                    .verifyComplete();
    }

    @After
    public void shutdown() {
        instance1.close();
        instance2.close();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableAdminServer
    @EnableWebFluxSecurity
    public static class TestAdminApplication {
        @Bean
        SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
            return http.authorizeExchange().anyExchange().permitAll()//
                       .and().csrf().disable()//
                       .build();
        }

        @Bean
        public Config hazelcastConfig() {
            MapConfig mapConfig = new MapConfig("spring-boot-admin-event-store").setInMemoryFormat(InMemoryFormat.OBJECT)
                                                                                .setBackupCount(1)
                                                                                .setEvictionPolicy(EvictionPolicy.NONE)
                                                                                .setMergePolicyConfig(new MergePolicyConfig(
                                                                                    PutIfAbsentMapMergePolicy.class.getName(),
                                                                                    100
                                                                                ));

            Config config = new Config();
            config.addMapConfig(mapConfig);
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            TcpIpConfig tcpIpConfig = config.getNetworkConfig().getJoin().getTcpIpConfig();
            tcpIpConfig.setEnabled(true);
            tcpIpConfig.setMembers(singletonList("127.0.0.1"));
            return config;
        }
    }
}
