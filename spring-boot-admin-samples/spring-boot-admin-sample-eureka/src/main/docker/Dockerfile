FROM eclipse-temurin:21
VOLUME /tmp
ADD target/spring-boot-admin-sample-eureka.jar /app.jar
RUN bash -c 'touch /app.jar'
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
