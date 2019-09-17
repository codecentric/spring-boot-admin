FROM openjdk:8-jre-alpine

COPY /target/spring-boot-admin-sample-oauth2-authorization.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java","-jar","app.jar"]