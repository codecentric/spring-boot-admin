# Sample Projects

Spring Boot Admin includes several sample projects demonstrating different deployment scenarios and integration patterns. These samples provide working examples you can use as starting points for your own implementations.

## Available Samples[​](#available-samples "Direct link to Available Samples")

### Basic Deployments[​](#basic-deployments "Direct link to Basic Deployments")

* **[Servlet Sample](/4.0.0-SNAPSHOT/docs/samples/sample-servlet.md)** - Traditional servlet-based deployment with security
* **[Reactive Sample](/4.0.0-SNAPSHOT/docs/samples/sample-reactive.md)** - WebFlux reactive deployment

### Service Discovery[​](#service-discovery "Direct link to Service Discovery")

* **[Eureka Sample](/4.0.0-SNAPSHOT/docs/samples/sample-eureka.md)** - Netflix Eureka integration
* **[Consul Sample](/4.0.0-SNAPSHOT/docs/samples/sample-consul.md)** - HashiCorp Consul integration
* **[Zookeeper Sample](/4.0.0-SNAPSHOT/docs/samples/sample-zookeeper.md)** - Apache Zookeeper integration

### Advanced[​](#advanced "Direct link to Advanced")

* **[Hazelcast Sample](/4.0.0-SNAPSHOT/docs/samples/sample-hazelcast.md)** - Clustered deployment with Hazelcast
* **[Custom UI Sample](/4.0.0-SNAPSHOT/docs/samples/sample-custom-ui.md)** - Custom UI extensions and branding

## Repository Location[​](#repository-location "Direct link to Repository Location")

All samples are available in the [Spring Boot Admin GitHub repository](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples):

```
spring-boot-admin-samples/
├── spring-boot-admin-sample-servlet/
├── spring-boot-admin-sample-reactive/
├── spring-boot-admin-sample-war/
├── spring-boot-admin-sample-eureka/
├── spring-boot-admin-sample-consul/
├── spring-boot-admin-sample-zookeeper/
├── spring-boot-admin-sample-hazelcast/
└── spring-boot-admin-sample-custom-ui/
```

## Running the Samples[​](#running-the-samples "Direct link to Running the Samples")

### Prerequisites[​](#prerequisites "Direct link to Prerequisites")

* Java 17 or higher
* Maven 3.6 or higher
* Docker (optional, for some samples)

### Build All Samples[​](#build-all-samples "Direct link to Build All Samples")

```
git clone https://github.com/codecentric/spring-boot-admin.git
cd spring-boot-admin
mvn clean install -DskipTests
```

### Run Individual Sample[​](#run-individual-sample "Direct link to Run Individual Sample")

```
cd spring-boot-admin-samples/spring-boot-admin-sample-servlet
mvn spring-boot:run
```

Access the Admin UI at: `http://localhost:8080`

### Default Credentials[​](#default-credentials "Direct link to Default Credentials")

Most secured samples use:

* **Username**: `user`
* **Password**: Check console output or `application.yml`

## Sample Features Comparison[​](#sample-features-comparison "Direct link to Sample Features Comparison")

| Feature           | Servlet | Reactive | Eureka  | Consul  | Zookeeper | Hazelcast | Custom UI | WAR     |
| ----------------- | ------- | -------- | ------- | ------- | --------- | --------- | --------- | ------- |
| Web Stack         | Servlet | WebFlux  | Servlet | Servlet | Servlet   | Servlet   | Servlet   | Servlet |
| Security          | ✅      | ✅       | ✅      | ✅      | -         | -         | -         | -       |
| Service Discovery | Static  | Static   | Eureka  | Consul  | Zookeeper | Static    | Static    | Static  |
| Clustering        | -       | -        | -       | -       | -         | ✅        | -         | -       |
| Custom UI         | -       | -        | -       | -       | -         | -         | ✅        | -       |
| JMX Support       | ✅      | -        | -       | -       | -         | -         | -         | ✅      |
| Notifications     | ✅      | -        | -       | -       | -         | -         | -         | -       |

## Common Configuration[​](#common-configuration "Direct link to Common Configuration")

All samples share common patterns:

### Actuator Configuration[​](#actuator-configuration "Direct link to Actuator Configuration")

```
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: ALWAYS
```

