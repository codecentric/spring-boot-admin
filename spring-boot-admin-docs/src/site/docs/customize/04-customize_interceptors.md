---
sidebar_custom_props:
  icon: 'http'
---

# HTTP Interceptors

You can intercept and modify requests and responses made to the monitored application’s actuator endpoints by implementing the `InstanceExchangeFilterFunction` interface. This can be useful for auditing or adding some extra security checks.

```java title="CustomHttpInterceptor.java"
@Bean
public InstanceExchangeFilterFunction auditLog() {
    return (instance, request, next) -> next.exchange(request).doOnSubscribe((s) -> {
        if (HttpMethod.DELETE.equals(request.method()) || HttpMethod.POST.equals(request.method())) {
            log.info("{} for {} on {}", request.method(), instance.getId(), request.url());
        }
    });
}
```
