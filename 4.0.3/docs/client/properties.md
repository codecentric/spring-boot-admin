# Properties

<!-- -->

| Property                                                                                                                                                                                                                                         |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `spring.boot.admin.client.url`The admin server urls to register at- Type: 

  `java.lang.String[]`

- Default: 

  `[]`                                                                                                                                |
| `spring.boot.admin.client.period`Time interval the registration is repeated- Type: 

  `java.time.Duration`

- Default: 

  `"10000ms"`                                                                                                                |
| `spring.boot.admin.client.enabled`Enable Spring Admin Client.- Type: 

  `java.lang.Boolean`

- Default: 

  `"true"`                                                                                                                                  |
| `spring.boot.admin.client.api-path`The admin rest-apis path.- Type: 

  `java.lang.String`

- Default: 

  `"instances"`                                                                                                                               |
| `spring.boot.admin.client.password`Password for basic authentication on admin server- Type: 

  `java.lang.String`                                                                                                                                 |
| `spring.boot.admin.client.username`Username for basic authentication on admin server- Type: 

  `java.lang.String`                                                                                                                                 |
| `spring.boot.admin.client.read-timeout`Read timeout (in ms) for the registration.- Type: 

  `java.time.Duration`

- Default: 

  `"5000ms"`                                                                                                           |
| `spring.boot.admin.client.instance.name`Name to register with. Defaults to ${spring.application.name}- Type: 

  `java.lang.String`

- Default: 

  `"spring-boot-application"`                                                                        |
| `spring.boot.admin.client.register-once`Enable registration against one or all admin servers- Type: 

  `java.lang.Boolean`

- Default: 

  `true`                                                                                                     |
| `spring.boot.admin.client.connect-timeout`Connect timeout for the registration.- Type: 

  `java.time.Duration`

- Default: 

  `"5000ms"`                                                                                                             |
| `spring.boot.admin.client.auto-registration`Enable automatic registration when the application is ready.- Type: 

  `java.lang.Boolean`

- Default: 

  `true`                                                                                         |
| `spring.boot.admin.client.instance.metadata`Metadata that should be associated with this application- Type: 

  `java.util.Map<java.lang.String,java.lang.String>`                                                                                 |
| `spring.boot.admin.client.instance.prefer-ip`Should the registered urls be built with server.address or with hostname. @deprecated Use serviceHostType instead.- Type: 

  `java.lang.Boolean`                                                     |
| `spring.boot.admin.client.auto-deregistration`Enable automatic deregistration on shutdown If not set it defaults to true if an active {@link CloudPlatform} is present;- Type: 

  `java.lang.Boolean`                                             |
| `spring.boot.admin.client.instance.health-url`Client-health-URL to register with. Inferred at runtime, can be overridden in case the reachable URL is different (e.g. Docker). Must be unique all services registry.- Type: 

  `java.lang.String` |
| `spring.boot.admin.client.instance.service-url`Client-service-URL register with. Inferred at runtime, can be overridden in case the reachable URL is different (e.g. Docker).- Type: 

  `java.lang.String`                                        |
| `spring.boot.admin.client.instance.service-path`Path for computing the service-url to register with. If not specified, defaults to "/"- Type: 

  `java.lang.String`                                                                               |
| `spring.boot.admin.client.instance.management-url`Management-url to register with. Inferred at runtime, can be overridden in case the reachable URL is different (e.g. Docker).- Type: 

  `java.lang.String`                                      |
| `spring.boot.admin.client.instance.service-base-url`Base url for computing the service-url to register with. The path is inferred at runtime, and appended to the base url.- Type: 

  `java.lang.String`                                          |
| `spring.boot.admin.client.instance.service-host-type`Should the registered urls be built with server.address or with hostname.- Type: 

  `de.codecentric.boot.admin.client.config.ServiceHostType`

- Default: 

  `"canonical-host-name"`            |
| `spring.boot.admin.client.instance.management-base-url`Base url for computing the management-url to register with. The path is inferred at runtime, and appended to the base url.- Type: 

  `java.lang.String`                                    |
