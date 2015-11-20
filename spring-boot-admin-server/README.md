spring-boot-admin-server
========================

## Easy Setup
Add the following dependency to your pom.xml.

```xml
<dependency>
	<groupId>de.codecentric</groupId>
	<artifactId>spring-boot-admin-server</artifactId>
	<version>1.3.0</version>
</dependency>
<dependency>
	<groupId>de.codecentric</groupId>
	<artifactId>spring-boot-admin-server-ui</artifactId>
	<version>1.3.0</version>
</dependency>
```

Create the Spring Boot Admin Server with only one single Annotation.
Example in spring-admin-samples/spring-boot-admin-sample.
```java
@Configuration
@EnableAutoConfiguration
@EnableAdminServer
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
```

## Spring Cloud DiscoveryClient support
The Spring Boot Admin Server is capable of using  Spring Clouds DiscoveryClient to discover applications. When you do this the clients don't have to include the spring-boot-admin-starter-client. You just have to configure a DiscoveryClient - everything else is done by AutoConfiguration.
See the [discovery sample project](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-discovery) in this repository.

One note: If you omit the Spring Boot Admin Client in you Client Applications you can't download the logfile (but hopefully my pull request will make it into Spring Boot 1.3.0);

### Further configuration
Since the DiscoveryClient doesn't tell the management.context-path you can suffix the url for all discovered clients by setting ``spring.boot.admin.discovery.management.context-path``.

Explictly disable DiscoveryClient support by setting ``spring.boot.admin.discover.enabled=false``.

## Mail notification:
To enable mail-notification you just have to add a JavaMailSender to you ApplicationContext. The simplest way to achieve this is by adding the spring-boot-mail-starter to your dependencies and setting `spring.mail.host`.

### Further options:
| Name                  | Description |
| --------------------- | ----------- |
|spring.boot.admin.notify.mail.enabled|enable mail notification (default: true)|
|spring.boot.admin.notify.mail.to|comma-delimited list of mail recipients (default: "root@localhost")|
|spring.boot.admin.notify.mail.cc|comma-delimited list of mail cc-recipients|
|spring.boot.admin.notify.mail.from|sender of mail|
|spring.boot.admin.notify.mail.subject|mail-subject; SpEL-expressions supported (default: "#{application.name} (#{application.id}) is #{to.status}") |
|spring.boot.admin.notify.mail.text|mail-body; SpEL-expressions supported (default: "#{application.name} (#{application.id})\nstatus changed from #{from.status} to #{to.status}\n\n#{application.healthUrl}"|
|spring.boot.admin.notify.mail.ignoreChanges|comma-delimited list of status changes to be ignored. (default: "UNKNOWN:UP")|

## Pagerduty notification:
To enable pagerduty-notifications you just have to add a generic service for your pagerduty-account and set ``spring.boot.admin.notify.pagerduty.service-keya`` to the service-key.

### Further options:
| Name                   | Description |
| ---------------------- | ----------- |
|spring.boot.admin.notify.pagerduty.enabled|enable mail notification (default: true)|
|spring.boot.admin.notify.pagerduty.service-key | service-key for Pagerduty |
|spring.boot.admin.notify.pagerduty.uri | The Pagerduty-rest-api url (default: https://events.pagerduty.com/generic/2010-04-15/create_event.json) |
|spring.boot.admin.notify.pagerduty.description | description to use in the event. SpEL-expressions supported (default: #{application.name}/#{application.id} is #{to.status}) |
|spring.boot.admin.notify.pagerduty.client | client-name to use in the event |
|spring.boot.admin.notify.pagerduty.clientUrl | client-url to use in the event |
|spring.boot.admin.notify.pagerduty.ignoreChanges|comma-delimited list of status changes to be ignored. (default: "UNKNOWN:UP")|

## Hazelcast Support
Spring Boot Admin Server supports cluster replication with Hazelcast. It is automatically enabled when a HazelcastConfig- or HazelcastInstance is present.
Also have a look at the [Spring Boot support for Hazelcast](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#boot-features-hazelcast).

1. Add Hazelcast to your dependencies:
```xml
<dependency>
	<groupId>com.hazelcast</groupId>
	<artifactId>hazelcast</artifactId>
</dependency>
```
2. Instantiate a HazelcastConfig:
```java
@Configuration
@EnableAutoConfiguration
@EnableAdminServer
public class SpringBootAdminApplication {
	@Bean
	public Config hazelcastConfig() {
		return new Config().setProperty("hazelcast.jmx", "true")
				.addMapConfig(new MapConfig("spring-boot-admin-application-store").setBackupCount(1)
						.setEvictionPolicy(EvictionPolicy.NONE))
				.addListConfig(new ListConfig("spring-boot-admin-event-store").setBackupCount(1)
						.setMaxSize(1000));
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringBootAdminApplication.class, args);
	}
}
```


### Further configuration
Disable Hazelcast support by setting ``spring.boot.admin.hazelcast.enabled=false``.

To alter the name of the hazelcast-map backing the application-store set ``spring.boot.admin.hazelcast.map= my-own-map-name``.
To alter the name of the hazelcast-list backing the event-store set ``spring.boot.admin.hazelcast.event-store=my-own-list-name``
