# Properties

<!-- -->

| Property                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `spring.boot.admin.context-path`The context-path prefixes the path where the Admin Servers static assets and api should be served, relative to the Dispatcher-Servlet.- Type: 

  `java.lang.String`                                                                                                                                                                                                                                                                                                                                                                                                                                |
| `spring.boot.admin.monitor.period`- Type: 

  `java.lang.Long`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| `spring.boot.admin.server.enabled`Enable Spring Boot Admin Server Default: true- Type: 

  `java.lang.Boolean`

- Default: 

  `true`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| `spring.boot.admin.monitor.retries`Number of retries per endpointId. Defaults to default-retry.- Type: 

  `java.util.Map<java.lang.String,java.lang.Integer>`                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| `spring.boot.admin.monitor.timeout`timeout per endpointId. Defaults to default-timeout.- Type: 

  `java.util.Map<java.lang.String,java.time.Duration>`                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| `spring.boot.admin.probed-endpoints`For Spring Boot 2.x applications the endpoints should be discovered automatically using the actuator links. For Spring Boot 1.x applications SBA probes for the specified endpoints using an OPTIONS request. If the path differs from the id you can specify this as id:path (e.g. health:ping).- Type: 

  `java.lang.String[]`

- Default: 

  `["health","env","metrics","httptrace:trace","httptrace","threaddump:dump","threaddump","jolokia","info","logfile","refresh","flyway","liquibase","heapdump","loggers","auditevents","mappings","scheduledtasks","configprops","caches","beans"]` |
| `spring.boot.admin.hazelcast.enabled`Enable Hazelcast support.- Type: 

  `java.lang.Boolean`

- Default: 

  `"true"`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| `spring.boot.admin.monitor.read-timeout`- Type: 

  `java.lang.Long`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| `spring.boot.admin.hazelcast.event-store`Name of backing Hazelcast-Map for storing the instance events.- Type: 

  `java.lang.String`

- Default: 

  `"spring-boot-admin-application-store"`                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| `spring.boot.admin.instance-auth.enabled`Whether or not to use configuration properties as a source for instance credentials<br />Default: true- Type: 

  `java.lang.Boolean`

- Default: 

  `true`                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| `spring.boot.admin.monitor.info-interval`Time interval to check the info of instances,- Type: 

  `java.time.Duration`

- Default: 

  `"1m"`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| `spring.boot.admin.monitor.info-lifetime`Lifetime of info. The info won't be updated as long the last info isn't expired.- Type: 

  `java.time.Duration`

- Default: 

  `"1m"`                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `spring.boot.admin.monitor.connect-timeout`- Type: 

  `java.lang.Long`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| `spring.boot.admin.monitor.default-retries`Default number of retries for failed requests. Individual values for specific endpoints can be overriden using \`spring.boot.admin.monitor.retries.\*\`.- Type: 

  `java.lang.Integer`

  0                                                                                                                                                                                                                                                                                                                                                                                               |
| `spring.boot.admin.monitor.default-timeout`Default timeout when making requests. Individual values for specific endpoints can be overriden using \`spring.boot.admin.monitor.timeout.\*\`.- Type: 

  `java.time.Duration`

- Default: 

  `"10000ms"`                                                                                                                                                                                                                                                                                                                                                                                  |
| `spring.boot.admin.monitor.status-interval`Time interval to check the status of instances, must be greater than 1 second.- Type: 

  `java.time.Duration`

- Default: 

  `"10000ms"`                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| `spring.boot.admin.monitor.status-lifetime`Lifetime of status. The status won't be updated as long the last status isn't expired.- Type: 

  `java.time.Duration`

- Default: 

  `"10000ms"`                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| `spring.boot.admin.monitor.info-max-backoff`The maximal backoff for info check retries (retry after error has exponential backoff, minimum backoff is 1 second).- Type: 

  `java.time.Duration`

- Default: 

  `"10m"`                                                                                                                                                                                                                                                                                                                                                                                                                |
| `spring.boot.admin.instance-auth.service-map`Map of instance credentials per registered service name- Type: 

  `java.util.Map<java.lang.String,de.codecentric.boot.admin.server.web.client.BasicAuthHttpHeaderProvider$InstanceCredentials>`                                                                                                                                                                                                                                                                                                                                                                                       |
| `spring.boot.admin.metadata-keys-to-sanitize`The metadata keys which should be sanitized when serializing to json- Type: 

  `java.lang.String[]`

- Default: 

  `[".*password$",".*secret$",".*key$",".*token$",".*credentials.*",".*vcap_services$"]`                                                                                                                                                                                                                                                                                                                                                                                |
| `spring.boot.admin.monitor.status-max-backoff`The maximal backoff for status check retries (retry after error has exponential backoff, minimum backoff is 1 second).- Type: 

  `java.time.Duration`

- Default: 

  `"60000ms"`                                                                                                                                                                                                                                                                                                                                                                                                        |
| `spring.boot.admin.hazelcast.sent-notifications`Name of backing Hazelcast-Map for storing the sent notifications.- Type: 

  `java.lang.String`

- Default: 

  `"spring-boot-admin-sent-notifications"`                                                                                                                                                                                                                                                                                                                                                                                                                                |
| `spring.boot.admin.instance-auth.default-password`Default userpassword used for authentication to each instance. Individual values for specific instances can be overriden using \`spring.boot.admin.instance-auth.service-map.\*.user-password\`.<br />Default: null- Type: 

  `java.lang.String`                                                                                                                                                                                                                                                                                                                                 |
| `spring.boot.admin.instance-proxy.ignored-headers`Headers not to be forwarded when making requests to clients.- Type: 

  `java.util.Set<java.lang.String>`                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| `spring.boot.admin.instance-auth.default-user-name`Default username used for authentication to each instance. Individual values for specific instances can be overriden using \`spring.boot.admin.instance-auth.service-map.\*.user-name\`.<br />Default: null- Type: 

  `java.lang.String`                                                                                                                                                                                                                                                                                                                                        |
