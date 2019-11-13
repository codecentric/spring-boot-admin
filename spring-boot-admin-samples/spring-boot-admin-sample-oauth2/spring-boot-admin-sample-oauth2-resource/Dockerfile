FROM openjdk:8-jre-alpine

RUN apk --no-cache add curl

COPY /target/spring-boot-admin-sample-oauth2-resource.jar app.jar

RUN wget -O wait-for-command.sh "https://raw.githubusercontent.com/ettore26/wait-for-command/master/wait-for-command.sh"
RUN chmod +x wait-for-command.sh

ENTRYPOINT ["/bin/sh", "-c"]

EXPOSE 8082