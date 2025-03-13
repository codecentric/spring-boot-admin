---
sidebar_custom_props:
  icon: 'features'
---

# Client features

## Show Version in Application List

For **Spring Boot** applications the easiest way to show the version, is to use the `build-info` goal from the `spring-boot-maven-plugin`, which generates the `META-INF/build-info.properties`. See also the [Spring Boot Reference Guide](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#howto-build-info).

For **non-Spring Boot** applications you can either add a `version` or `build.version` to the registration metadata and the version will show up in the application list.

```xml title="pom.xml"
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <executions>
                <execution>
                    <goals>
                        <goal>build-info</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

To generate the build-info in a gradle project, add the following snippet to your `build.gradle`:

```groovy title="build.gradle"
springBoot {
    buildInfo()
}
```

## JMX-Bean Management

ATTENTION: Spring Boot 3 does currently not support Jolokia, so this will not work with Spring Boot 3 based applications. You can still monitor Spring Boot 2 applications with Jolokia endpoint using a Spring Boot Admin 3 server.

To interact with JMX-beans in the admin UI you have to include [Jolokia](https://jolokia.org/) in your application. As Jolokia is servlet based there is no support for reactive applications. In case you are using the `spring-boot-admin-starter-client` it will be pulled in for you, if not add Jolokia to your dependencies. With Spring Boot 2.2.0 you might want to set `spring.jmx.enabled=true` if you want to expose Spring beans via JMX.

```xml title="pom.xml"
<dependency>
    <groupId>org.jolokia</groupId>
    <artifactId>jolokia-core</artifactId>
</dependency>
```

## Logfile Viewer

By default, the logfile is not accessible via actuator endpoints and therefore not visible in Spring Boot Admin. In order to enable the logfile actuator endpoint you need to configure Spring Boot to write a logfile, either by setting`logging.file.path` or `logging.file.name`.

Spring Boot Admin will detect everything that looks like an URL and render it as hyperlink.

ANSI color-escapes are also supported. You need to set a custom file log pattern as Spring Boot’s default one doesn’t use colors.

To enforce the use of ANSI-colored output, set `spring.output.ansi.enabled=ALWAYS`. Otherwise Spring tries to detect if ANSI-colored output is available and might disable it.

```properties title="application.properties"
logging.file.name=/var/log/sample-boot-application.log (1)
logging.pattern.file=%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(%5p) %clr(${PID}){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n%wEx (2)
```

1. Destination the logfile is written to. Enables the logfile actuator endpoint.
2. File log pattern using ANSI colors.

## Show Tags per Instance

`Tags` are a way to add visual markers per instance, they will appear in the application list as well as in the instance view. By default, no tags are added to instances, and it’s up to the client to specify the desired tags by adding the information to the metadata or info endpoint.

```properties title="application.properties"
#using the metadata
spring.boot.admin.client.instance.metadata.tags.environment=test

#using the info endpoint
info.tags.environment=test
```

## Spring Boot Admin Client

The Spring Boot Admin Client registers the application at the admin server. This is done by periodically doing a HTTP post request to the SBA Server providing information about the application.

:::tip
There are plenty of properties to influence the way how the SBA Client registers your application. In case that doesn’t fit your needs, you can provide your own ApplicationFactory implementation.
:::
