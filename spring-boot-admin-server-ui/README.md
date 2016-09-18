spring-boot-admin-server-ui
================================

### Building this module
The jar **can be build with Maven** with the maven-exec-plugin. To do this node.js and npm must be installed on your machine and be on your $PATH.
If you dont want to use the maven exec run the following commands:

### Build
```shell
npm install
npm run build
```

Repeated build with watching the files:
```shell
npm run watch:js
```

### Run dev-server
This starts a dev-server serving the ui only at port 9000. The rest requests are forwarded to port 8080.
```shell
npm run dev-server
```

## Run tests
```shell
npm run test
```

Repeated tests with watching the files:
```shell
npm run watch:test
```