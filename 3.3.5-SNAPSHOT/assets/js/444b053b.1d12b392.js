"use strict";(self.webpackChunksite=self.webpackChunksite||[]).push([[575],{7614:(e,n,i)=>{i.r(n),i.d(n,{assets:()=>d,contentTitle:()=>a,default:()=>c,frontMatter:()=>r,metadata:()=>s,toc:()=>l});var t=i(4848),o=i(8453);const r={sidebar_position:1},a="Installation and Setup",s={id:"installation-and-setup/index",title:"Installation and Setup",description:"Overview",source:"@site/docs/installation-and-setup/index.md",sourceDirName:"installation-and-setup",slug:"/installation-and-setup/",permalink:"/docs/installation-and-setup/",draft:!1,unlisted:!1,tags:[],version:"current",sidebarPosition:1,frontMatter:{sidebar_position:1},sidebar:"tutorialSidebar",previous:{title:"Getting started",permalink:"/docs/index"},next:{title:"Spring Boot Admin Server",permalink:"/docs/server/"}},d={},l=[{value:"Overview",id:"overview",level:2},{value:"Motivation",id:"motivation",level:2},{value:"Quick Start",id:"quick-start",level:2},{value:"Setting up the Spring Boot Admin Server",id:"setting-up-the-spring-boot-admin-server",level:3},{value:"Registering Applications",id:"registering-applications",level:3},{value:"Using Spring Boot Admin Client",id:"using-spring-boot-admin-client",level:4},{value:"Using Spring Cloud Discovery",id:"using-spring-cloud-discovery",level:4}];function p(e){const n={a:"a",admonition:"admonition",code:"code",h1:"h1",h2:"h2",h3:"h3",h4:"h4",header:"header",li:"li",ol:"ol",p:"p",pre:"pre",...(0,o.R)(),...e.components};return(0,t.jsxs)(t.Fragment,{children:[(0,t.jsx)(n.header,{children:(0,t.jsx)(n.h1,{id:"installation-and-setup",children:"Installation and Setup"})}),"\n",(0,t.jsx)(n.h2,{id:"overview",children:"Overview"}),"\n",(0,t.jsx)(n.p,{children:"Spring Boot Admin works by registering Spring Boot applications that expose Actuator endpoints. Each application's\nhealth and metrics data is polled by Spring Boot Admin Server, which aggregates and displays this information in a web\ndashboard. The registered applications can either self-register or be discovered using service discovery tools like\nEureka or Consul. Through the dashboard, users can monitor the health, memory usage, logs, and more for each\napplication, and even interact with them via management endpoints for tasks like restarting or updating configurations."}),"\n",(0,t.jsx)(n.h2,{id:"motivation",children:"Motivation"}),"\n",(0,t.jsx)(n.p,{children:"In modern microservices architecture, monitoring and managing distributed systems is complex and challenging. Spring\nBoot Admin provides a powerful solution for visualizing, monitoring, and managing Spring Boot applications in real-time.\nBy offering a web interface that aggregates the health and metrics of all attached services, Spring Boot Admin\nsimplifies the process of ensuring system stability and performance. Whether you need insights into application health,\nmemory usage, or log output, Spring Boot Admin offers a centralized tool that streamlines operational management,\nhelping developers and DevOps teams maintain robust and efficient applications."}),"\n",(0,t.jsx)(n.p,{children:"While Spring Boot Admin offers a user-friendly and centralized interface for monitoring Spring Boot applications, it is\nnot designed to replace sophisticated, full-scale monitoring and observability tools like Grafana, Datadog, or Instana.\nThese tools provide advanced capabilities such as real-time alerting, history data, complex metric analysis, distributed\ntracing, and customizable dashboards across diverse environments."}),"\n",(0,t.jsx)(n.p,{children:"Spring Boot Admin excels at providing a lightweight, application-centric view with essential health checks, metrics, and\nmanagement endpoints. For production-grade observability in larger, more complex systems, integrating Spring Boot Admin\nalongside these advanced platforms ensures comprehensive system monitoring and deep insights."}),"\n",(0,t.jsx)(n.h2,{id:"quick-start",children:"Quick Start"}),"\n",(0,t.jsxs)(n.p,{children:["Since Spring Boot Admin is built on top of Spring Boot, you'll need to set up a Spring Boot application first. We\nrecommend using ",(0,t.jsx)(n.a,{href:"http://start.spring.io",children:"http://start.spring.io"})," for easy project setup. The Spring Boot Admin Server\ncan run as either in a Servlet or WebFlux application, so you'll need to choose one and add the corresponding Spring\nBoot Starter. In this example, we'll use the Servlet Web Starter."]}),"\n",(0,t.jsx)(n.h3,{id:"setting-up-the-spring-boot-admin-server",children:"Setting up the Spring Boot Admin Server"}),"\n",(0,t.jsxs)(n.p,{children:["To set up Spring Boot Admin Server, you need to add the dependency ",(0,t.jsx)(n.code,{children:"spring-boot-admin-starter-server"})," as well as\n",(0,t.jsx)(n.code,{children:"spring-boot-starter-web"})," to your project (either in your ",(0,t.jsx)(n.code,{children:"pom.xml"})," or ",(0,t.jsx)(n.code,{children:"build.gradle(.kts)"}),")."]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-xml",metastring:'title="pom.xml"',children:"\n<project>\n    <dependency>\n        <groupId>de.codecentric</groupId>\n        <artifactId>spring-boot-admin-starter-server</artifactId>\n        <version>3.3.5-SNAPSHOT</version>\n    </dependency>\n    <dependency>\n        <groupId>org.springframework.boot</groupId>\n        <artifactId>spring-boot-starter-web</artifactId>\n    </dependency>\n</project>\n"})}),"\n",(0,t.jsxs)(n.p,{children:["After that, you need to annotate your main class with ",(0,t.jsx)(n.code,{children:"@EnableAdminServer"})," to enable the Spring Boot Admin Server.\nThis will load all required configuration at runtime by leveraging Springs' autodiscovery feature."]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-java",metastring:'title="SpringBootAdminApplication.java"',children:"\n@SpringBootApplication\n@EnableAdminServer\npublic class SpringBootAdminApplication {\n\tpublic static void main(String[] args) {\n\t\tSpringApplication.run(SpringBootAdminApplication.class, args);\n\t}\n}\n"})}),"\n",(0,t.jsxs)(n.p,{children:["After starting your application, you can now access the Spring Boot Admin Server web interface at\n",(0,t.jsx)(n.code,{children:"http://localhost:8080"}),"."]}),"\n",(0,t.jsx)(n.admonition,{type:"note",children:(0,t.jsxs)(n.p,{children:["If you want to set up Spring Boot Admin Server via war-deployment in a servlet-container, please have a look at\nthe ",(0,t.jsx)(n.a,{href:"https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-war/",children:"spring-boot-admin-sample-war"}),"."]})}),"\n",(0,t.jsx)(n.admonition,{type:"note",children:(0,t.jsxs)(n.p,{children:["See also\nthe ",(0,t.jsx)(n.a,{href:"https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-servlet/",children:"spring-boot-admin-sample-servlet"}),"\nproject, which adds security."]})}),"\n",(0,t.jsx)(n.h3,{id:"registering-applications",children:"Registering Applications"}),"\n",(0,t.jsxs)(n.p,{children:["To register your application at the server, you can either include the Spring Boot Admin Client or\nuse ",(0,t.jsx)(n.a,{href:"https://spring.io/projects/spring-cloud",children:"Spring Cloud Discovery"})," (e.g. Eureka, Consul, \u2026\u200b). There is also\nan ",(0,t.jsx)(n.a,{href:"../server#spring-cloud-discovery-static-config",children:"option to use static configuration on server side"}),"."]}),"\n",(0,t.jsx)(n.h4,{id:"using-spring-boot-admin-client",children:"Using Spring Boot Admin Client"}),"\n",(0,t.jsx)(n.p,{children:"Each application that is not using Spring Cloud features but wants to register at the server has to include the Spring\nBoot Admin Client as dependency."}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-xml",metastring:'title="pom.xml"',children:"\n<project>\n    <dependency>\n        <groupId>de.codecentric</groupId>\n        <artifactId>spring-boot-admin-starter-client</artifactId>\n        <version>3.3.5-SNAPSHOT</version>\n    </dependency>\n    <dependency>\n        <groupId>org.springframework.boot</groupId>\n        <artifactId>spring-boot-starter-security</artifactId>\n    </dependency>\n</project>\n"})}),"\n",(0,t.jsxs)(n.p,{children:["After adding the dependency, you need to configure the URL of the Spring Boot Admin Server in your\n",(0,t.jsx)(n.code,{children:"application.properties"})," or ",(0,t.jsx)(n.code,{children:"application.yml"})," file as follows:"]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-properties",metastring:'title="application.properties"',children:"spring.boot.admin.client.url=http://localhost:8080  #1\nmanagement.endpoints.web.exposure.include=*  #2\nmanagement.info.env.enabled=true #3\n"})}),"\n",(0,t.jsxs)(n.ol,{children:["\n",(0,t.jsx)(n.li,{children:"This property defines the URL of the Spring Boot Admin Server."}),"\n",(0,t.jsxs)(n.li,{children:["As with Spring Boot 2 most of the endpoints aren\u2019t exposed via http by default, but we want to expose all of them.\nFor production, you should carefully choose which endpoints to expose and keep security in mind. It is also possible\nto use a different port for the actuator endpoints by setting ",(0,t.jsx)(n.code,{children:"management.port"})," property."]}),"\n",(0,t.jsx)(n.li,{children:"Since Spring Boot 2.6, the info actuator endpoint is disabled by default. In our case, we enable it to\nprovide additional information to the Spring Boot Admin Server."}),"\n"]}),"\n",(0,t.jsx)(n.p,{children:"When you start your monitored application now, it will register itself at the Spring Boot Admin Server. You can see your\napp in the web interface of Spring Boot Admin."}),"\n",(0,t.jsx)(n.admonition,{type:"info",children:(0,t.jsxs)(n.p,{children:["It is possible to add ",(0,t.jsx)(n.code,{children:"sprinb-boot-admin-client"})," as well as ",(0,t.jsx)(n.code,{children:"spring-boot-admin-server"})," to the same application. This\nallows you to monitor the Spring Boot Admin Server itself. To get a more realistic setup, you should run the Spring Boot\nAdmin Server and clients in separate applications."]})}),"\n",(0,t.jsx)(n.h4,{id:"using-spring-cloud-discovery",children:"Using Spring Cloud Discovery"}),"\n",(0,t.jsx)(n.p,{children:"If you already use Spring Cloud Discovery in your application architecture you don\u2019t need to add Spring Boot Admin\nClient. In this case you can leverage the Spring Cloud features by adding a DiscoveryClient to Spring Boot Admin Server."}),"\n",(0,t.jsxs)(n.p,{children:["The following steps uses Eureka, but other Spring Cloud Discovery implementations are supported as well. There are\nexamples\nfor ",(0,t.jsx)(n.a,{href:"https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-consul/",children:"Consul"}),"\nand ",(0,t.jsx)(n.a,{href:"https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-zookeeper/",children:"Zookeeper"}),"."]}),"\n",(0,t.jsxs)(n.p,{children:["Since Spring Boot Admin Server is fully build on top of Spring Cloud features and uses its discovery mechanism, please\nrefer to the ",(0,t.jsx)(n.a,{href:"http://projects.spring.io/spring-cloud",children:"Spring Cloud documentation"})," for more information."]}),"\n",(0,t.jsx)(n.p,{children:"To start using Eureka, you need to add the following dependencies to your project:"}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-xml",metastring:'title="pom.xml"',children:"\n<dependency>\n    <groupId>org.springframework.cloud</groupId>\n    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>\n</dependency>\n"})}),"\n",(0,t.jsxs)(n.p,{children:["After that, you have to enable discovery by adding ",(0,t.jsx)(n.code,{children:"@EnableDiscoveryClient"})," to your configuration:"]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-java",metastring:'title="SpringBootAdminApplication.java"',children:"\n@EnableDiscoveryClient\n@SpringBootApplication\n@EnableAdminServer\npublic class SpringBootAdminApplication {\n\tpublic static void main(String[] args) {\n\t\tSpringApplication.run(SpringBootAdminApplication.class, args);\n\t}\n}\n"})}),"\n",(0,t.jsxs)(n.p,{children:["The next step is to configure the Eureka client in your ",(0,t.jsx)(n.code,{children:"application.yml"})," file and define the URL of Eureka's service\nregistry."]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-yml",metastring:'title="application.yml"',children:'spring:\n  application:\n    name: spring-boot-admin-sample-eureka\n  profiles:\n    active:\n      - secure\neureka:\n  instance:\n    leaseRenewalIntervalInSeconds: 10\n    health-check-url-path: /actuator/health\n    metadata-map:\n      startup: ${random.int}    # needed to trigger info and endpoint update after restart\n  client:\n    registryFetchIntervalSeconds: 5\n    serviceUrl:\n      defaultZone: http://localhost:8761/eureka/\nmanagement:\n  endpoints:\n    web:\n      exposure:\n        include: "*"\n  endpoint:\n    health:\n      show-details: ALWAYS\n'})}),"\n",(0,t.jsx)(n.admonition,{type:"info",children:(0,t.jsxs)(n.p,{children:["There is also a ",(0,t.jsx)(n.a,{href:"https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-eureka/",children:"basic example"})," in Spring Boot Admin's GitHub repository using Eureka."]})}),"\n",(0,t.jsx)(n.admonition,{type:"tip",children:(0,t.jsxs)(n.p,{children:["You can include the Spring Boot Admin Server to your Eureka server as well. Setup everything as described above and set\n",(0,t.jsx)(n.code,{children:"spring.boot.admin.context-path"})," to something different from ",(0,t.jsx)(n.code,{children:"/"})," so that the Spring Boot Admin Server UI won\u2019t clash\nwith\nEureka\u2019s one."]})})]})}function c(e={}){const{wrapper:n}={...(0,o.R)(),...e.components};return n?(0,t.jsx)(n,{...e,children:(0,t.jsx)(p,{...e})}):p(e)}},8453:(e,n,i)=>{i.d(n,{R:()=>a,x:()=>s});var t=i(6540);const o={},r=t.createContext(o);function a(e){const n=t.useContext(r);return t.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function s(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(o):e.components||o:a(e.components),t.createElement(r.Provider,{value:n},e.children)}}}]);