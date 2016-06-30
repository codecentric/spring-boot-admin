spring-boot-admin
=================
[![Apache License 2](https://img.shields.io/badge/license-ASF2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)
[![Build Status](https://travis-ci.org/codecentric/spring-boot-admin.svg?branch=master)](https://travis-ci.org/codecentric/spring-boot-admin)
[![Coverage Status](https://coveralls.io/repos/codecentric/spring-boot-admin/badge.svg)](https://coveralls.io/r/codecentric/spring-boot-admin)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/8fd7bac6edac417a8451387286fe6917)](https://www.codacy.com/app/joshiste/spring-boot-admin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.codecentric/spring-boot-admin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.codecentric/spring-boot-admin/)
[![Gitter](https://badges.gitter.im/Join Chat.svg)](https://gitter.im/codecentric/spring-boot-admin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

This is a simple admin interface for [Spring Boot](http://projects.spring.io/spring-boot/ "Official Spring-Boot website") applications.

This application provides a simple GUI to administrate Spring Boot applications in some ways. At the moment it provides the following features for every registered application.

* Show name/id and version number
* Show health status
* Download main logfile
* Show details, like
 * JVM & memory metrics
 * Counter & gauge metrics
 * Datasource metrics
 * Cache metrics
* View Java, system- & environment-properties
* Support for Spring Clouds postable /env- &/refresh-endpoint
* Easy loglevel management (for Logback only)
* Interact with JMX-beans
* View threaddump
* View traces
* Mail and desktop notification on status change
* Event journal of status changes (non persistent)

## Getting Started

[A quick guide](http://codecentric.github.io/spring-boot-admin/1.3.3/#getting-started) to get started can be found in our docs.

## Reference Guide

[Version 1.3.4](http://codecentric.github.io/spring-boot-admin/1.3.4/)

[Version 1.3.3](http://codecentric.github.io/spring-boot-admin/1.3.3/)

[Version 1.3.2](http://codecentric.github.io/spring-boot-admin/1.3.2/)

[Version 1.3.0](http://codecentric.github.io/spring-boot-admin/1.3.0/)

## Screenshots

### Dashboard

[](url "title")
<img src="https://raw.githubusercontent.com/codecentric/spring-boot-admin/master/screenshot.png">

### Details

[](url "title")
<img src="https://raw.githubusercontent.com/codecentric/spring-boot-admin/master/screenshot-details.png">

### Environment

[](url "title")
<img src="https://raw.githubusercontent.com/codecentric/spring-boot-admin/master/screenshot-environment.png">

### Logging

[](url "title")
<img src="https://raw.githubusercontent.com/codecentric/spring-boot-admin/master/screenshot-logging.png">

### JMX

[](url "title")
<img src="https://raw.githubusercontent.com/codecentric/spring-boot-admin/master/screenshot-jmx.png">

### Threads

[](url "title")
<img src="https://raw.githubusercontent.com/codecentric/spring-boot-admin/master/screenshot-threads.png">

### Trace

[](url "title")
<img src="https://raw.githubusercontent.com/codecentric/spring-boot-admin/master/screenshot-trace.png">

### Journal

[](url "title")
<img src="https://raw.githubusercontent.com/codecentric/spring-boot-admin/master/screenshot-journal.png">

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