### Logging Configuration[​](#logging-configuration "Direct link to Logging Configuration")

```
logging:
  file:
    name: "target/boot-admin-sample.log"
  pattern:
    file: "%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(%5p) %clr(${PID}){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n%wEx"
```

### Build Info[​](#build-info "Direct link to Build Info")

All samples generate build information:

```
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>build-info</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## Quick Start Guide[​](#quick-start-guide "Direct link to Quick Start Guide")

### 1. Servlet Sample (Recommended for Beginners)[​](#1-servlet-sample-recommended-for-beginners "Direct link to 1. Servlet Sample (Recommended for Beginners)")

```
cd spring-boot-admin-samples/spring-boot-admin-sample-servlet
mvn spring-boot:run
```

Features:

* Security enabled
* Self-monitoring
* Mail notifications
* Custom UI extensions

### 2. Eureka Sample (Recommended for Microservices)[​](#2-eureka-sample-recommended-for-microservices "Direct link to 2. Eureka Sample (Recommended for Microservices)")

```
# Start Eureka Server
docker run -d -p 8761:8761 springcloud/eureka

# Start Admin Server
cd spring-boot-admin-samples/spring-boot-admin-sample-eureka
mvn spring-boot:run
```

Features:

* Automatic service discovery
* Dynamic registration
* No client library needed

### 3. Hazelcast Sample (Recommended for Production)[​](#3-hazelcast-sample-recommended-for-production "Direct link to 3. Hazelcast Sample (Recommended for Production)")

```
# Start multiple instances
cd spring-boot-admin-samples/spring-boot-admin-sample-hazelcast

# Terminal 1
SERVER_PORT=8080 mvn spring-boot:run

# Terminal 2
SERVER_PORT=8081 mvn spring-boot:run
```

Features:

* High availability
* Shared event store
* Load balancing ready

## Docker Support[​](#docker-support "Direct link to Docker Support")

Some samples include Docker Compose configurations:

```
cd spring-boot-admin-samples/spring-boot-admin-sample-eureka
docker-compose up
```

## Customizing Samples[​](#customizing-samples "Direct link to Customizing Samples")

Use the samples as templates:

1. **Copy sample directory**:

   ```
   cp -r spring-boot-admin-sample-servlet my-admin-server
   ```

2. **Update `pom.xml`**:

   ```
   <artifactId>my-admin-server</artifactId>
   <name>My Admin Server</name>
   ```

3. **Customize configuration**:

   * Update `application.yml`
   * Add security configuration
   * Configure notifications

4. **Build and run**:

   ```
   mvn clean package
   java -jar target/my-admin-server.jar
   ```

## Testing Samples[​](#testing-samples "Direct link to Testing Samples")

Each sample includes tests:

```
cd spring-boot-admin-samples/spring-boot-admin-sample-servlet
mvn test
```

## Troubleshooting Samples[​](#troubleshooting-samples "Direct link to Troubleshooting Samples")

### Port Already in Use[​](#port-already-in-use "Direct link to Port Already in Use")

Change the port:

```
SERVER_PORT=9090 mvn spring-boot:run
```

Or in `application.yml`:

```
server:
  port: 9090
```

### Build Failures[​](#build-failures "Direct link to Build Failures")

Clean and rebuild:

```
mvn clean install -DskipTests
```

### Dependencies Issues[​](#dependencies-issues "Direct link to Dependencies Issues")

Update Spring Boot Admin version in parent POM and rebuild.

## Contributing[​](#contributing "Direct link to Contributing")

To add a new sample:

1. Create directory under `spring-boot-admin-samples/`
2. Follow existing sample structure
3. Add `README.md` with specific instructions
4. Include `docker-compose.yml` if applicable
5. Add tests
6. Update samples documentation

## See Also[​](#see-also "Direct link to See Also")

* [Getting Started](/4.0.0-SNAPSHOT/docs/02-getting-started/) - Basic setup guide
* [Server Configuration](/4.0.0-SNAPSHOT/docs/server/server.md) - Server configuration options
* [Integration](/4.0.0-SNAPSHOT/docs/04-integration/) - Service discovery integration
* [Customization](/4.0.0-SNAPSHOT/docs/06-customization/) - UI and server customization
