# Spring Boot Admin GraalVM sample application

This is a sample project running a Spring Boot Admin server which works with GraalVM and Native Image Builder.

In order to show basic functionalities, the server itself is registered as a client.

## Build project

```
$ mvn clean package -Pnative
```

## Build an OCI image that can be run with Docker

```
$ mvn spring-boot:build-image
```

## Running the example

```
$ docker run --rm -p 8080:8080 docker.io/library/spring-boot-admin-sample-servlet-graalvm:3.0.1-SNAPSHOT
```
