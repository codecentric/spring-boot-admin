# Spring Boot Admin GraalVM sample application

## Build Project

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
