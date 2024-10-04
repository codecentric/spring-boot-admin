## Spring Cloud Discovery

The Spring Boot Admin Server can use Spring Clouds `DiscoveryClient` to discover applications. The advantage is that the clients don’t have to include the `spring-boot-admin-starter-client`. You just have to add a `DiscoveryClient` implementation to your admin server - everything else is done by AutoConfiguration.

### Static Configuration using SimpleDiscoveryClient

Spring Cloud provides a `SimpleDiscoveryClient`. It allows you to specify client applications via static configuration:

```xml title="pom.xml"
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter</artifactId>
</dependency>
```

```yaml title="application.yml"
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

### Other DiscoveryClients

Spring Boot Admin supports all other implementations of Spring Cloud’s `DiscoveryClient` ([Eureka](https://docs.spring.io/spring-cloud-netflix/docs/current/reference/html/#service-discovery-eureka-clients/), [Zookeeper](https://docs.spring.io/spring-cloud-zookeeper/docs/current/reference/html/#spring-cloud-zookeeper-discovery), [Consul](https://docs.spring.io/spring-cloud-consul/docs/current/reference/html/#spring-cloud-consul-discovery), [Kubernetes](https://docs.spring.io/spring-cloud-kubernetes/docs/current/reference/html/#discoveryclient-for-kubernetes), …​). You need to add it to the Spring Boot Admin Server and configure it properly. An [example setup using Eureka](/docs/index#discover-clients-via-spring-cloud-discovery) is shown above.

### Converting ServiceInstances

The information from the service registry are converted by the `ServiceInstanceConverter`. Spring Boot Admin ships with a default and Eureka converter implementation. The correct one is selected by AutoConfiguration.

:::tip
You can modify how the information from the registry is used to register the application by using SBA Server configuration options and instance metadata. The values from the metadata takes precedence over the server config. If the plenty of options don’t fit your needs you can provide your own ServiceInstanceConverter.
:::

:::tip
When using Eureka, the healthCheckUrl known to Eureka is used for health-checking, which can be set on your client using eureka.instance.healthCheckUrl.
:::

__Instance metadata options__
| Key                     | Value                                                                                                                            | Default value                                                                  |
| ----------------------- | -------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------ |
| user.nameuser.password  | Credentials being used to access the endpoints.                                                                                  |                                                                                |
| management.scheme       | The scheme is substituted in the service URL and will be used for accessing the actuator endpoints.                              |                                                                                |
| management.address      | The address is substituted in the service URL and will be used for accessing the actuator endpoints.                             |                                                                                |
| management.port         | The port is substituted in the service URL and will be used for accessing the actuator endpoints.                                |                                                                                |
| management.context-path | The path is appended to the service URL and will be used for accessing the actuator endpoints.                                   | &#36;&#123;spring.boot.admin.discovery.converter.management-context-path&#125; |
| health.path             | The path is appended to the service URL and will be used for the health-checking. Ignored by the EurekaServiceInstanceConverter. | &#36;&#123;spring.boot.admin.discovery.converter.health-endpoint&#125;         |
| group                   | The group is used to group services in the UI by the group name instead of application name.                                     |                                                                                |

__Discovery configuration options__
| Property name                                                 | Description                                                                                                                                       | Default value |
| ------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------- | ------------- |
| spring.boot.admin.discovery.enabled                           | Enables the DiscoveryClient-support for the admin server.                                                                                         | true          |
| spring.boot.admin.discovery.converter.management-context-path | Will be appended to the service-url of the discovered service when the management-url is converted by the DefaultServiceInstanceConverter.        | /actuator     |
| spring.boot.admin.discovery.converter.health-endpoint-path    | Will be appended to the management-url of the discovered service when the health-url is converted by the DefaultServiceInstanceConverter.         | "health"      |
| spring.boot.admin.discovery.ignored-services                  | This services will be ignored when using discovery and not registered as application. Supports simple patterns (e.g. "foo*", "*bar", "foo*bar*"). |               |
| spring.boot.admin.discovery.services                          | This services will be included when using discovery and registered as application. Supports simple patterns (e.g. "foo*", "*bar", "foo*bar*").    | "*"           |
| spring.boot.admin.discovery.ignored-instances-metadata        | Instances of services will be ignored if they contain at least one metadata item that matches this list. (e.g. "discoverable=false")              |               |
| spring.boot.admin.discovery.instances-metadata                | Instances of services will be included if they contain at least one metadata item that matches this list. (e.g. "discoverable=true")              |               |

### CloudFoundry

If you are deploying your applications to CloudFoundry then `vcap.application.application_id` and `vcap.application.instance_index` **_must_** be added to the metadata for proper registration of applications with Spring Boot Admin Server. Here is a sample configuration for Eureka:

```yml title="application.yml"
eureka:
  instance:
    hostname: ${vcap.application.uris[0]}
    nonSecurePort: 80
    metadata-map:
      applicationId: ${vcap.application.application_id}
      instanceId: ${vcap.application.instance_index}
```
