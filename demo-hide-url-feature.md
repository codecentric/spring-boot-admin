# Demo: Hide URL Feature in Spring Boot Admin

This guide demonstrates how to use the hide URL feature that was implemented for issue #4338.

## Prerequisites
- Java 17+
- Maven 3.6+
- Spring Boot Admin Server running

## Demo Setup

### 1. Start Spring Boot Admin Server
```bash
# Navigate to the sample directory
cd spring-boot-admin-samples/spring-boot-admin-sample-servlet

# Start with default configuration (URLs visible)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev,insecure
```

### 2. Create Test Applications

#### Application 1: With UI (URL should be visible)
Create `demo-app-with-ui/src/main/resources/application.yml`:
```yaml
spring:
  application:
    name: demo-app-with-ui
  boot:
    admin:
      client:
        url: http://localhost:8080
        instance:
          metadata:
            hide-url: "false"  # Explicitly show URL

server:
  port: 8081

management:
  endpoints:
    web:
      exposure:
        include: "*"
```

#### Application 2: Without UI (URL should be hidden)
Create `demo-app-without-ui/src/main/resources/application.yml`:
```yaml
spring:
  application:
    name: demo-app-without-ui
  boot:
    admin:
      client:
        url: http://localhost:8080
        instance:
          metadata:
            hide-url: "true"  # Hide URL

server:
  port: 8082

management:
  endpoints:
    web:
      exposure:
        include: "*"
```

### 3. Test Scenarios

#### Scenario 1: Per-Instance URL Hiding
1. Start both demo applications
2. Open Spring Boot Admin UI at http://localhost:8080
3. Observe:
   - `demo-app-with-ui` shows the service URL (http://localhost:8081)
   - `demo-app-without-ui` hides the service URL

#### Scenario 2: Global URL Hiding
1. Stop the Spring Boot Admin Server
2. Create `application-global-hide.yml`:
```yaml
spring:
  boot:
    admin:
      ui:
        hide-instance-url: true  # Hide all URLs globally
```
3. Start server with: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev,insecure -Dspring.config.location=classpath:/application-global-hide.yml`
4. Observe: All service URLs are hidden regardless of individual metadata settings

#### Scenario 3: Mixed Configuration
1. Use default server config (URLs visible)
2. Start applications with different metadata:
   - App A: `hide-url: "true"` → URL hidden
   - App B: `hide-url: "false"` → URL visible
   - App C: No metadata → URL visible (default)

## Expected Behavior

### When URLs are visible:
- Service URLs appear as clickable links in the instance list
- "Service", "Management", and "Health" buttons appear in instance details
- Users can navigate directly to application endpoints

### When URLs are hidden:
- Service URLs are not displayed in the instance list
- Navigation buttons are hidden in instance details
- Only the instance ID and status information are shown

## Configuration Examples

### Hide URLs for all instances (Server config)
```yaml
spring:
  boot:
    admin:
      ui:
        hide-instance-url: true
```

### Hide URL for specific application (Client config)
```yaml
spring:
  boot:
    admin:
      client:
        instance:
          metadata:
            hide-url: "true"
```

### Show URL explicitly (Client config)
```yaml
spring:
  boot:
    admin:
      client:
        instance:
          metadata:
            hide-url: "false"
```

## Verification

To verify the feature is working:

1. **Check UI**: URLs should be hidden/shown based on configuration
2. **Check Network**: No requests should be made to hidden URLs
3. **Check Logs**: No 404 errors from clicking on hidden URLs

## Troubleshooting

### URLs not hiding:
- Check that the metadata key is exactly `hide-url` (with hyphen)
- Verify the value is exactly `"true"` (string)
- Ensure global setting is not overriding per-instance setting

### URLs hiding when they shouldn't:
- Check global `hide-instance-url` setting
- Verify metadata value is not `"true"`
- Check for typos in configuration

## Benefits Demonstrated

1. **Cleaner UI**: No broken links or 404 errors
2. **Better UX**: Users only see functional URLs
3. **Security**: Prevents accidental access to non-functional endpoints
4. **Flexibility**: Mix of visible and hidden URLs as needed

This demo shows how the feature successfully addresses the original issue #4338 by providing granular control over URL visibility in Spring Boot Admin. 