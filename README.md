codecentric's Spring Boot Admin
===============================
[![Apache License 2](https://img.shields.io/badge/license-ASF2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)
![Build Status](https://github.com/codecentric/spring-boot-admin/actions/workflows/build-main.yml/badge.svg?branch=master)
[![codecov](https://codecov.io/gh/codecentric/spring-boot-admin/branch/master/graph/badge.svg?token=u5SWsZpj5S)](https://codecov.io/gh/codecentric/spring-boot-admin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.codecentric/spring-boot-admin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.codecentric/spring-boot-admin/)
[![Gitter](https://badges.gitter.im/codecentric/spring-boot-admin.svg)](https://gitter.im/codecentric/spring-boot-admin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

![](./images/logo-spring-boot-admin.png)

This community project provides an admin interface for [Spring Boot <sup>®</sup>](http://projects.spring.io/spring-boot/ "Official Spring-Boot website") applications.

Monitoring Python applications is available using [Pyctuator](https://github.com/SolarEdgeTech/pyctuator).

Spring Boot Admin provides the following features for registered applications:

* Show health status
* Show details, like
  * JVM & memory metrics
  * micrometer.io metrics
  * Datasource metrics
  * Cache metrics
* Show build-info number
* Follow and download logfile
* View jvm system- & environment-properties
* View Spring Boot Configuration Properties
* Support for Spring Cloud's postable /env- &/refresh-endpoint
* Easy loglevel management
* Interact with JMX-beans
* View thread dump
* View http-traces
* View auditevents
* View http-endpoints
* View scheduled tasks
* View and delete active sessions (using spring-session)
* View Flyway / Liquibase database migrations
* Download heapdump
* Notification on status change (via e-mail, Slack, Hipchat, ...)
* Event journal of status changes (non persistent)

## Getting Started

[A quick guide](http://codecentric.github.io/spring-boot-admin/2.5.1/#getting-started) to get started can be found in our docs.

There are introductory talks available on YouTube:

<a href="https://youtu.be/Ql1Gnz4L_-c" target="_blank"><img src="https://i.ytimg.com/vi/Ql1Gnz4L_-c/maxresdefault.jpg"
alt="Cloud Native Spring Boot® Admin by Johannes Edmeier @ Spring I/O 2019" width="240" height="135" border="10" /></a><br>
**Cloud Native Spring Boot® Admin by Johannes Edmeier @ Spring I/O 2019**

<a href="https://youtu.be/__zkypwjSMs" target="_blank"><img src="https://i.ytimg.com/vi/__zkypwjSMs/maxresdefault.jpg"
alt="Monitoring Spring Boot® Applications with Spring Boot Admin @ Spring I/O 2018" width="240" height="135" border="10" /></a><br>
**Monitoring Spring Boot® Applications with Spring Boot Admin @ Spring I/O 2018**

<a href="https://goo.gl/2tRiUi" target="_blank"><img src="https://i.ytimg.com/vi/PWd9Q8_4OFo/maxresdefault.jpg"
alt="Spring Boot® Admin - Monitoring and Configuring Spring Boot Applications at Runtime" width="240" height="135" border="10" /></a><br>
**Spring Boot® Admin - Monitoring and Configuring Spring Boot Applications at Runtime**

## Compatibility Matrix
Spring Boot Admin's version matches the major and minor versions of Spring Boot:
            
 * SBA 2.0.X -> Spring Boot 2.0.Y
 * SBA 2.1.X -> Spring Boot 2.1.Y
 * SBA 2.2.X -> Spring Boot 2.2.Y

etc. pp.

Nevertheless, it is possible to monitor any version of a Spring Boot service independently of the underlying Spring Boot version in the service. 
Hence, it is possible to run Spring Boot Admin Server version 2.6 and monitor a service that is running on Spring Boot 2.3 using Spring Boot Admin Client version 2.3.

## Getting Help

Having trouble with codecentric's Spring Boot Admin? We’d like to help!

 * Check the [reference documentation](http://codecentric.github.io/spring-boot-admin/current/).

 * Ask a question on [stackoverflow.com](http://stackoverflow.com/questions/tagged/spring-boot-admin) - we monitor questions tagged with `spring-boot-admin`.

 * Ask for help in our [spring-boot-admin Gitter chat](https://gitter.im/codecentric/spring-boot-admin)

 * Report bugs at http://github.com/codecentric/spring-boot-admin/issues.

## Reference Guide
[Version 2.6.6](http://codecentric.github.io/spring-boot-admin/2.6.6/)

[Version 2.5.6](http://codecentric.github.io/spring-boot-admin/2.5.6/)

[Version 1.5.7](http://codecentric.github.io/spring-boot-admin/1.5.7/)

**Translated version**
The following reference guides have been translated by users of Spring Boot Admin and are not part of the official bundle.
The maintainers of Spring Boot Admin will not update and maintain the guides mentioned below.

[Version 2.6.6 (Chinese translated by @qq253498229)](https://consolelog.gitee.io/docs-spring-boot-admin-docs-chinese/)

## Trademarks and licenses
The source code of codecentric's Spring Boot Admin is licensed under [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

Spring, Spring Boot and Spring Cloud are trademarks of [Pivotal Software, Inc.](https://pivotal.io/) in the U.S. and other countries.

## Snapshot builds
You can access snapshot builds from the sonatype snapshot repository by adding the following to your `repositories`:
```xml
<repository>
	<id>sba-snapshot</id>
	<name>Spring Boot Admin Snapshots</name>
	<url>https://maven.pkg.github.com/codecentric/spring-boot-admin</url>
	<snapshots>
		<enabled>true</enabled>
	</snapshots>
	<releases>
		<enabled>false</enabled>
	</releases>
</repository>
```

## Screenshots

![Screenshot application list](/images/screenshot.png)
*Dashboard with desktop notifications*

![Screenshot details](/images/screenshot-details.png)
*View application health, info and details*

![Screenshot metrics](/images/screenshot-metrics.png)
*View metric counters and gauges*

![Screenshot logfile](/images/screenshot-logfile.png)
*View logfile (with follow)*

![Screenshot environment](/images/screenshot-environment.png)
*View and change Spring environment (via Spring Cloud)*

![Screenshot logging](/images/screenshot-logging.png)
*Manage Logback logger levels*

![Screenshot jmx](/images/screenshot-jmx.png)
*View and use JMX beans via jolokia*

![Screenshot threads](/images/screenshot-threads.png)
*View thread dump*

![Screenshot traces](/images/screenshot-trace.png)
*View http request traces*

![Screenshot journal](/images/screenshot-journal.png)
*View history of registered applications*

## Build
**Requirements:**

* JDK >= 1.8

Please make sure you set `$JAVA_HOME` points to the correct JDK.

```shell
./mvnw clean package
```

## Contributing
See [CONTRIBUTING.md](CONTRIBUTING.md) file.
