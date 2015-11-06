spring-boot-admin
=================
[![Apache License 2](https://img.shields.io/badge/license-ASF2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)
[![Build Status](https://travis-ci.org/codecentric/spring-boot-admin.svg?branch=master)](https://travis-ci.org/codecentric/spring-boot-admin)
[![Coverage Status](https://coveralls.io/repos/codecentric/spring-boot-admin/badge.svg)](https://coveralls.io/r/codecentric/spring-boot-admin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.codecentric/spring-boot-admin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.codecentric/spring-boot-admin/)
[![Gitter](https://badges.gitter.im/Join Chat.svg)](https://gitter.im/codecentric/spring-boot-admin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

This is a simple admin interface for [Spring Boot](http://projects.spring.io/spring-boot/ "Official Spring-Boot website") applications.

This application provides a simple GUI to administrate Spring Boot applications in some ways. At the moment it provides the following features for every registered application.

* Show name/id and version number
* Show health status
* Download main logfile
* Show details, like
 * JVM & memory metrics
 * Counter & gauge Metrics
 * Datasource Metrics
* View Java, system- & environment-properties
* Support for Spring Clouds postable /env- &/refresh-endpoint
* Easy loggerlevel management (for Logback)
* Interact with JMX-beans
* View threaddump
* View traces
* Mail and desktop notification on status change
* Event journal of status changes (non persistent)

#### Server application
Add the following dependency to your pom.xml.
```xml
<dependency>
	<groupId>de.codecentric</groupId>
	<artifactId>spring-boot-admin-server</artifactId>
	<version>1.2.4</version>
</dependency>
<dependency>
	<groupId>de.codecentric</groupId>
	<artifactId>spring-boot-admin-server-ui</artifactId>
	<version>1.2.4</version>
</dependency>
```

Create the Spring Boot Admin server with only one single annotation.
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

See also the [example project](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample) in this repository.

For configuring hazelcast support see [spring-boot-admin-server](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-server/README.md) or [hazelcast-example project](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-hazelcast)

#### Register / discover client applications
To get all your boot applications shown in spring boot admin you have two choices:

1) either you include the spring-boot-admin-starter-client into your applications

or

2) you add a DiscoveryClient (e.g. Eureka) to your spring boot admin server.

**Note:** If you don't include the spring-boot-admin-starter-client the logfile won't be available with spring boot 1.2.x, due to the fact that the logfile endpoint won't be exposed.

##### Register client applications via spring-boot-admin-starter-client
Each application that want to register itself to the admin application has to include the [spring-boot-admin-starter-client](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-starter-client) as dependency. This starter JAR includes some [AutoConfiguration](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#using-boot-auto-configuration "Spring Boot documentation") features that includes registering tasks, controller, etc.
```xml
<dependency>
	<groupId>de.codecentric</groupId>
	<artifactId>spring-boot-admin-starter-client</artifactId>
	<version>1.2.4</version>
</dependency>
```
Inside your configuration (e.g. application.properties) you also have to define the URL of the Spring Boot Admin server, e.g.
```
spring.boot.admin.url=http://localhost:8080
```
For all configuration options see [spring-boot-admin-starter-client](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-starter-client/README.md)

##### Discover client applications via DiscoveryClient
Just add spring-clouds ``@EnableDiscoveryClient`` annotation and include an appropriate implementation (e.g. Eureka) to your classpath.
```java
@Configuration
@EnableAutoConfiguration
@EnableDiscoveryClient
@EnableAdminServer
public class SpringBootAdminApplication {
	public static void main(String[] args) {
		SpringApplication.run(SpringBootAdminApplication.class, args);
	}
}
```

```xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-eureka</artifactId>
	<version>1.0.3.RELEASE</version>
</dependency>
```
See the sample [discovery sample project](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-discovery)

#### Mail notification on status change

Configure a JavaMailSender using spring-boot-starter-mail and set a recipient:
```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

```
spring.mail.host=smtp.example.com
spring.boot.admin.notify.to=admin@example.com
```

For all configuration options see [spring-boot-admin-server](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-server/README.md)

### Snapshot builds
You can access snapshot builds from the sonatype repository: 
```xml
<snapshotRepository>
 <id>sonatype-nexus-snapshots</id>
 <name>Sonatype Nexus Snapshots</name>
 <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
</snapshotRepository>
```

#### Screenshots

##### Dashboard

[](url "title")
<img src="https://raw.githubusercontent.com/codecentric/spring-boot-admin/master/screenshot.png">

##### Details

[](url "title")
<img src="https://raw.githubusercontent.com/codecentric/spring-boot-admin/master/screenshot-details.png">

##### Environment

[](url "title")
<img src="https://raw.githubusercontent.com/codecentric/spring-boot-admin/master/screenshot-environment.png">

##### Logging

[](url "title")
<img src="https://raw.githubusercontent.com/codecentric/spring-boot-admin/master/screenshot-logging.png">

##### JMX

[](url "title")
<img src="https://raw.githubusercontent.com/codecentric/spring-boot-admin/master/screenshot-jmx.png">

##### Threads

[](url "title")
<img src="https://raw.githubusercontent.com/codecentric/spring-boot-admin/master/screenshot-threads.png">

##### Trace

[](url "title")
<img src="https://raw.githubusercontent.com/codecentric/spring-boot-admin/master/screenshot-trace.png">

##### Journal

[](url "title")
<img src="https://raw.githubusercontent.com/codecentric/spring-boot-admin/master/screenshot-journal.png">


#### Build
In order to build spring-boot-admin you need to have node.js and npm on your PATH.

```shell
mvn clean package
```

#### Release

```shell
mvn build-helper:parse-version versions:set -DnewVersion=${parsedVersion.majorVersion}.${parsedVersion.minorVersion}.${parsedVersion.incrementalVersion}
mvn -Psign-artifacts clean deploy
```

#### Increment version for next release

Example:

```shell
mvn build-helper:parse-version versions:set versions:commit -DnewVersion=1.0.0-SNAPSHOT
```
