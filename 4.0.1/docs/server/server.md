# Set up server

## Running Behind a Front-end Proxy Server[​](#running-behind-a-front-end-proxy-server "Direct link to Running Behind a Front-end Proxy Server")

In case the Spring Boot Admin server is running behind a reverse proxy, it may be requried to configure the public url where the server is reachable via (`spring.boot.admin.ui.public-url`). In addition, when the reverse proxy terminates the https connection, it may be necessary to configure `server.forward-headers-strategy=native` (also see [Spring Boot Reference Guide](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto-use-tomcat-behind-a-proxy-server)).

## Spring Cloud Discovery[​](#spring-cloud-discovery "Direct link to Spring Cloud Discovery")

The Spring Boot Admin Server can use Spring Clouds `DiscoveryClient` to discover applications. The advantage is that the clients don’t have to include the `spring-boot-admin-starter-client`. You just have to add a `DiscoveryClient` implementation to your admin server - everything else is done by AutoConfiguration.

### Static Configuration using SimpleDiscoveryClient[​](#static-configuration-using-simplediscoveryclient "Direct link to Static Configuration using SimpleDiscoveryClient")

Spring Cloud provides a `SimpleDiscoveryClient`. It allows you to specify client applications via static configuration:

pom.xml

```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter</artifactId>
</dependency>
```

application.yml

```
spring:
  cloud:
    discovery:
      client:
        simple:
          instances:
            test:
              - uri: http://instance1.intern:8080
                metadata:
                  management.context-path: /actuator
              - uri: http://instance2.intern:8080
                metadata:
                  management.context-path: /actuator
```

### Other DiscoveryClients[​](#other-discoveryclients "Direct link to Other DiscoveryClients")

Spring Boot Admin supports all other implementations of Spring Cloud's `DiscoveryClient` ([Eureka](https://docs.spring.io/spring-cloud-netflix/docs/current/reference/html/#service-discovery-eureka-clients/), [Zookeeper](https://docs.spring.io/spring-cloud-zookeeper/docs/current/reference/html/#spring-cloud-zookeeper-discovery), [Consul](https://docs.spring.io/spring-cloud-consul/docs/current/reference/html/#spring-cloud-consul-discovery), [Kubernetes](https://docs.spring.io/spring-cloud-kubernetes/docs/current/reference/html/#discoveryclient-for-kubernetes), …​). You need to add it to the Spring Boot Admin Server and configure it properly. See the [integration guides](/4.0.1/docs/04-integration/) for detailed setup instructions.

### Converting ServiceInstances[​](#converting-serviceinstances "Direct link to Converting ServiceInstances")

The information from the service registry are converted by the `ServiceInstanceConverter`. Spring Boot Admin ships with a default and Eureka converter implementation. The correct one is selected by AutoConfiguration.

tip

You can modify how the information from the registry is used to register the application by using SBA Server configuration options and instance metadata. The values from the metadata takes precedence over the server config. If the plenty of options don’t fit your needs you can provide your own ServiceInstanceConverter.

tip

When using Eureka, the healthCheckUrl known to Eureka is used for health-checking, which can be set on your client using eureka.instance.healthCheckUrl.

| Property                                                                                                                                                                                                                                        |
| ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `spring.boot.admin.discovery.converter.health-endpoint-path`Default path of the health-endpoint to be used for the health-url of the discovered service.- Type: 

  `java.lang.String`

- Default: 

  `"health"`                                     |
| `spring.boot.admin.discovery.converter.health-endpoint-path`Default path of the health-endpoint to be used for the health-url of the discovered service.- Type: 

  `java.lang.String`

- Default: 

  `"health"`                                     |
| `spring.boot.admin.discovery.converter.health-endpoint-path`Default path of the health-endpoint to be used for the health-url of the discovered service.- Type: 

  `java.lang.String`

- Default: 

  `"health"`                                     |
| `spring.boot.admin.discovery.converter.management-context-path`Default context-path to be appended to the url of the discovered service for the management-url.- Type: 

  `java.lang.String`

- Default: 

  `"/actuator"`                           |
| `spring.boot.admin.discovery.converter.management-context-path`Default context-path to be appended to the url of the discovered service for the management-url.- Type: 

  `java.lang.String`

- Default: 

  `"/actuator"`                           |
| `spring.boot.admin.discovery.converter.management-context-path`Default context-path to be appended to the url of the discovered service for the management-url.- Type: 

  `java.lang.String`

- Default: 

  `"/actuator"`                           |
| `spring.boot.admin.discovery.enabled`Enable Spring Cloud Discovery support.- Type: 

  `java.lang.Boolean`

- Default: 

  `"true"`                                                                                                                   |
| `spring.boot.admin.discovery.ignored-instances-metadata`Map of metadata that has to be matched by service instance that is to be ignored. (e.g. "discoverable=false")- Type: 

  `java.util.Map<java.lang.String,java.lang.String>`               |
| `spring.boot.admin.discovery.ignored-services`Set of serviceIds to be ignored and not to be registered as application. Supports simple patterns (e.g. "foo\*", "\*foo", "foo\*bar").- Type: 

  `java.util.Set<java.lang.String>`                 |
| `spring.boot.admin.discovery.instances-metadata`Map of metadata that has to be matched by service instance that is to be registered. (e.g. "discoverable=true")- Type: 

  `java.util.Map<java.lang.String,java.lang.String>`                     |
| `spring.boot.admin.discovery.services`Set of serviceIds that has to match to be registered as application. Supports simple patterns (e.g. "foo\*", "\*foo", "foo\*bar"). Default value is everything- Type: 

  `java.util.Set<java.lang.String>` |

### CloudFoundry[​](#cloudfoundry "Direct link to CloudFoundry")

If you are deploying your applications to CloudFoundry then `vcap.application.application_id` and `vcap.application.instance_index` ***must*** be added to the metadata for proper registration of applications with Spring Boot Admin Server. Here is a sample configuration for Eureka:

application.yml

```
eureka:
  instance:
    hostname: ${vcap.application.uris[0]}
    nonSecurePort: 80
    metadata-map:
      applicationId: ${vcap.application.application_id}
      instanceId: ${vcap.application.instance_index}
```
