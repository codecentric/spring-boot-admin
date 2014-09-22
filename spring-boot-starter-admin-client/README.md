spring-boot-starter-admin-client
================================

This [Spring-Boot starter](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#using-boot-starter-poms "Spring Boot docu") that provides services and controllers that a required if an application show be able to be administrated with the [spring-boot-admin application](https://github.com/codecentric/spring-boot-admin "GitHub project").

This client uses the [AutoConfiguration](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#using-boot-auto-configuration "Spring Boot docu") feature of Spring Boot to register service and controller beans in the application context.

The main service that is used is a registrar that registeres the application at the spring-boot-admin application by periodically calling a REST-API to check or perform the registration of itself.

The following properties have to be included in the environment (i.e. application.properties) to ensure all features to work properly.

<table>
<tr>
<td>info.id</td><td>The identifier in the registry - this property is published by the /info endpoint</td>
</tr>
<tr>
<td>info.version</td><td>The version number - also published by the /info endpoint</td>
</tr>
<tr>
<td>spring.boot.admin.url</td><td>URL of the spring-boot-admin application to register at</td>
</tr>
<tr>
<td>logging.file</td><td>File path of the logfile of the application</td>
</tr>
</table>
