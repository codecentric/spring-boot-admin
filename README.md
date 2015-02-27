spring-boot-admin
=================
[![Build Status](https://travis-ci.org/codecentric/spring-boot-admin.svg)](https://travis-ci.org/codecentric/spring-boot-admin)
[![Coverage Status](https://coveralls.io/repos/codecentric/spring-boot-admin/badge.svg)](https://coveralls.io/r/codecentric/spring-boot-admin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.codecentric/spring-boot-admin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.codecentric/spring-boot-admin/)
[![Gitter](https://badges.gitter.im/Join Chat.svg)](https://gitter.im/codecentric/spring-boot-admin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

This is a simple admin interface for [Spring Boot](http://projects.spring.io/spring-boot/ "Official Spring-Boot website") applications.

This application provides a simple GUI to administrate Spring Boot applications in some ways. At the moment it provides the following features for every registered application.

* Show name/id and version number
* Show health status
* Download main logfile
* Show details, like
 * Java System- / Environment- / Spring properties
 * JVM & memory metrics
 * Counter & gauge Metrics
 * Datasource Metrics
* Easy loggerlevel management
* Interact with JMX-Beans
* View Threaddump

#### Server application
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

For configuring Hazelcast support see [spring-boot-admin-server](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-server/README.md) or [hazelcast-example project](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-hazelcast)

#### Client applications
Each application that want to register itself to the admin application has to include the [spring-boot-starter-admin-client](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-starter-admin-client) as dependency. This starter JAR includes some [AutoConfiguration](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#using-boot-auto-configuration "Spring Boot documentation") features that includes registering tasks, controller, etc.
```xml
<dependency>
	<groupId>de.codecentric</groupId>
	<artifactId>spring-boot-starter-admin-client</artifactId>
	<version>1.1.2</version>
</dependency>
```

Inside your configuration (e.g. application.properties) you also have to define the URL of the Spring Boot Admin Server, e.g.
```
spring.boot.admin.url=http://localhost:8080
```

For all configuration options see [spring-boot-starter-admin-client](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-starter-admin-client/README.md)


#### Screenshots

##### Dashboard

[](url "title")
<img src="https://raw.githubusercontent.com/codecentric/spring-boot-admin/master/screenshot.png">

##### Details

[](url "title")
<img src="https://raw.githubusercontent.com/codecentric/spring-boot-admin/master/screenshot-details.png">

##### Logging

[](url "title")
<img src="https://raw.githubusercontent.com/codecentric/spring-boot-admin/master/screenshot-logging.png">

##### JMX

[](url "title")
<img src="https://raw.githubusercontent.com/codecentric/spring-boot-admin/master/screenshot-jmx.png">

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
