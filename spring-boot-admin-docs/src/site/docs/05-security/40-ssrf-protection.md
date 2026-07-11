---
sidebar_position: 40
sidebar_custom_props:
  icon: 'shield'
---

# SSRF Protection

Spring Boot Admin Server includes opt-in protection against Server-Side Request Forgery (SSRF) attacks that can be
triggered through the instance registration API.

## The Risk

When a Spring Boot application registers with the Admin Server, it sends a `POST /instances` request containing
`healthUrl`, `managementUrl`, and `serviceUrl`. The Admin Server immediately begins making outbound HTTP requests to
those URLs to poll health status and discover actuator endpoints. It also exposes a proxy at
`/instances/{id}/actuator/**` that forwards requests verbatim to the registered management URL.

Without authentication or URL validation on `POST /instances`, an attacker can:

1. Register a fake instance pointing to `http://169.254.169.254/latest/meta-data/` (AWS IMDSv1)
2. The Admin Server polls the metadata endpoint automatically and stores the response
3. The attacker retrieves the response — including IAM credentials — through the actuator proxy

This applies to loopback addresses, RFC 1918 private ranges, and any internal service reachable from the server's
network, not just cloud metadata endpoints.

:::warning
SSRF protection is **disabled by default** to avoid breaking existing deployments where the Admin Server legitimately
communicates with services on private IP ranges. Enable it explicitly and provide a custom `InetAddressFilter` bean
for any intranet services that need to register.
:::

---

## Enabling SSRF Protection

Add the following to your `application.yml`:

```yaml
spring:
  boot:
    admin:
      ssrf-protection:
        enabled: true
```

When enabled, all URLs submitted during instance registration (`healthUrl`, `managementUrl`, `serviceUrl`) are
validated before the instance is stored. The proxy also re-validates the resolved target URL before each outbound
request.

---

## What Gets Blocked

