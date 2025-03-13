# Spring Boot Admin by [codecentric](https://codecentric.de)
[![Apache License 2](https://img.shields.io/badge/license-ASF2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)
![Build Status](https://github.com/codecentric/spring-boot-admin/actions/workflows/build-main.yml/badge.svg?branch=master)
[![codecov](https://codecov.io/gh/codecentric/spring-boot-admin/branch/master/graph/badge.svg?token=u5SWsZpj5S)](https://codecov.io/gh/codecentric/spring-boot-admin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.codecentric/spring-boot-admin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.codecentric/spring-boot-admin/)
[![Gitter](https://badges.gitter.im/codecentric/spring-boot-admin.svg)](https://gitter.im/codecentric/spring-boot-admin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

This community project provides an admin interface for [Spring Boot <sup>®</sup>](http://projects.spring.io/spring-boot/ "Official Spring-Boot website") web applications that expose actuator endpoints.

Monitoring Python applications is available using [Pyctuator](https://github.com/SolarEdgeTech/pyctuator).

## Compatibility Matrix
In the Spring Boot Admin Server App, the Spring Boot Admin's version matches the major and minor versions of Spring Boot.

| Spring Boot Version | Spring Boot Admin |
|---------------------|-------------------|
| 2.7                 | 2.7.Y             |
| 3.0                 | 3.0.Y             |
| ...                 | ...               |
| 3.3                 | 3.3.Y             |

Nevertheless, it is possible to monitor any version of a Spring Boot service independently of the underlying Spring Boot version in the service.
Hence, it is possible to run Spring Boot Admin Server version 2.6 and monitor a service that is running on Spring Boot 2.3 using Spring Boot Admin Client version 2.3.

## Getting Started

[A quick guide](https://docs.spring-boot-admin.com/current) to get started can be found in our docs.

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
### Translated versions
The following reference guides have been translated by users of Spring Boot Admin and are not part of the official bundle.
The maintainers of Spring Boot Admin will not update and maintain the guides mentioned below.

[Version 2.6.6 (Chinese translated by @qq253498229)](https://consolelog.gitee.io/docs-spring-boot-admin-docs-chinese/)

## Trademarks and licenses
The source code of codecentric's Spring Boot Admin is licensed under [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

Spring, Spring Boot and Spring Cloud are trademarks of [Pivotal Software, Inc.](https://pivotal.io/) in the U.S. and other countries.

## Snapshot builds
You can access snapshot builds from the github snapshot repository by adding the following to your `repositories`:
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

## Contributing
See [CONTRIBUTING.md](CONTRIBUTING.md) file.
