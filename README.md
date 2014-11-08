spring-boot-admin
=================
[![Build Status](https://travis-ci.org/codecentric/spring-boot-admin.png?branch=master)](https://travis-ci.org/codecentric/spring-boot-admin)


This is a simple admin interface for [Spring Boot](http://projects.spring.io/spring-boot/ "Official Spring-Boot website") applications.

This application provides a simple GUI to administrate Spring Boot applications in some ways. At the moment it provides the following features for every registered application.

<ul>
<li>Show name/id and version number</li>
<li>Show online status</li>
<li>Download main logfile</li>
<li>Show details, like</li>
<ul>
<li>Java system properties</li>
<li>Java environment properties</li>
<li>Memory metrics</li>
<li>Spring environment properties</li>
</ul>
</ul> 

#### Server application

Add the following dependency to your pom.xml.

```
<dependency>
	<groupId>de.codecentric</groupId>
	<artifactId>spring-boot-admin-server</artifactId>
	<version>1.0.5</version>
</dependency>
```

Create the Spring Boot Admin Server with only one single Annotation.

```
@Configuration
@EnableAutoConfiguration
@EnableAdminServer
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
```

See also the [example project](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-example) in this repository.

#### Client applications

Each application that want to register itself to the admin application has to include the [spring-boot-starter-admin-client](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-starter-admin-client) as dependency. This starter JAR includes some [AutoConfiguration](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#using-boot-auto-configuration "Spring Boot docu") features that includes registering tasks, controller, etc.

```
<dependency>
	<groupId>de.codecentric</groupId>
	<artifactId>spring-boot-starter-admin-client</artifactId>
	<version>1.0.5</version>
</dependency>
```

The following need to be available to the Spring Environment (via application.properties, command-line argument, etc):

- <b>spring.boot.admin.url</b> - URL of the Spring Boot Admin Server
- <b>spring.application.name</b> - the unique identifier for this client application (cannot be used by another client)
- <b>info.id</b> - an ID that identifies this application (shared by all instances of the same application; <u>recommended</u>: <i>@pom.artifactId@</i>)
- <b>info.version</b> - the version of this application (<u>recommended</u>: <i>@pom.version@</i>)

E.g., in application.properties
```
spring.boot.admin.url=http://localhost:8080
spring.application.name=spring-boot-admin-example
info.id=@pom.artifactId@
info.version=@pom.version@
```

#### Screenshots

##### Dashboard

[](url "title") 
<img src="https://raw.githubusercontent.com/codecentric/spring-boot-admin/master/screenshot.jpg">

##### Metrics

[](url "title") 
<img src="https://raw.githubusercontent.com/codecentric/spring-boot-admin/master/screenshot-metrics.png">

#### Build

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
mvn build-helper:parse-version versions:set -DnewVersion=1.0.0-SNAPSHOT
mvn versions:commit
```


