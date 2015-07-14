spring-boot-starter-admin-client
================================

This [Spring-Boot starter](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#using-boot-starter-poms "Spring Boot docu") that provides services and controllers that a required if an application show be able to be administrated with the [spring-boot-admin application](https://github.com/codecentric/spring-boot-admin "GitHub project").

This client uses the [AutoConfiguration](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#using-boot-auto-configuration "Spring Boot docu") feature of Spring Boot to register service and controller beans in the application context.

The main service that is used is a registrar that registeres the application at the spring-boot-admin application by periodically calling a REST-API to perform the registration of itself.

##Configuration properties
### spring-boot-admin
| Name                  | Description |
| --------------------- | ----------- |
| spring.boot.admin.url | URL of the spring-boot-admin application to register at.<br>_Mandatory_. | |
| spring.boot.admin.contextPath | Context-path of registration point.<br>Default: api/applications |
| spring.boot.admin.username | Username for Http-Basic authentication. Default: empty |
| spring.boot.admin.password | Password for Http-Basic authentication. Default: empty |
| spring.boot.admin.period | Time period for registration repeat.<br>Default: 10000 |
| spring.boot.admin.autoDeregistration | Swtich to enable auto-deregistration at admin when context is closed<br>Default: false |
| spring.boot.admin.client.serviceUrl | Client-management-URL to register with. Can be overriden in case the reachable URL is different (e.g. Docker). Must be unique in registry.<br>Default: is guessed based on hostname, server.port and server.context-path |
| spring.boot.admin.client.managemntUrl | Client-management-URL to register with. Can be overriden in case the reachable URL is different (e.g. Docker). Must be unique in registry.<br>Default: is guessed based on serviceUrl management.port and management.context-path|
| spring.boot.admin.client.healthUrl | Client-management-URL to register with. Can be overriden in case the reachable URL is different (e.g. Docker). Must be unique in registry.<br>Default: is guessed based on managementUrl and endpoints.health.id |
| spring.boot.admin.client.name | Name to register with. Defaults to the ApplicationContexts name. Only set when it should differ.<br>Default: _${spring.application.name}_ if set, spring-boot-application otherwise. |
| spring.boot.admin.client.useIpAddressOf | If an network-interface name is specified, its ip-address wil be used for the guessed url (instead of hostname).|

### Other configuration properties
Options from other spring boot features. These should be set to enable all features.

| Name                    | Description |
| ----------------------- | ----------- |
| spring.application.name | Name to be shown in the application list. Name of the ApplicationContext. |
| info.version            | Version number to be shown in the application list. Also published via /info-endpoint.  |
| logging.file            | Path to the applications logfile for access via spring-boot-admin. From Spring Boot logging configuration. |
