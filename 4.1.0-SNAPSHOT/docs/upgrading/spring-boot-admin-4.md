# To Spring Boot Admin 4

This guide covers the breaking changes, deprecated features, and migration steps required to upgrade from Spring Boot Admin 3.x to 4.x.

## Prerequisites[​](#prerequisites "Direct link to Prerequisites")

Before upgrading to Spring Boot Admin 4, ensure your application meets these requirements:

* **Spring Boot 4.0+** - Spring Boot Admin 4 requires Spring Boot 4.0 or higher
* **Java 17+** - Minimum Java version is 17
* **Review dependencies** - Check that all third-party dependencies are compatible with Spring Boot 4

Java Version Compatibility

Spring Boot Admin strives to support the same Java baseline version as the corresponding Spring Boot version. This means:

* Spring Boot Admin 4.x supports the same minimum Java version as Spring Boot 4.x (Java 17+)
* Future Spring Boot Admin releases will align with Spring Boot's Java requirements

Always check the [Spring Boot documentation](https://docs.spring.io/spring-boot/system-requirements.html) for the supported Java versions of your Spring Boot version.

***

## Breaking Changes[​](#breaking-changes "Direct link to Breaking Changes")

### 1. Nullable Annotations Change[​](#1-nullable-annotations-change "Direct link to 1. Nullable Annotations Change")

**What Changed:**

Spring Boot Admin 4 replaces Spring's nullable annotations with JSpecify annotations for better null-safety across the Java ecosystem.

**Migration:**

```
// Before (Spring Boot Admin 3.x)
import org.springframework.lang.Nullable;

public class MyService {
    public void process(@Nullable String value) {
        // ...
    }
}
```

```
// After (Spring Boot Admin 4.x)
import org.jspecify.annotations.Nullable;

public class MyService {
    public void process(@Nullable String value) {
        // ...
    }
}
```

**Action Required:**

If you extend Spring Boot Admin classes or implement interfaces using `@Nullable` annotations:

1. Add JSpecify dependency to your `pom.xml`:

```
<dependency>
    <groupId>org.jspecify</groupId>
    <artifactId>jspecify</artifactId>
    <version>1.0.0</version>
</dependency>
```

2. Update your imports from `org.springframework.lang.Nullable` to `org.jspecify.annotations.Nullable`

***

### 2. HTTP Client Configuration Changes[​](#2-http-client-configuration-changes "Direct link to 2. HTTP Client Configuration Changes")

**What Changed:**

Spring Boot Admin 4 modernizes HTTP client usage:

* **Client**: Now uses `RestClient` exclusively (replaces `WebClient` autoconfiguration)
* **Server**: Uses `WebClient` for instance communication and `RestTemplate` for notifiers

**Migration:**

#### For Admin Client[​](#for-admin-client "Direct link to For Admin Client")

The client autoconfiguration now provides `RestClient` instead of `WebClient`:

```
// Before (Spring Boot Admin 3.x)
@Bean
public WebClient.Builder webClientBuilder() {
    return WebClient.builder()
        .defaultHeader("X-Custom-Header", "value");
}
```

```
// After (Spring Boot Admin 4.x)
@Bean
public RestClient.Builder restClientBuilder() {
    return RestClient.builder()
        .defaultHeader("X-Custom-Header", "value");
}
```

#### For Admin Server[​](#for-admin-server "Direct link to For Admin Server")

No changes required - the server continues using `WebClient` for instance communication:

```
// Server-side customization (unchanged)
@Bean
public InstanceWebClient instanceWebClient(WebClient.Builder builder) {
    return InstanceWebClient.builder(builder)
        .connectTimeout(Duration.ofSeconds(5))
        .build();
}
```

**Action Required:**

* If you customize the client's HTTP configuration, migrate from `WebClient.Builder` to `RestClient.Builder`
* Update any custom beans that depend on `WebClient` in client applications

***

### 3. Property Rename: `prefer-ip` Removed[​](#3-property-rename-prefer-ip-removed "Direct link to 3-property-rename-prefer-ip-removed")

**What Changed:**

The property `spring.boot.admin.client.instance.prefer-ip` has been removed in favor of the more flexible `spring.boot.admin.client.instance.service-host-type`.

**Migration:**

```
# Before (Spring Boot Admin 3.x)
spring:
  boot:
    admin:
      client:
        instance:
          prefer-ip: true
```

```
# After (Spring Boot Admin 4.x)
spring:
  boot:
    admin:
      client:
        instance:
          service-host-type: IP  # Options: IP, HOST_NAME, CANONICAL_HOST_NAME
```

**Available Options:**

| Value                 | Description                                          |
| --------------------- | ---------------------------------------------------- |
| `IP`                  | Use IP address (equivalent to old `prefer-ip: true`) |
| `HOST_NAME`           | Use hostname (equivalent to old `prefer-ip: false`)  |
| `CANONICAL_HOST_NAME` | Use canonical hostname                               |

**Action Required:**

* Search your configuration files for `prefer-ip`
* Replace with `service-host-type: IP` (if `prefer-ip: true`) or `service-host-type: HOST_NAME` (if `prefer-ip: false`)

***

### 4. Jolokia Compatibility[​](#4-jolokia-compatibility "Direct link to 4. Jolokia Compatibility")

**What Changed:**

The current stable Jolokia version (2.4.2) does not yet support Spring Boot 4. Spring Boot Admin 4 temporarily downgrades to **Jolokia 2.1.0** for basic JMX functionality.

**Limitations:**

* Some advanced Jolokia features may not be available
* JMX operations work but with reduced functionality compared to Jolokia 2.4.2

**Future Outlook:**

Spring Boot Admin will upgrade to a newer Jolokia version once Spring Boot 4 support is added. Monitor the [Jolokia project](https://github.com/jolokia/jolokia) for updates on Spring Boot 4 compatibility.

**Action Required:**

* **No immediate action needed** - Jolokia 2.1.0 is included automatically and provides basic JMX functionality
* Test your JMX operations to ensure they work with the limited feature set
* If JMX functionality is critical, consider waiting for full Jolokia support before upgrading

***

## Migration Checklist[​](#migration-checklist "Direct link to Migration Checklist")

Follow these steps to ensure a smooth upgrade:

### Step 1: Update Dependencies[​](#step-1-update-dependencies "Direct link to Step 1: Update Dependencies")

Update your `pom.xml`:

```
<properties>
    <spring-boot.version>4.0.0</spring-boot.version>
    <spring-boot-admin.version>4.0.0</spring-boot-admin.version>
</properties>

<dependencies>
    <!-- Admin Server -->
    <dependency>
        <groupId>de.codecentric</groupId>
        <artifactId>spring-boot-admin-starter-server</artifactId>
        <version>${spring-boot-admin.version}</version>
    </dependency>

    <!-- Admin Client -->
    <dependency>
        <groupId>de.codecentric</groupId>
        <artifactId>spring-boot-admin-starter-client</artifactId>
        <version>${spring-boot-admin.version}</version>
    </dependency>
</dependencies>
```

### Step 2: Update Configuration[​](#step-2-update-configuration "Direct link to Step 2: Update Configuration")

1. **Replace `prefer-ip` property:**

```
# Find and replace in all configuration files
grep -r "prefer-ip" src/main/resources/
# Replace with service-host-type
```

2. **Review HTTP client customizations:**

```
# Check for WebClient customizations in client apps
grep -r "WebClient.Builder" src/main/java/
```

### Step 3: Update Code[​](#step-3-update-code "Direct link to Step 3: Update Code")

1. **Update nullable annotations:**

```
# Find all Spring nullable imports
find src -name "*.java" -exec grep -l "org.springframework.lang.Nullable" {} \;

# Replace with JSpecify
sed -i 's/org.springframework.lang.Nullable/org.jspecify.annotations.Nullable/g' <files>
```

2. **Migrate client HTTP configuration:**

Review and update any beans creating `WebClient.Builder` for the Admin Client.

### Step 4: Test[​](#step-4-test "Direct link to Step 4: Test")

1. **Start the Admin Server:**

```
mvn spring-boot:run
```

2. **Register a client application**

3. **Verify functionality:**

   * Instance registration works
   * Health checks update correctly
   * Actuator endpoints are accessible
   * Notifications fire properly
   * JMX operations work (with Jolokia 2.1.0 limitations)

### Step 5: Monitor Logs[​](#step-5-monitor-logs "Direct link to Step 5: Monitor Logs")

Watch for deprecation warnings or errors:

```
tail -f logs/spring-boot-admin.log | grep -i "deprecat\|error\|warn"
```

***

## Getting Help[​](#getting-help "Direct link to Getting Help")

If you encounter issues during the upgrade:

1. **Check the changelog**: Review detailed changes in the [release notes](https://github.com/codecentric/spring-boot-admin/releases)
2. **Search existing issues**: [GitHub Issues](https://github.com/codecentric/spring-boot-admin/issues)
3. **Ask the community**: [Stack Overflow](https://stackoverflow.com/questions/tagged/spring-boot-admin) with tag `spring-boot-admin`
4. **Report bugs**: Create an issue on [GitHub](https://github.com/codecentric/spring-boot-admin/issues/new)

***

## Summary[​](#summary "Direct link to Summary")

**Key Changes:**

* ✅ Update to Spring Boot 4.0+
* ✅ Replace `org.springframework.lang.Nullable` with `org.jspecify.annotations.Nullable`
* ✅ Migrate client from `WebClient` to `RestClient`
* ✅ Change `prefer-ip` to `service-host-type`
* ⚠️ Accept Jolokia 2.1.0 limitations temporarily

Most applications can upgrade with minimal code changes, primarily focused on configuration updates and dependency management.
