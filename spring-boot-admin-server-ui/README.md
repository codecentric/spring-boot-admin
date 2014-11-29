spring-boot-admin-server-ui
================================

This is the user-interface for the spring-boot-admin-server.
There are neither configuration nor customization options. 
If you want to change something, copy this project and build your own static resources and ship 'em with your spring-boot-admin-server.

### Building this module
The jar **can be build with Maven** with the maven-exec-plugin, node.js and npm must be installed on your machine and be on your $PATH.
If you dont want to use the maven exec run the following commands:

### Build
```shell
npm install
gulp
```

### Run server (with watchify)
```shell
npm install
gulp watch
```

