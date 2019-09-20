FROM openjdk:8-jre-alpine

COPY /target/spring-boot-admin-sample-oauth2-resource.jar app.jar

EXPOSE 8082

RUN wget -O wait-for.sh "https://raw.githubusercontent.com/eficode/wait-for/master/wait-for"
RUN chmod +x wait-for.sh

ENTRYPOINT ["/bin/sh", "-c"]