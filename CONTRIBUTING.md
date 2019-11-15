# Contributing to Spring Boot Admin

Contriubutions are highly welcome. Feel free to submit Pull Requests. Maybe watch out for tickets tagged with `ideal-for-contribution`, these tickets should always be a good starting point for contributing.   

You can find some hints for starting development in the [README](spring-boot-admin-server-ui/README.md) of `spring-boot-admin-server-ui`.

## Coding Conventions
### Java / Server
We try to satisfy the [Code Style of Spring Framework](https://github.com/spring-projects/spring-framework/wiki/Code-Style). If you are using IntelliJ, you can find the needed settings [here](https://github.com/spring-projects/spring-framework/wiki/IntelliJ-IDEA-Editor-Settings).

The [Spring Java Format Plugins](https://github.com/spring-io/spring-javaformat) are added to the build. Checkstyle will enforce the consistency of the code. Nevertheless there are some disabled rules, due to backward compatibility. You can find these disabled rules in a comment in `src/checkstyle.xml`.

If you are using IntelliJ, there is also a [formatter-plugin provided by Spring](https://github.com/spring-io/spring-javaformat#intellij-idea).

Otherwise you can always run `mvn spring-javaformat:apply` to fix some basic errors, like indentation.

### JavaScript / Client
tbd
