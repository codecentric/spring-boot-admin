spring-boot-admin-server-ui
================================

### Building this module
The jar **can be build with Maven** with the maven-exec-plugin. To do this node.js and npm must be installed on your machine and be on your `$PATH`.
If you don't want to use the maven exec run the following commands:

### Running Spring Boot Admin Server for development
To develop the ui on an running server the best to do is

1. Running the ui build in watch mode so the resources get updated:
```shell
npm run watch
```
2. Run a Spring Boot Admin Server instances with the template-location and resource-location pointing to the build output and disable caching:
```
spring.boot.admin.ui.cache.no-cache: true
spring.boot.admin.ui.resource-locations: file:@project.basedir@/../../spring-boot-admin-server-ui/target/dist/
spring.boot.admin.ui.template-location: file:@project.basedir@/../../spring-boot-admin-server-ui/target/dist/
spring.boot.admin.ui.cache-templates: false
```
Or just start the [spring-boot-admin-sample-servlet](../spring-boot-admin-samples/spring-boot-admin-sample-servlet)
project using the `dev` profile. You also might want to use the `insecure` profile so you don't need to login.

### Build
```shell
npm install
npm run build
```

Repeated build with watching the files:
```shell
npm run watch
```

## Run tests
```shell
npm run test
```

Repeated tests with watching the files:
```shell
npm run watch:test
```
