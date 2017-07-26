spring-boot-admin
=================
[![Apache License 2](https://img.shields.io/badge/license-ASF2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)
[![Build Status](https://travis-ci.org/codecentric/spring-boot-admin.svg?branch=master)](https://travis-ci.org/codecentric/spring-boot-admin)
[![Coverage Status](https://coveralls.io/repos/codecentric/spring-boot-admin/badge.svg)](https://coveralls.io/r/codecentric/spring-boot-admin)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/8fd7bac6edac417a8451387286fe6917)](https://www.codacy.com/app/joshiste/spring-boot-admin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.codecentric/spring-boot-admin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.codecentric/spring-boot-admin/)
[![Gitter](https://badges.gitter.im/codecentric/spring-boot-admin.svg)](https://gitter.im/codecentric/spring-boot-admin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

This is a simple admin interface for [Spring Boot](http://projects.spring.io/spring-boot/ "Official Spring-Boot website") applications.

This application provides a simple UI to administrate Spring Boot applications. It provides the following features for registered application.

* Show health status
* Show details, like
 * JVM & memory metrics
 * Counter & gauge metrics
 * Datasource metrics
 * Cache metrics
* Show build-info number
* Follow and download logfile
* View jvm system- & environment-properties
* Support for Spring Cloud's postable /env- &/refresh-endpoint
* Easy loglevel management (currently for Logback only)
* Interact with JMX-beans
* View thread dump
* View traces
* Hystrix-Dashboard integration
* Download heapdump
* Notification on status change (via mail, Slack, Hipchat, ...)
* Event journal of status changes (non persistent)

## Getting Started

[A quick guide](http://codecentric.github.io/spring-boot-admin/1.5.2/#getting-started) to get started can be found in our docs.

## Getting Help

Having trouble with Spring Boot Admin? We’d like to help!

 * Check the [reference documentation](http://codecentric.github.io/spring-boot-admin/1.5.2/).
 
 * Ask a question on [stackoverflow.com](http://stackoverflow.com/questions/tagged/spring-boot-admin) - we monitor questions tagged with `spring-boot-admin`.
 
 * Ask for help in our [spring-boot-admin Gitter chat](https://gitter.im/codecentric/spring-boot-admin)
 
 * Report bugs with Spring Boot Admin at http://github.com/codecentric/spring-boot-admin/issues.

## Reference Guide
[Version 1.5.2](http://codecentric.github.io/spring-boot-admin/1.5.2/)

[Version 1.4.6](http://codecentric.github.io/spring-boot-admin/1.4.6/)

[Version 1.3.7](http://codecentric.github.io/spring-boot-admin/1.3.7/)

## Screenshots

![Screenshot application list](/images/screenshot.png?raw=true)
*Dashboard with desktop notifications*

![Screenshot details](/images/screenshot-details.png?raw=true)
*View application health, info and details*

![Screenshot metrics](/images/screenshot-metrics.png?raw=true)
*View metric counters and gauges*

![Screenshot logfile](/images/screenshot-logfile.png?raw=true)
*View logfile (with follow)*

![Screenshot environment](/images/screenshot-environment.png?raw=true)
*View and change Spring environment (via Spring Cloud)*

![Screenshot logging](/images/screenshot-logging.png?raw=true)
*Manage Logback logger levels*

![Screenshot jmx](/images/screenshot-jmx.png?raw=true)
*View and use JMX beans via jolokia*

![Screenshot threads](/images/screenshot-threads.png?raw=true)
*View thread dump*

![Screenshot traces](/images/screenshot-trace.png?raw=true)
*View http request traces*

![Screenshot hystrix](/images/screenshot-hystrix.png?raw=true)
*View Hystrix dashboard*

![Screenshot journal](/images/screenshot-journal.png?raw=true)
*View history of registered applications*

## Snapshot builds
You can access snapshot builds from the sonatype repository:
```xml
<snapshotRepository>
 <id>sonatype-nexus-snapshots</id>
 <name>Sonatype Nexus Snapshots</name>
 <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
</snapshotRepository>
```

## Build
In order to build spring-boot-admin you need to have node.js and npm on your `PATH`.

```shell
mvn clean package
```

## Set version for next release
```shell
mvn versions:set versions:commit -DnewVersion=1.0.0-SNAPSHOT
```
