spring-boot-admin-sample-custom-ui
================================

### Building this module
The jar **can be build with Maven** using the frontend-maven-plugin. This will download node.js and npm automatically.
If you don't want to use the maven exec run the following commands:

### Running Spring Boot Admin Server for development
To develop the ui on an running server the best to do is

1. Running the ui build in watch mode so the resources get updated:
```shell
npm run build:watch
```
2. Run a Spring Boot Admin Server instances with the template-location and resource-location pointing to the build output and disable caching:
```
spring.boot.admin.ui.cache.no-cache: true
spring.boot.admin.ui.extension-resource-locations: file:../spring-boot-admin-sample-custom-ui/target/dist/
spring.boot.admin.ui.cache-templates: false
```
Or just start the [spring-boot-admin-sample-servlet](../spring-boot-admin-sample-servlet) project using the `dev` profile.

### Build
```shell
npm install
npm run build
```

Repeated build with watching the files:
```shell
npm run build:watch
```
