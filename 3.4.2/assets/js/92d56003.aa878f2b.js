"use strict";(self.webpackChunksite=self.webpackChunksite||[]).push([[82],{9859:(e,n,i)=>{i.r(n),i.d(n,{assets:()=>l,contentTitle:()=>a,default:()=>p,frontMatter:()=>s,metadata:()=>t,toc:()=>c});const t=JSON.parse('{"id":"client/client-features","title":"Client features","description":"Show Version in Application List","source":"@site/docs/client/client-features.md","sourceDirName":"client","slug":"/client/client-features","permalink":"/3.4.2-SNAPSHOT/docs/client/client-features","draft":false,"unlisted":false,"tags":[],"version":"current","frontMatter":{"sidebar_custom_props":{"icon":"features"}},"sidebar":"tutorialSidebar","previous":{"title":"Spring Boot Admin Client","permalink":"/3.4.2-SNAPSHOT/docs/client/"},"next":{"title":"Properties","permalink":"/3.4.2-SNAPSHOT/docs/client/client-properties"}}');var o=i(4848),r=i(8453);const s={sidebar_custom_props:{icon:"features"}},a="Client features",l={},c=[{value:"Show Version in Application List",id:"show-version-in-application-list",level:2},{value:"JMX-Bean Management",id:"jmx-bean-management",level:2},{value:"Logfile Viewer",id:"logfile-viewer",level:2},{value:"Show Tags per Instance",id:"show-tags-per-instance",level:2},{value:"Spring Boot Admin Client",id:"spring-boot-admin-client",level:2}];function d(e){const n={a:"a",admonition:"admonition",code:"code",h1:"h1",h2:"h2",header:"header",li:"li",ol:"ol",p:"p",pre:"pre",strong:"strong",...(0,r.R)(),...e.components};return(0,o.jsxs)(o.Fragment,{children:[(0,o.jsx)(n.header,{children:(0,o.jsx)(n.h1,{id:"client-features",children:"Client features"})}),"\n",(0,o.jsx)(n.h2,{id:"show-version-in-application-list",children:"Show Version in Application List"}),"\n",(0,o.jsxs)(n.p,{children:["For ",(0,o.jsx)(n.strong,{children:"Spring Boot"})," applications the easiest way to show the version, is to use the ",(0,o.jsx)(n.code,{children:"build-info"})," goal from the ",(0,o.jsx)(n.code,{children:"spring-boot-maven-plugin"}),", which generates the ",(0,o.jsx)(n.code,{children:"META-INF/build-info.properties"}),". See also the ",(0,o.jsx)(n.a,{href:"http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#howto-build-info",children:"Spring Boot Reference Guide"}),"."]}),"\n",(0,o.jsxs)(n.p,{children:["For ",(0,o.jsx)(n.strong,{children:"non-Spring Boot"})," applications you can either add a ",(0,o.jsx)(n.code,{children:"version"})," or ",(0,o.jsx)(n.code,{children:"build.version"})," to the registration metadata and the version will show up in the application list."]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-xml",metastring:'title="pom.xml"',children:"<build>\n    <plugins>\n        <plugin>\n            <groupId>org.springframework.boot</groupId>\n            <artifactId>spring-boot-maven-plugin</artifactId>\n            <executions>\n                <execution>\n                    <goals>\n                        <goal>build-info</goal>\n                    </goals>\n                </execution>\n            </executions>\n        </plugin>\n    </plugins>\n</build>\n"})}),"\n",(0,o.jsxs)(n.p,{children:["To generate the build-info in a gradle project, add the following snippet to your ",(0,o.jsx)(n.code,{children:"build.gradle"}),":"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-groovy",metastring:'title="build.gradle"',children:"springBoot {\n    buildInfo()\n}\n"})}),"\n",(0,o.jsx)(n.h2,{id:"jmx-bean-management",children:"JMX-Bean Management"}),"\n",(0,o.jsx)(n.p,{children:"ATTENTION: Spring Boot 3 does currently not support Jolokia, so this will not work with Spring Boot 3 based applications. You can still monitor Spring Boot 2 applications with Jolokia endpoint using a Spring Boot Admin 3 server."}),"\n",(0,o.jsxs)(n.p,{children:["To interact with JMX-beans in the admin UI you have to include ",(0,o.jsx)(n.a,{href:"https://jolokia.org/",children:"Jolokia"})," in your application. As Jolokia is servlet based there is no support for reactive applications. In case you are using the ",(0,o.jsx)(n.code,{children:"spring-boot-admin-starter-client"})," it will be pulled in for you, if not add Jolokia to your dependencies. With Spring Boot 2.2.0 you might want to set ",(0,o.jsx)(n.code,{children:"spring.jmx.enabled=true"})," if you want to expose Spring beans via JMX."]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-xml",metastring:'title="pom.xml"',children:"<dependency>\n    <groupId>org.jolokia</groupId>\n    <artifactId>jolokia-core</artifactId>\n</dependency>\n"})}),"\n",(0,o.jsx)(n.h2,{id:"logfile-viewer",children:"Logfile Viewer"}),"\n",(0,o.jsxs)(n.p,{children:["By default, the logfile is not accessible via actuator endpoints and therefore not visible in Spring Boot Admin. In order to enable the logfile actuator endpoint you need to configure Spring Boot to write a logfile, either by setting",(0,o.jsx)(n.code,{children:"logging.file.path"})," or ",(0,o.jsx)(n.code,{children:"logging.file.name"}),"."]}),"\n",(0,o.jsx)(n.p,{children:"Spring Boot Admin will detect everything that looks like an URL and render it as hyperlink."}),"\n",(0,o.jsx)(n.p,{children:"ANSI color-escapes are also supported. You need to set a custom file log pattern as Spring Boot\u2019s default one doesn\u2019t use colors."}),"\n",(0,o.jsxs)(n.p,{children:["To enforce the use of ANSI-colored output, set ",(0,o.jsx)(n.code,{children:"spring.output.ansi.enabled=ALWAYS"}),". Otherwise Spring tries to detect if ANSI-colored output is available and might disable it."]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-properties",metastring:'title="application.properties"',children:"logging.file.name=/var/log/sample-boot-application.log (1)\nlogging.pattern.file=%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(%5p) %clr(${PID}){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n%wEx (2)\n"})}),"\n",(0,o.jsxs)(n.ol,{children:["\n",(0,o.jsx)(n.li,{children:"Destination the logfile is written to. Enables the logfile actuator endpoint."}),"\n",(0,o.jsx)(n.li,{children:"File log pattern using ANSI colors."}),"\n"]}),"\n",(0,o.jsx)(n.h2,{id:"show-tags-per-instance",children:"Show Tags per Instance"}),"\n",(0,o.jsxs)(n.p,{children:[(0,o.jsx)(n.code,{children:"Tags"})," are a way to add visual markers per instance, they will appear in the application list as well as in the instance view. By default, no tags are added to instances, and it\u2019s up to the client to specify the desired tags by adding the information to the metadata or info endpoint."]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-properties",metastring:'title="application.properties"',children:"#using the metadata\nspring.boot.admin.client.instance.metadata.tags.environment=test\n\n#using the info endpoint\ninfo.tags.environment=test\n"})}),"\n",(0,o.jsx)(n.h2,{id:"spring-boot-admin-client",children:"Spring Boot Admin Client"}),"\n",(0,o.jsx)(n.p,{children:"The Spring Boot Admin Client registers the application at the admin server. This is done by periodically doing a HTTP post request to the SBA Server providing information about the application."}),"\n",(0,o.jsx)(n.admonition,{type:"tip",children:(0,o.jsx)(n.p,{children:"There are plenty of properties to influence the way how the SBA Client registers your application. In case that doesn\u2019t fit your needs, you can provide your own ApplicationFactory implementation."})})]})}function p(e={}){const{wrapper:n}={...(0,r.R)(),...e.components};return n?(0,o.jsx)(n,{...e,children:(0,o.jsx)(d,{...e})}):d(e)}},8453:(e,n,i)=>{i.d(n,{R:()=>s,x:()=>a});var t=i(6540);const o={},r=t.createContext(o);function s(e){const n=t.useContext(r);return t.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function a(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(o):e.components||o:s(e.components),t.createElement(r.Provider,{value:n},e.children)}}}]);