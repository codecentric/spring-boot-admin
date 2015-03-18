spring-boot-admin-server
================================

## Easy Setup
Add the following dependency to your pom.xml.

```xml
<dependency>
	<groupId>de.codecentric</groupId>
	<artifactId>spring-boot-admin-server</artifactId>
	<version>1.1.2</version>
</dependency>
<dependency>
	<groupId>de.codecentric</groupId>
	<artifactId>spring-boot-admin-server-ui</artifactId>
	<version>1.1.2</version>
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

## Hazelcast Support
Spring Boot Admin Server supports cluster replication with Hazelcast.
It is automatically enabled when its found on the classpath.

Just add Hazelcast to your dependencies:
```xml
<dependency>
	<groupId>com.hazelcast</groupId>
	<artifactId>hazelcast</artifactId>
	<version>3.3.3</version>
</dependency>
```

And thats it! The server is going to use the default Hazelcast configuration.

### Custom Hazelcast configuration
To change the configuration add a ``com.hazelcast.config.Config``-bean to your application context (for example with hazelcast-spring):

Add hazelcast-spring to dependencies:
```xml
<dependency>
	<groupId>com.hazelcast</groupId>
	<artifactId>hazelcast-spring</artifactId>
	<version>3.3.3</version>
</dependency>
```

Import hazelcast spring configuration xml-file:
```java
@Configuration
@EnableAutoConfiguration
@EnableAdminServer
@ImportResource({ "classpath:hazelcast-config.xml" })
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
```

Write xml-config hazelcast-config.xml:
```xml
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:hz="http://www.hazelcast.com/schema/spring" 
	xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.0.xsd http://www.hazelcast.com/schema/spring http://www.hazelcast.com/schema/spring/hazelcast-spring-3.3.xsd">
	<hz:config id="hazelcastConfig">
		<hz:instance-name>${hz.instance.name}</hz:instance-name>
		<hz:map name="spring-boot-admin-application-store" backup-count="1" eviction-policy="NONE" />
	</hz:config>
</beans>
```

### Further configuration
Disable Hazelcast support by setting ``spring.boot.admin.hazelcast.enabled=false``.

To alter the name of the Hazelcast-Map set ``spring.boot.admin.hazelcast.map= my-own-map-name``.
