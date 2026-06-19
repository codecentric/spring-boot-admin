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
communicates with services on private IP ranges. Enable it explicitly and configure an allowlist for any intranet
services that need to register.
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

When protection is enabled, the following are rejected by default:

| Category | Examples |
|---|---|
| Loopback | `localhost`, `127.0.0.1`, `127.x.x.x`, `::1` |
| Link-local (cloud metadata) | `169.254.0.0/16` — includes AWS IMDSv1 (`169.254.169.254`), GCP, Azure |
| RFC 1918 Class A | `10.0.0.0/8` |
| RFC 1918 Class B | `172.16.0.0/12` (172.16.x – 172.31.x) |
| RFC 1918 Class C | `192.168.0.0/16` |
| IPv6 link-local | `fe80::/10` |
| IPv6 unique-local | `fc00::/7` (fc and fd prefixes) |
| IPv4-mapped IPv6 | `::ffff:` prefix embedding a private IPv4 |
| Unspecified address | `0.0.0.0` |
| Disallowed schemes | Anything other than `http` and `https` (e.g. `file://`, `ftp://`) |

Registration attempts targeting any of these return `400 Bad Request`. Proxy requests that resolve to a blocked address
return `403 Forbidden`.

:::note
Hostname-to-IP resolution is **not** performed during validation. Only the literal hostname string from the URL is
checked. An attacker who controls a public DNS record pointing to a private IP (DNS rebinding) is not blocked by this
validator alone. Use IMDSv2 on AWS or network-level egress controls as additional layers of defence.
:::

---

## Allowing Internal Services

If your Admin Server legitimately needs to reach services on private addresses — for example in an on-premises or
Kubernetes cluster deployment — add those hosts to the allowlist. An allowlisted host bypasses all block checks.

### Exact host match

```yaml
spring:
  boot:
    admin:
      ssrf-protection:
        enabled: true
        allowed-hosts:
          - 192.168.1.100
          - monitoring-service.internal
```

### Glob-style suffix pattern

Use `*.suffix` to allow an entire subdomain:

```yaml
spring:
  boot:
    admin:
      ssrf-protection:
        enabled: true
        allowed-hosts:
          - "*.svc.cluster.local"   # all Kubernetes services
          - "*.internal.corp"
```

The glob `*.svc.cluster.local` matches `my-service.svc.cluster.local` but not `svc.cluster.local` itself. Matching
is case-insensitive.

---

## Blocking Additional Hosts

To block hostnames beyond the built-in private ranges — for example internal domains that should never register — add
regex patterns to `blocked-host-patterns`:

```yaml
spring:
  boot:
    admin:
      ssrf-protection:
        enabled: true
        blocked-host-patterns:
          - ".*\\.internal\\.corp$"
          - "metadata\\.google\\.internal"
```

Patterns are matched against the raw hostname using `java.util.regex.Pattern`. Invalid patterns are logged as warnings
and skipped.

:::note
The allowlist takes precedence over blocked patterns. A host matching both an `allowed-hosts` entry and a
`blocked-host-patterns` entry is **allowed**.
:::

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
| `spring.boot.admin.ssrf-protection.allowed-hosts` | `List<String>` | _(empty)_ | Hosts exempt from all block checks. Supports exact names and `*.suffix` glob patterns |
| `spring.boot.admin.ssrf-protection.blocked-host-patterns` | `List<String>` | _(empty)_ | Additional Java regex patterns matched against the raw hostname |

---

## Providing a Custom Validator

Override the default `SsrfUrlValidator` bean to implement custom logic — for example, DNS resolution or CIDR matching:

```java
import de.codecentric.boot.admin.server.utils.SsrfUrlValidator;
import de.codecentric.boot.admin.server.config.AdminServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomSsrfConfig {

    @Bean
    public SsrfUrlValidator ssrfUrlValidator(AdminServerProperties properties) {
        AdminServerProperties.SsrfProtectionProperties ssrfProps =
            properties.getSsrfProtection();
        // Wrap or extend the default validator
        SsrfUrlValidator defaultValidator = new SsrfUrlValidator(ssrfProps);
        return url -> {
            defaultValidator.validate(url);
            // Add custom checks here
        };
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
