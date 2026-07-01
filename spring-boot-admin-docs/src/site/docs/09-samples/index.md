---

sidebar_position: 70
sidebar_custom_props:
  icon: 'file-code'
---

# Sample Projects

Spring Boot Admin includes several sample projects demonstrating different deployment scenarios and integration
patterns. These samples provide working examples you can use as starting points for your own implementations.

## Available Samples

### Basic Deployments

- **[Servlet Sample](./10-sample-servlet.md)** - Traditional servlet-based deployment with security
- **[Reactive Sample](./20-sample-reactive.md)** - WebFlux reactive deployment

### Service Discovery

- **[Eureka Sample](./30-sample-eureka.md)** - Netflix Eureka integration
- **[Consul Sample](./40-sample-consul.md)** - HashiCorp Consul integration
- **[Zookeeper Sample](./50-sample-zookeeper.md)** - Apache Zookeeper integration

### Advanced

- **[Hazelcast Sample](./60-sample-hazelcast.md)** - Clustered deployment with Hazelcast
- **[Custom UI Sample](./70-sample-custom-ui.md)** - Custom UI extensions and branding

## Repository Location

All samples are available in
the [Spring Boot Admin GitHub repository](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples):

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

## Running the Samples

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Docker (optional, for some samples)

### Build All Samples

```bash
git clone https://github.com/codecentric/spring-boot-admin.git
cd spring-boot-admin
mvn clean install -DskipTests
```

### Run Individual Sample

```bash
cd spring-boot-admin-samples/spring-boot-admin-sample-servlet
mvn spring-boot:run
```

Access the Admin UI at: `http://localhost:8080`

### Default Credentials

Most secured samples use:

- **Username**: `user`
- **Password**: Check console output or `application.yml`

## Sample Features Comparison

| Feature           | Servlet | Reactive | Eureka  | Consul  | Zookeeper | Hazelcast | Custom UI | WAR     |
|-------------------|---------|----------|---------|---------|-----------|-----------|-----------|---------|
| Web Stack         | Servlet | WebFlux  | Servlet | Servlet | Servlet   | Servlet   | Servlet   | Servlet |
| Security          | ✅       | ✅        | ✅       | ✅       | -         | -         | -         | -       |
| Service Discovery | Static  | Static   | Eureka  | Consul  | Zookeeper | Static    | Static    | Static  |
| Clustering        | -       | -        | -       | -       | -         | ✅         | -         | -       |
| Custom UI         | -       | -        | -       | -       | -         | -         | ✅         | -       |
| JMX Support       | ✅       | -        | -       | -       | -         | -         | -         | ✅       |
| Notifications     | ✅       | -        | -       | -       | -         | -         | -         | -       |

## Common Configuration

All samples share common patterns:

### Actuator Configuration

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: ALWAYS
```

### Logging Configuration

```yaml
logging:
  file:
    name: "target/boot-admin-sample.log"
  pattern:
    file: "%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(%5p) %clr(${PID}){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n%wEx"
```

### Build Info

All samples generate build information:

```xml
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

## Quick Start Guide

### 1. Servlet Sample (Recommended for Beginners)

```bash
cd spring-boot-admin-samples/spring-boot-admin-sample-servlet
mvn spring-boot:run
```

Features:

- Security enabled
- Self-monitoring
- Mail notifications
- Custom UI extensions

### 2. Eureka Sample (Recommended for Microservices)

```bash
# Start Eureka Server
docker run -d -p 8761:8761 springcloud/eureka

# Start Admin Server
cd spring-boot-admin-samples/spring-boot-admin-sample-eureka
mvn spring-boot:run
```

Features:

- Automatic service discovery
- Dynamic registration
- No client library needed

### 3. Hazelcast Sample (Recommended for Production)

```bash
# Start multiple instances
cd spring-boot-admin-samples/spring-boot-admin-sample-hazelcast

# Terminal 1
SERVER_PORT=8080 mvn spring-boot:run

# Terminal 2
SERVER_PORT=8081 mvn spring-boot:run
```

Features:

- High availability
- Shared event store
- Load balancing ready

## Docker Support

Some samples include Docker Compose configurations:

```bash
cd spring-boot-admin-samples/spring-boot-admin-sample-eureka
docker-compose up
```

## Customizing Samples

Use the samples as templates:

1. **Copy sample directory**:
   ```bash
   cp -r spring-boot-admin-sample-servlet my-admin-server
   ```

2. **Update `pom.xml`**:
   ```xml
   <artifactId>my-admin-server</artifactId>
   <name>My Admin Server</name>
   ```

3. **Customize configuration**:
    - Update `application.yml`
    - Add security configuration
    - Configure notifications

4. **Build and run**:
   ```bash
   mvn clean package
   java -jar target/my-admin-server.jar
   ```

## Testing Samples

Each sample includes tests:

```bash
cd spring-boot-admin-samples/spring-boot-admin-sample-servlet
mvn test
```

## Troubleshooting Samples

### Port Already in Use

Change the port:

```bash
SERVER_PORT=9090 mvn spring-boot:run
```

Or in `application.yml`:

```yaml
server:
  port: 9090
```

### Build Failures

Clean and rebuild:

```bash
mvn clean install -DskipTests
```

### Dependencies Issues

Update Spring Boot Admin version in parent POM and rebuild.

## Contributing

To add a new sample:

1. Create directory under `spring-boot-admin-samples/`
2. Follow existing sample structure
3. Add `README.md` with specific instructions
4. Include `docker-compose.yml` if applicable
5. Add tests
6. Update samples documentation

## See Also

- [Getting Started](../02-getting-started/) - Basic setup guide
- [Server Configuration](../02-server/01-server.mdx) - Server configuration options
- [Integration](../04-integration/) - Service discovery integration
- [Customization](../06-customization/) - UI and server customization
