FROM openjdk:8-jre-alpine

COPY /target/spring-boot-admin-sample-oauth2-admin.jar app.jar

EXPOSE 8080

# Add wait-for
COPY wait-for.sh wait-for.sh
RUN chmod +x wait-for.sh

ENTRYPOINT ["/bin/sh", "-c"]