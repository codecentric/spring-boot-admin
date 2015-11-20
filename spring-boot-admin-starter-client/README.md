spring-boot-starter-admin-client
================================

This [Spring-Boot starter](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#using-boot-starter-poms "Spring Boot Reference Guide") provides services and controllers that are required if an application should be able to be administrated with the [spring-boot-admin application](https://github.com/codecentric/spring-boot-admin "GitHub project").

This client uses the [AutoConfiguration](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#using-boot-auto-configuration "Spring Boot Reference Guide") feature of Spring Boot to register service and controller beans in the application context.

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
| spring.boot.admin.client.managementUrl | Client-management-URL to register with. Can be overriden in case the reachable URL is different (e.g. Docker). Must be unique in registry.<br>Default: is guessed based on serviceUrl management.port and management.context-path|
| spring.boot.admin.client.healthUrl | Client-management-URL to register with. Can be overriden in case the reachable URL is different (e.g. Docker). Must be unique in registry.<br>Default: is guessed based on managementUrl and endpoints.health.id |
| spring.boot.admin.client.name | Name to register with. Defaults to the ApplicationContexts name. Only set when it should differ.<br>Default: _${spring.application.name}_ if set, spring-boot-application otherwise. |
| spring.boot.admin.client.preferIp | Use the ip-address rather then the hostname in the guessed urls. It's required to set `server.address` and `management.address`respectively. |

### Other configuration properties
Options from other spring boot features. These should be set to enable all features.

| Name                    | Description |
| ----------------------- | ----------- |
| spring.application.name | Name to be shown in the application list. Name of the ApplicationContext. |
| info.version            | Version number to be shown in the application list. Also published via /info-endpoint.  |

### Logging Support ([Logback](http://logback.qos.ch/))
The spring-boot-admin-server-ui allows users to manipulate logger levels dynamically. To support this, a `JMXConfigurator` must be made available by the spring boot client app. This is conveniently done by the following configuration in your logback configuration file(under the root `<configuration>` element).
```
<jmxConfigurator/>
```
Note that, the UI will intelligently try to determine the JMXConfigurator by first attempting to locate one with the application name (spring.application.name) and then falliing back to a default configuration. This is useful if you have the need  to deploy multiple spring boot applications to the same JVM. In such a case, specify a context name in your logback configuration as follows:
```
<contextName>your_spring_boot_application_name</contextName>
```
