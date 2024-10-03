# HTTP headers

In case you need to inject custom HTTP headers into the requests made to the monitored application’s actuator endpoints you can easily add a `HttpHeadersProvider`:

```java
@Bean
public HttpHeadersProvider customHttpHeadersProvider() {
    return (instance) -> {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("X-CUSTOM", "My Custom Value");
        return httpHeaders;
    };
}
```
