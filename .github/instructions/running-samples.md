# Running Samples and UI Development

## Running the Servlet Sample

> Install all modules first — samples depend on locally built artifacts.

```bash
# 1. Install all modules
export PATH="/tmp/node-v22.18.0-linux-x64/bin:$PATH" && ./mvnw install -B --no-transfer-progress -DskipTests

# 2. Run the servlet sample
cd spring-boot-admin-samples/spring-boot-admin-sample-servlet
export PATH="/tmp/node-v22.18.0-linux-x64/bin:$PATH" && ../../mvnw spring-boot:run -Dspring-boot.run.profiles=dev,insecure
```

Access the UI at `http://localhost:8080`.  
Username: `user` — password shown in logs (or bypassed with `insecure` profile).  
Startup takes ~3 seconds and shows "all up" status with a registered instance.

## UI Development Mode

```bash
cd spring-boot-admin-server-ui
npm run build:watch   # rebuilds on file changes
```

Configure the Spring Boot Admin Server to serve from the local build:

```yaml
spring:
  boot:
    admin:
      ui:
        cache.no-cache: true
        resource-locations: file:../../spring-boot-admin-server-ui/target/dist/
        template-location: file:../../spring-boot-admin-server-ui/target/dist/
        cache-templates: false
```

## Manual Validation Checklist

After building and running, verify:

- [ ] Spring Boot Admin UI loads at `http://localhost:8080`
- [ ] Application registration shows `spring-boot-admin-sample-servlet` with **UP** status
- [ ] Navigation works: Applications, Wallboard, instance details
- [ ] Actuator endpoints are detected and accessible
- [ ] Instance details show health, metrics, loggers, etc.
