# Spring Boot Admin Client

Spring Boot Admin Client registers the application at the admin server. This is done by periodically doing a HTTP post request to the SBA Server providing information about the application.

<div class="tip">

There are plenty of properties to influence the way how the SBA Client registers your application. In case that doesn’t fit your needs, you can provide your own `ApplicationFactory` implementation.

</div>

# Show version in application list

For **Spring Boot** applications the easiest way to show the version, is to use the `build-info` goal from the `spring-boot-maven-plugin`, which generates the `META-INF/build-info.properties`. See also the [Spring Boot Reference Guide](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#howto-build-info).

For **non-Spring Boot** applications you can either add a `version` or `build.version` to the registration metadata and the version will show up in the application list.

<div class="formalpara-title">

**pom.xml**

</div>

``` xml
<build> <plugins> <plugin> <groupId>org.springframework.boot</groupId> <artifactId>spring-boot-maven-plugin</artifactId> <executions> <execution> <goals> <goal>build-info</goal> </goals> </execution> </executions> </plugin> </plugins> </build>
```

To generate the build-info in a gradle project, add the following snippet to your `build.gradle`:

<div class="formalpara-title">

**build.gradle**

</div>

``` groovy
springBoot { buildInfo() }
```

# JMX-Bean Management

<div class="warning">

Spring Boot 3 does currently not support Jolokia, so this will not work with Spring Boot 3 based applications. You can still monitor Spring Boot 2 applications with Jolokia endpoint using a Spring Boot Admin 3 server.

</div>

To interact with JMX-beans in the admin UI you have to include [Jolokia](https://jolokia.org/) in your application. As Jolokia is servlet based there is no support for reactive applications. In case you are using the `spring-boot-admin-starter-client` it will be pulled in for you, if not add Jolokia to your dependencies. With Spring Boot 2.2.0 you might want to set `spring.jmx.enabled=true` if you want to expose Spring beans via JMX.

<div class="formalpara-title">

**pom.xml**

</div>

``` xml
<dependency> <groupId>org.jolokia</groupId> <artifactId>jolokia-core</artifactId> </dependency>
```

# Logfile Viewer

By default, the logfile is not accessible via actuator endpoints and therefore not visible in Spring Boot Admin. In order to enable the logfile actuator endpoint you need to configure Spring Boot to write a logfile, either by setting `logging.file.path` or `logging.file.name`.

Spring Boot Admin will detect everything that looks like an URL and render it as hyperlink.

ANSI color-escapes are also supported. You need to set a custom file log pattern as Spring Boot’s default one doesn’t use colors.

To enforce the use of ANSI-colored output, set `spring.output.ansi.enabled=ALWAYS`. Otherwise Spring tries to detect if ANSI-colored output is available and might disable it.

<div class="formalpara-title">

**application.properties**

</div>

``` properties
logging.file.name=/var/log/sample-boot-application.log  logging.pattern.file=%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(%5p) %clr(${PID}){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n%wEx 
```

- Destination the logfile is written to. Enables the logfile actuator endpoint.

- File log pattern using ANSI colors.
