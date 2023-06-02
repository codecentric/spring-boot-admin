# Contributing to Spring Boot Admin
Contributions are highly welcome. Feel free to submit Pull Requests. Maybe watch out for tickets tagged with `ideal-for-contribution`, these tickets should always be a good starting point for contributing.

You can find some hints for starting development in the [README](spring-boot-admin-server-ui/README.md) of `spring-boot-admin-server-ui`.

## Coding Conventions
### Java / Server
We try to satisfy the [Code Style of Spring Framework](https://github.com/spring-projects/spring-framework/wiki/Code-Style).

The [Spring Java Format Plugin](https://github.com/spring-io/spring-javaformat) is added to the build. Checkstyle will enforce the consistency of the code. Nevertheless there are some disabled rules, due to backward compatibility. You can find these disabled rules in a comment in `src/checkstyle/checkstyle.xml`.

However, you can always run `mvn spring-javaformat:apply` to fix some basic errors, like indentation.

### JavaScript / Client
The Vue frontend implements basic prettier rules as well as Vue3 recommended rules.
To check if there are any violations run `npm run lint` and `npm run format`.
Append `:fix` to let eslint or prettier auto-solve most issues.

## Working with the code
### IntelliJ
The IntelliJ settings are based on the IntelliJ-IDEA-Editor-Settings from spring, but have been adapted slightly, you can find the original settings [here](https://github.com/spring-projects/spring-framework/wiki/IntelliJ-IDEA-Editor-Settings).
The custom settings are stored in `.editorconfig` and are imported automatically by IntelliJ.
If you are using IntelliJ, there is also a [formatter-plugin provided by Spring](https://github.com/spring-io/spring-javaformat#intellij-idea).
(i) Plugin version x didn't not work in IntelliJ IDEA Ultimate 2020.3.

#### Checkstyle Plugin
This plugin scans Java files with the project's custom CheckStyle rules from within IDEA.
Install and configure the Checkstyle Plugin, and enable the configuration file.

##### Configuration
Before configuration, add the  `spring-javaformat-checkstyle` JAR to the Third-Party checks.
1. Preferences > Tools > Checkstyle > Third-Party Checks
2. Add `~/.m2/repository/io/spring/javaformat/spring-javaformat-checkstyle/0.0.26/spring-javaformat-checkstyle-0.0.26.jar`

##### Configuration File
Add the configuration file and enabled it:
1. Preferences > Tools > Checkstyle > Configuration File > +
2. Add a Name, ex. Spring Boot Admin
3. Use a local Checkstyle File, Browse to `src/checkstyle/checkstyle.xml` and click Next
4. Enter the full path to the checkstyle header file: `<git repo>/src/checkstyle/checkstyle-header.txt`, click Finish
5. Select the new configuration file to enable it

#### Prettier Plugin
This plugin is able to run Prettier from within IntelliJ. It can even be configured to run on "Reformat Code" action. It comes bundles with the IDE but needs to be enabled.

##### Configuration
1. Preferences > Languages & Frameworks > JavaScript > Prettier
2. Manual Prettier configuration
   1. Prettier package: `<git repo>/spring-boot-admin-server-ui/node_modules/prettier`
   2. Enable "Run on 'Reformat Code' action"
