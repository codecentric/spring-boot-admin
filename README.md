codecentric's Spring Boot Admin
===============================
[![Apache License 2](https://img.shields.io/badge/license-ASF2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)
[![Build Status](https://travis-ci.org/codecentric/spring-boot-admin.svg?branch=master)](https://travis-ci.org/codecentric/spring-boot-admin)
[![Coverage Status](https://coveralls.io/repos/github/codecentric/spring-boot-admin/badge.svg?branch=master)](https://coveralls.io/github/codecentric/spring-boot-admin?branch=master)
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

[A quick guide](http://codecentric.github.io/spring-boot-admin/2.3.0/#getting-started) to get started can be found in our docs.

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

## Getting Help

Having trouble with codecentric's Spring Boot Admin? We’d like to help!

 * Check the [reference documentation](http://codecentric.github.io/spring-boot-admin/current/).

 * Ask a question on [stackoverflow.com](http://stackoverflow.com/questions/tagged/spring-boot-admin) - we monitor questions tagged with `spring-boot-admin`.

 * Ask for help in our [spring-boot-admin Gitter chat](https://gitter.im/codecentric/spring-boot-admin)

 * Report bugs at http://github.com/codecentric/spring-boot-admin/issues.

## Reference Guide
[Version 2.1.6](http://codecentric.github.io/spring-boot-admin/2.1.6/)

[Version 2.0.6](http://codecentric.github.io/spring-boot-admin/2.0.6/)

[Version 1.5.7](http://codecentric.github.io/spring-boot-admin/1.5.7/)

## Trademarks and licenses
The source code of codecentric's Spring Boot Admin is licensed under [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

Spring, Spring Boot and Spring Cloud are trademarks of [Pivotal Software, Inc.](https://pivotal.io/) in the U.S. and other countries.

## Snapshot builds
You can access snapshot builds from the sonatype snapshot repository by adding the following to your `repositories`:
```xml
<repository>
	<id>sonatype-nexus-snapshots</id>
	<name>Sonatype Nexus Snapshots</name>
	<url>https://oss.sonatype.org/content/repositories/snapshots/</url>
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
* [Node.js v8.x (LTS)](https://nodejs.org/en/download/)
* JDK >= 1.8

Please make sure you set `$JAVA_HOME` points to the correct JDK and that the `node` and `npm` commands are on your `$PATH`.

```shell
./mvnw clean package
```

## Contributing
See [CONTRIBUTING.md](CONTRIBUTING.md) file.
