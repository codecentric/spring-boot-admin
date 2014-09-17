spring-boot-admin
=================

This is a simple admin interface for [Spring Boot](http://projects.spring.io/spring-boot/ "Official Spring-Boot website") applications.

This application provides a simple GUI to administrate Spring Boot applications in some ways. At the moment it provides the following features for every registered application.

<ul>
<li>Show name/id and version number</li>
<li>Show online status</li>
<li>Download main logfile</li>
<li>Show details, like</li>
<ul>
<li>Java system properties</li>
<li>Java environment properties</li>
<li>Memory metrics</li>
<li>Spring environment properties</li>
</ul>
</ul> 

#### Client applications

Each application that want to register itself to the admin application has to include the [spring-boot-starter-admin-client](https://github.com/dickerpulli/spring-boot-starter-admin-client "GitHub project") as dependency. This starter JAR includes some [AutoConfiguration](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#using-boot-auto-configuration "Spring Boot docu") features that includes registering tasks, controller, etc.

#### Screenshot:

[](url "title") 
<img src="https://raw.githubusercontent.com/dickerpulli/spring-boot-admin/master/screenshot.png">
