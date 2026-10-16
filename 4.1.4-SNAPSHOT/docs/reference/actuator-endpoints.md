# Supported Spring Boot Actuator Endpoints

Below is a comprehensive list of actuator endpoints which are supported by Spring Boot Admin.

Spring Boot Admin detects the endpoints exposed by each registered instance and only shows the corresponding views and actions in the UI if the endpoint is available. Endpoints are grouped by the project providing them, as some of them require additional dependencies on the client application (e.g. Spring Cloud).

## Spring Boot[​](#spring-boot "Direct link to Spring Boot")

These endpoints are provided by [Spring Boot Actuator](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html).

| Actuator Endpoint           | Used in Spring Boot Admin                                         | Documentation Link                                                                                     |
| --------------------------- | ----------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------ |
| `/actuator/auditevents`     | *Audit Log* view                                                  | [Audit Events](https://docs.spring.io/spring-boot/api/rest/actuator/auditevents.html#page-title)       |
| `/actuator/beans`           | *Beans* view                                                      | [Beans](https://docs.spring.io/spring-boot/api/rest/actuator/beans.html#page-title)                    |
| `/actuator/caches`          | *Caches* view                                                     | [Caches](https://docs.spring.io/spring-boot/api/rest/actuator/caches.html#page-title)                  |
| `/actuator/conditions`      | *Conditions* view                                                 | [Conditions](https://docs.spring.io/spring-boot/api/rest/actuator/conditions.html#page-title)          |
| `/actuator/configprops`     | *Configuration Properties* view                                   | [Config Props](https://docs.spring.io/spring-boot/api/rest/actuator/configprops.html#page-title)       |
| `/actuator/env`             | *Environment* view (read-only, see [below](#environment-manager)) | [Environment](https://docs.spring.io/spring-boot/api/rest/actuator/env.html#page-title)                |
| `/actuator/flyway`          | *Flyway* view                                                     | [Flyway](https://docs.spring.io/spring-boot/api/rest/actuator/flyway.html#page-title)                  |
| `/actuator/health`          | Status and *Details* view                                         | [Health](https://docs.spring.io/spring-boot/api/rest/actuator/health.html#page-title)                  |
| `/actuator/heapdump`        | *Heap Dump* download                                              | [Heap Dump](https://docs.spring.io/spring-boot/api/rest/actuator/heapdump.html#page-title)             |
| `/actuator/httpexchanges`   | *HTTP Exchanges* view                                             | [HTTP Exchanges](https://docs.spring.io/spring-boot/api/rest/actuator/httpexchanges.html#page-title)   |
| `/actuator/httptrace`       | *HTTP Traces* view (Spring Boot 2 only)                           | [HTTP Trace](https://docs.spring.io/spring-boot/docs/2.1.7.RELEASE/actuator-api/html/#http-trace)      |
| `/actuator/info`            | *Details* view                                                    | [Info](https://docs.spring.io/spring-boot/api/rest/actuator/info.html#page-title)                      |
| `/actuator/liquibase`       | *Liquibase* view                                                  | [Liquibase](https://docs.spring.io/spring-boot/api/rest/actuator/liquibase.html#page-title)            |
| `/actuator/logfile`         | *Logfile* view                                                    | [Logfile](https://docs.spring.io/spring-boot/api/rest/actuator/logfile.html#page-title)                |
| `/actuator/loggers`         | *Loggers* view                                                    | [Loggers](https://docs.spring.io/spring-boot/api/rest/actuator/loggers.html#page-title)                |
| `/actuator/mappings`        | *Mappings* view                                                   | [Mappings](https://docs.spring.io/spring-boot/api/rest/actuator/mappings.html#page-title)              |
| `/actuator/metrics`         | *Details* and *Metrics* view                                      | [Metrics](https://docs.spring.io/spring-boot/api/rest/actuator/metrics.html#page-title)                |
| `/actuator/quartz`          | *Quartz* view                                                     | [Quartz](https://docs.spring.io/spring-boot/api/rest/actuator/quartz.html#page-title)                  |
| `/actuator/quartz/jobs`     | *Quartz* view                                                     | [Quartz Jobs](https://docs.spring.io/spring-boot/api/rest/actuator/quartz.html#page-title)             |
| `/actuator/quartz/triggers` | *Quartz* view                                                     | [Quartz Triggers](https://docs.spring.io/spring-boot/api/rest/actuator/quartz.html#page-title)         |
| `/actuator/sbom`            | *Dependency trees* view                                           | [SBOM](https://docs.spring.io/spring-boot/api/rest/actuator/sbom.html#page-title)                      |
| `/actuator/scheduledtasks`  | *Scheduled Tasks* view                                            | [Scheduled Tasks](https://docs.spring.io/spring-boot/api/rest/actuator/scheduledtasks.html#page-title) |
| `/actuator/sessions`        | *Sessions* view                                                   | [Sessions](https://docs.spring.io/spring-boot/api/rest/actuator/sessions.html#page-title)              |
| `/actuator/shutdown`        | *Shutdown* action in the applications list                        | [Shutdown](https://docs.spring.io/spring-boot/api/rest/actuator/shutdown.html#page-title)              |
| `/actuator/startup`         | *Startup* view                                                    | [Startup](https://docs.spring.io/spring-boot/api/rest/actuator/startup.html#page-title)                |
| `/actuator/threaddump`      | *Thread Dump* view                                                | [Thread Dump](https://docs.spring.io/spring-boot/api/rest/actuator/threaddump.html#page-title)         |

## Spring Cloud Commons[​](#spring-cloud-commons "Direct link to Spring Cloud Commons")

These endpoints are provided by [Spring Cloud Commons](https://docs.spring.io/spring-cloud-commons/reference/spring-cloud-commons/application-context-services.html#endpoints) and require `spring-cloud-context` on the classpath of the client application.

| Actuator Endpoint                 | Used in Spring Boot Admin                          | Documentation Link                                                                                                                  |
| --------------------------------- | -------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------- |
| `/actuator/env` (`POST`/`DELETE`) | *Environment Manager* in the *Environment* view    | [Endpoints](https://docs.spring.io/spring-cloud-commons/reference/spring-cloud-commons/application-context-services.html#endpoints) |
| `/actuator/refresh`               | *Refresh context* button in the *Environment* view | [Endpoints](https://docs.spring.io/spring-cloud-commons/reference/spring-cloud-commons/application-context-services.html#endpoints) |
| `/actuator/restart`               | *Restart* action in the applications list          | [Endpoints](https://docs.spring.io/spring-cloud-commons/reference/spring-cloud-commons/application-context-services.html#endpoints) |

### Environment Manager[​](#environment-manager "Direct link to Environment Manager")

Spring Boot's `/actuator/env` endpoint is read-only. Spring Cloud Commons extends it so that properties can be set (`POST`) and reset (`DELETE`) at runtime. Spring Boot Admin sends an `OPTIONS` request to `/actuator/env` and shows the *Environment Manager* only if the `Allow` response header contains `POST`.

To enable it on the client application:

```
management:

  endpoint:

    env:

      post:

        enabled: true

        valid-keys-regex: "^(logging\\.level\\..*|my\\.app\\..*)$" # restrict the properties that may be changed

  endpoints:

    web:

      exposure:

        include: env, refresh
```

After updating properties, use *Refresh context* to re-bind `@ConfigurationProperties` and re-create `@RefreshScope` beans without restarting the application.

warning

Changing the environment at runtime is a powerful feature. Make sure the actuator endpoints are [secured](/4.1.4-SNAPSHOT/docs/security/actuator-security.md) and limit the modifiable properties using `management.endpoint.env.post.valid-keys-regex`.

### Refresh vs. Restart[​](#refresh-vs-restart "Direct link to Refresh vs. Restart")

| Action            | Endpoint            | Effect                                                                                                                             |
| ----------------- | ------------------- | ---------------------------------------------------------------------------------------------------------------------------------- |
| *Refresh context* | `/actuator/refresh` | Reloads the configuration, re-binds `@ConfigurationProperties` and refreshes `@RefreshScope` beans. The application keeps running. |
| *Restart*         | `/actuator/restart` | Closes and restarts the `ApplicationContext` (disabled by default in Spring Cloud).                                                |

## Spring Cloud Bus[​](#spring-cloud-bus "Direct link to Spring Cloud Bus")

These endpoints are provided by [Spring Cloud Bus](https://docs.spring.io/spring-cloud-bus/reference/).

| Actuator Endpoint      | Used in Spring Boot Admin                                   | Documentation Link                                                |
| ---------------------- | ----------------------------------------------------------- | ----------------------------------------------------------------- |
| `/actuator/busrefresh` | *Spring Cloud Bus-refresh* button in the *Environment* view | [Bus Refresh](https://docs.spring.io/spring-cloud-bus/reference/) |

## Spring Cloud Gateway[​](#spring-cloud-gateway "Direct link to Spring Cloud Gateway")

These endpoints are provided by [Spring Cloud Gateway](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webflux/actuator-api.html) and are only available on gateway applications.

| Actuator Endpoint                 | Used in Spring Boot Admin                    | Documentation Link                                                                                                            |
| --------------------------------- | -------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------- |
| `/actuator/gateway/globalfilters` | *Gateway* view                               | [Global Filters](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webflux/actuator-api.html) |
| `/actuator/gateway/routes`        | *Gateway* view (list, add and delete routes) | [Routes](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webflux/actuator-api.html)         |
| `/actuator/gateway/refresh`       | *Refresh Routes Cache* in the *Gateway* view | [Refresh](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webflux/actuator-api.html)        |

note

`/actuator/gateway/refresh` only refreshes the route cache of Spring Cloud Gateway. It is not related to `/actuator/refresh` from Spring Cloud Commons.

## Others[​](#others "Direct link to Others")

| Actuator Endpoint   | Used in Spring Boot Admin | Documentation Link                                               |
| ------------------- | ------------------------- | ---------------------------------------------------------------- |
| `/actuator/jolokia` | *JMX* view                | [Jolokia](https://jolokia.org/reference/html/manual/spring.html) |
