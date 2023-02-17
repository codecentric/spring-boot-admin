# Spring Boot Admin GraalVM sample application

This is a sample project running a Spring Boot Admin server which works with GraalVM and Native Image Builder.

In order to show basic functionalities, the server itself is registered as a client.

## Build project

Make sure to use a GraalVM with a v17-BaseJDK to build the project (e.g. 22.3.r17-grl).

```
$ mvn clean package -Pnative
```

## Build an OCI image that can be run with Docker

```
$ mvn spring-boot:build-image -Dspring-boot.build-image.imageName=spring-boot-admin-sample-servlet-graalvm:latest
```

## Running the example

```
$ docker run --rm -p 8080:8080 docker.io/library/spring-boot-admin-sample-servlet-graalvm:latest
```

You should now be able to access Spring Boot Admin locally under `http://localhost:8080/`.

## Current limitations of Spring Boot's native image build feature

Keep in mind that currently not all Spring modules have built-in support. Therefore, you might need to tell the AOT compiler about the usage of reflection, dynamic proxies etc. There are several ways to deal with these concerns. A good starting point for specifying additional native configuration can be found in the official [Spring documentation](https://docs.spring.io/spring-framework/docs/6.0.0/reference/html/core.html#aot-hints).