When protection is enabled, address filtering is delegated to Spring Boot's
[`InetAddressFilter`](https://docs.spring.io/spring-boot/4.1/api/java/org/springframework/boot/http/client/InetAddressFilter.html).
The default filter (`InetAddressFilter.externalAddresses()`) blocks all non-external addresses, including:

| Category | Examples |
|---|---|
| Loopback | `localhost`, `127.0.0.1`, `127.x.x.x`, `::1` |
| Link-local (cloud metadata) | `169.254.0.0/16` — includes AWS IMDSv1 (`169.254.169.254`), GCP, Azure |
| RFC 1918 Class A | `10.0.0.0/8` |
| RFC 1918 Class B | `172.16.0.0/12` (172.16.x – 172.31.x) |
| RFC 1918 Class C | `192.168.0.0/16` |
| IPv6 link-local | `fe80::/10` |
| IPv6 unique-local | `fc00::/7` (fc and fd prefixes) |
| Unspecified address | `0.0.0.0` |
| Disallowed schemes | Anything other than `http` and `https` (e.g. `file://`, `ftp://`) |

Registration attempts targeting any of these return `400 Bad Request`. Proxy requests that resolve to a blocked address
return `403 Forbidden`.

:::note
Hostnames are **resolved via DNS** during validation. This means a public hostname that resolves to a private IP
(DNS rebinding) is also blocked — not just hostnames that literally look like private addresses.
:::

---

## Allowing Internal Services

If your Admin Server legitimately needs to reach services on private addresses — for example in an on-premises or
Kubernetes cluster deployment — you have two options:

### Option 1: Configuration properties (recommended)

Add entries to `allowed-cidrs`. Both individual IP addresses and CIDR subnet masks are supported for IPv4 and IPv6.
These ranges are ORed with the default `externalAddresses()` filter, so public addresses always remain reachable.

**Single host** (exact IP address):

```yaml
spring:
  boot:
    admin:
      ssrf-protection:
        enabled: true
        allowed-cidrs:
          - "192.168.1.100"      # single host, equivalent to 192.168.1.100/32
```

**Subnet** (CIDR mask):

```yaml
spring:
  boot:
    admin:
      ssrf-protection:
        enabled: true
        allowed-cidrs:
          - "192.168.1.0/24"     # all hosts in 192.168.1.x
```

**Multiple ranges** (subnets, whole classes, IPv6):

```yaml
spring:
  boot:
    admin:
      ssrf-protection:
        enabled: true
        allowed-cidrs:
          - "192.168.1.0/24"     # single subnet
          - "10.0.0.0/8"         # entire RFC 1918 Class A
          - "172.16.0.0/12"      # entire RFC 1918 Class B
          - "fd00::/8"           # IPv6 unique-local range
```

### Option 2: Custom `InetAddressFilter` bean

For more control, expose an `InetAddressFilter` bean. It replaces the auto-configured filter entirely.

```java
import org.springframework.boot.http.client.InetAddressFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SsrfConfig {

    @Bean
    public InetAddressFilter ssrfInetAddressFilter() {
        return InetAddressFilter.externalAddresses()
            .or("10.0.0.0/8")       // pod CIDR
            .or("172.16.0.0/12");   // service CIDR
    }
}
```

`InetAddressFilter` supports CIDR notation for both IPv4 and IPv6, and can be composed with `.or()`, `.and()`, and
`.andNot()`. See the
[Spring Boot reference documentation](https://docs.spring.io/spring-boot/4.1/reference/io/rest-client.html#io.rest-client.global-configuration.inetaddress-filtering)
for the full API.

---

## Allowing Additional Schemes

By default only `http` and `https` are permitted. To add a custom scheme:

```yaml
spring:
  boot:
    admin:
      ssrf-protection:
        enabled: true
        allowed-schemes:
          - http
          - https
          - grpc
```

---

## Configuration Reference

| Property | Type | Default | Description |
|---|---|---|---|
| `spring.boot.admin.ssrf-protection.enabled` | `boolean` | `false` | Enable SSRF URL validation |
| `spring.boot.admin.ssrf-protection.allowed-schemes` | `Set<String>` | `http, https` | URL schemes that are permitted |
| `spring.boot.admin.ssrf-protection.allowed-cidrs` | `List<String>` | _(empty)_ | IP addresses or CIDR ranges (IPv4 and IPv6) that are permitted in addition to public addresses. Examples: `192.168.1.0/24`, `10.0.0.0/8`, `fd00::/8` |

For finer-grained control over address filtering, expose a custom `InetAddressFilter` bean (see [Allowing Internal Services](#allowing-internal-services)).

---

## Providing a Custom Validator

Override the entire `SsrfUrlValidator` bean to implement fully custom logic:

```java
import de.codecentric.boot.admin.server.utils.SsrfUrlValidator;
import de.codecentric.boot.admin.server.config.AdminServerProperties;
import org.springframework.boot.http.client.InetAddressFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomSsrfConfig {

    @Bean
    public SsrfUrlValidator ssrfUrlValidator(AdminServerProperties properties,
                                             InetAddressFilter ssrfInetAddressFilter) {
        return new SsrfUrlValidator(properties.getSsrfProtection(), ssrfInetAddressFilter);
    }
}
```

---

## Dual Validation

SSRF protection runs at two points:

1. **Registration (`POST /instances`)** — `healthUrl`, `managementUrl`, and `serviceUrl` are validated before the
   instance is stored. Invalid registrations are rejected with `400 Bad Request`.

2. **Proxy (`/instances/{id}/actuator/**`)** — The resolved target URL is re-validated before each outbound request.
   This provides defence-in-depth against scenarios where an endpoint URL is assembled dynamically after registration.
   Blocked proxy requests return `403 Forbidden`.

---

## Additional Hardening

SSRF protection alone is not sufficient for a publicly accessible Admin Server. Combine it with:

- **Authentication on `POST /instances`** — The most effective mitigation. See [Server Authentication](./10-server-authentication.md).
- **AWS IMDSv2** — Require a `PUT` request with a TTL header to obtain a metadata token, which the Admin Server cannot
  provide without explicit support.
- **Network egress controls** — Firewall rules or security groups that prevent the Admin Server's outbound traffic from
  reaching metadata endpoints and internal services.
- **VPC/private network isolation** — Run the Admin Server in a subnet that has no route to sensitive internal services.

---

## See Also

- [Server Authentication](./10-server-authentication.md) - Require authentication before instance registration
- [CSRF Protection](./30-csrf-protection.md) - Protect the registration endpoint against cross-origin forged requests
