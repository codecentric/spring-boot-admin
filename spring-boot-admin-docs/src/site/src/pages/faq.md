# FAQ

## Can I include spring-boot-admin into my business application?
**tl;dr** You can, but you shouldn't. +
You can set `spring.boot.admin.context-path` to alter the path where the UI and REST-API is served, but depending on the complexity of your application you might get in trouble. On the other hand in my opinion it makes no sense for an application to monitor itself. In case your application goes down your monitoring tool also does.

## Can I change or reload Spring Boot properties at runtime?
Yes, you can refresh the entire environment or set/update individual properties for both single instances as well as for the entire application.
Note, however, that the Spring Boot application needs to have [Spring Cloud Commons](https://docs.spring.io/spring-cloud-commons/docs/current/reference/html/#endpoints) and `management.endpoint.env.post.enabled=true` in place.
Also check the details of `@RefreshScope` https://docs.spring.io/spring-cloud-commons/docs/current/reference/html/#refresh-scope.
