FROM tomcat:8.5

ENV UAA_CONFIG_PATH=/usr/local/tomcat/webapps

WORKDIR /usr/local/tomcat/conf
RUN sed -i -e 's/8080/8081/' server.xml

WORKDIR /usr/local/tomcat/webapps
RUN wget -O uaa.war "https://repo1.maven.org/maven2/org/cloudfoundry/identity/cloudfoundry-identity-uaa/4.30.0/\
cloudfoundry-identity-uaa-4.30.0.war"

COPY config/uaa.yml $UAA_CONFIG_PATH/uaa.yml

EXPOSE 8081