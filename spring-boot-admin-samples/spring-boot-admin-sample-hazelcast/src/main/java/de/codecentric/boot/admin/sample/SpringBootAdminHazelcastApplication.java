/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.sample;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizePolicy;
import com.hazelcast.config.MergePolicyConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.spi.merge.PutIfAbsentMergePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import de.codecentric.boot.admin.server.config.EnableAdminServer;
import de.codecentric.boot.admin.server.notify.Notifier;

import static de.codecentric.boot.admin.server.config.AdminServerHazelcastAutoConfiguration.DEFAULT_NAME_EVENT_STORE_MAP;
import static de.codecentric.boot.admin.server.config.AdminServerHazelcastAutoConfiguration.DEFAULT_NAME_SENT_NOTIFICATIONS_MAP;
import static java.util.Collections.singletonList;

@SpringBootApplication
@EnableAdminServer
public class SpringBootAdminHazelcastApplication {

	private static final Logger log = LoggerFactory.getLogger(SpringBootAdminHazelcastApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootAdminHazelcastApplication.class, args);
	}

	// tag::application-hazelcast[]
	@Bean
	public Config hazelcastConfig() {
		// This map is used to store the events.
		// It should be configured to reliably hold all the data,
		// Spring Boot Admin will compact the events, if there are too many
		MapConfig eventStoreMap = new MapConfig(DEFAULT_NAME_EVENT_STORE_MAP).setInMemoryFormat(InMemoryFormat.OBJECT)
			.setBackupCount(1)
			.setMergePolicyConfig(new MergePolicyConfig(PutIfAbsentMergePolicy.class.getName(), 100));

		// This map is used to deduplicate the notifications.
		// If data in this map gets lost it should not be a big issue as it will atmost
		// lead to
		// the same notification to be sent by multiple instances
		MapConfig sentNotificationsMap = new MapConfig(DEFAULT_NAME_SENT_NOTIFICATIONS_MAP)
			.setInMemoryFormat(InMemoryFormat.OBJECT)
			.setBackupCount(1)
			.setEvictionConfig(
					new EvictionConfig().setEvictionPolicy(EvictionPolicy.LRU).setMaxSizePolicy(MaxSizePolicy.PER_NODE))
			.setMergePolicyConfig(new MergePolicyConfig(PutIfAbsentMergePolicy.class.getName(), 100));

		Config config = new Config();
		config.addMapConfig(eventStoreMap);
		config.addMapConfig(sentNotificationsMap);
		config.setProperty("hazelcast.jmx", "true");

		// WARNING: This setups a local cluster, you change it to fit your needs.
		config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
		TcpIpConfig tcpIpConfig = config.getNetworkConfig().getJoin().getTcpIpConfig();
		tcpIpConfig.setEnabled(true);
		tcpIpConfig.setMembers(singletonList("127.0.0.1"));
		return config;
	}
	// end::application-hazelcast[]

	@Bean
	public Notifier loggingNotifier() {
		return (event) -> Mono.fromRunnable(() -> log.info("Event occured: {}", event));
	}

	@Profile("insecure")
	@Configuration(proxyBeanMethods = false)
	public static class SecurityPermitAllConfig {

		private final AdminServerProperties adminServer;

		public SecurityPermitAllConfig(AdminServerProperties adminServer) {
			this.adminServer = adminServer;
		}

		@Bean
		protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
			http.authorizeHttpRequests((authorizeRequests) -> authorizeRequests.anyRequest().permitAll())
				.csrf((csrf) -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
					.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
					.ignoringRequestMatchers(
							new AntPathRequestMatcher(this.adminServer.path("/instances"), HttpMethod.POST.toString()),
							new AntPathRequestMatcher(this.adminServer.path("/instances/*"),
									HttpMethod.DELETE.toString()),
							new AntPathRequestMatcher(this.adminServer.path("/actuator/**"))));

			return http.build();
		}

	}

	@Profile("secure")
	@Configuration(proxyBeanMethods = false)
	public static class SecuritySecureConfig {

		private final AdminServerProperties adminServer;

		public SecuritySecureConfig(AdminServerProperties adminServer) {
			this.adminServer = adminServer;
		}

		@Bean
		protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
			SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
			successHandler.setTargetUrlParameter("redirectTo");
			successHandler.setDefaultTargetUrl(this.adminServer.path("/"));

			http.authorizeHttpRequests((authorizeRequests) -> authorizeRequests
				.requestMatchers(new AntPathRequestMatcher(this.adminServer.path("/assets/**")))
				.permitAll()
				.requestMatchers(new AntPathRequestMatcher(this.adminServer.path("/login")))
				.permitAll()
				.anyRequest()
				.authenticated())
				.formLogin((formLogin) -> formLogin.loginPage(this.adminServer.path("/login"))
					.successHandler(successHandler))
				.logout((logout) -> logout.logoutUrl(this.adminServer.path("/logout")))
				.httpBasic(Customizer.withDefaults())
				.csrf((csrf) -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
					.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
					.ignoringRequestMatchers(
							new AntPathRequestMatcher(this.adminServer.path("/instances"), HttpMethod.POST.toString()),
							new AntPathRequestMatcher(this.adminServer.path("/instances/*"),
									HttpMethod.DELETE.toString()),
							new AntPathRequestMatcher(this.adminServer.path("/actuator/**"))));

			return http.build();
		}

	}

}
