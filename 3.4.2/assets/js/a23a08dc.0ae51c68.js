"use strict";(self.webpackChunksite=self.webpackChunksite||[]).push([[701],{1254:(e,n,t)=>{t.r(n),t.d(n,{assets:()=>c,contentTitle:()=>a,default:()=>h,frontMatter:()=>o,metadata:()=>i,toc:()=>d});const i=JSON.parse('{"id":"server/server","title":"Set up the Server","description":"Running Behind a Front-end Proxy Server","source":"@site/docs/server/01-server.md","sourceDirName":"server","slug":"/server/server","permalink":"/3.4.2-SNAPSHOT/docs/server/server","draft":false,"unlisted":false,"tags":[],"version":"current","sidebarPosition":1,"frontMatter":{"sidebar_custom_props":{"icon":"server"}},"sidebar":"tutorialSidebar","previous":{"title":"Spring Boot Admin Server","permalink":"/3.4.2-SNAPSHOT/docs/server/"},"next":{"title":"Add Notifications","permalink":"/3.4.2-SNAPSHOT/docs/server/notifications/"}}');var r=t(4848),s=t(8453);const o={sidebar_custom_props:{icon:"server"}},a="Set up the Server",c={},d=[{value:"Running Behind a Front-end Proxy Server",id:"running-behind-a-front-end-proxy-server",level:2},{value:"Spring Cloud Discovery",id:"spring-cloud-discovery",level:2},{value:"Static Configuration using SimpleDiscoveryClient",id:"static-configuration-using-simplediscoveryclient",level:3},{value:"Other DiscoveryClients",id:"other-discoveryclients",level:3},{value:"Converting ServiceInstances",id:"converting-serviceinstances",level:3},{value:"CloudFoundry",id:"cloudfoundry",level:3},{value:"Clustering",id:"clustering",level:2}];function l(e){const n={a:"a",admonition:"admonition",code:"code",em:"em",h1:"h1",h2:"h2",h3:"h3",header:"header",li:"li",ol:"ol",p:"p",pre:"pre",strong:"strong",table:"table",tbody:"tbody",td:"td",th:"th",thead:"thead",tr:"tr",...(0,s.R)(),...e.components};return(0,r.jsxs)(r.Fragment,{children:[(0,r.jsx)(n.header,{children:(0,r.jsx)(n.h1,{id:"set-up-the-server",children:"Set up the Server"})}),"\n",(0,r.jsx)(n.h2,{id:"running-behind-a-front-end-proxy-server",children:"Running Behind a Front-end Proxy Server"}),"\n",(0,r.jsxs)(n.p,{children:["In case the Spring Boot Admin server is running behind a reverse proxy, it may be requried to configure the public url where the server is reachable via (",(0,r.jsx)(n.code,{children:"spring.boot.admin.ui.public-url"}),"). In addition when the reverse proxy terminates the https connection, it may be necessary to configure ",(0,r.jsx)(n.code,{children:"server.forward-headers-strategy=native"})," (also see ",(0,r.jsx)(n.a,{href:"https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto-use-tomcat-behind-a-proxy-server",children:"Spring Boot Reference Guide"}),")."]}),"\n",(0,r.jsx)(n.h2,{id:"spring-cloud-discovery",children:"Spring Cloud Discovery"}),"\n",(0,r.jsxs)(n.p,{children:["The Spring Boot Admin Server can use Spring Clouds ",(0,r.jsx)(n.code,{children:"DiscoveryClient"})," to discover applications. The advantage is that the clients don\u2019t have to include the ",(0,r.jsx)(n.code,{children:"spring-boot-admin-starter-client"}),". You just have to add a ",(0,r.jsx)(n.code,{children:"DiscoveryClient"})," implementation to your admin server - everything else is done by AutoConfiguration."]}),"\n",(0,r.jsx)(n.h3,{id:"static-configuration-using-simplediscoveryclient",children:"Static Configuration using SimpleDiscoveryClient"}),"\n",(0,r.jsxs)(n.p,{children:["Spring Cloud provides a ",(0,r.jsx)(n.code,{children:"SimpleDiscoveryClient"}),". It allows you to specify client applications via static configuration:"]}),"\n",(0,r.jsx)(n.pre,{children:(0,r.jsx)(n.code,{className:"language-xml",metastring:'title="pom.xml"',children:"<dependency>\n    <groupId>org.springframework.cloud</groupId>\n    <artifactId>spring-cloud-starter</artifactId>\n</dependency>\n"})}),"\n",(0,r.jsx)(n.pre,{children:(0,r.jsx)(n.code,{className:"language-yaml",metastring:'title="application.yml"',children:"spring:\n  cloud:\n    discovery:\n      client:\n        simple:\n          instances:\n            test:\n              - uri: http://instance1.intern:8080\n                metadata:\n                  management.context-path: /actuator\n              - uri: http://instance2.intern:8080\n                metadata:\n                  management.context-path: /actuator\n"})}),"\n",(0,r.jsx)(n.h3,{id:"other-discoveryclients",children:"Other DiscoveryClients"}),"\n",(0,r.jsxs)(n.p,{children:["Spring Boot Admin supports all other implementations of Spring Cloud\u2019s ",(0,r.jsx)(n.code,{children:"DiscoveryClient"})," (",(0,r.jsx)(n.a,{href:"https://docs.spring.io/spring-cloud-netflix/docs/current/reference/html/#service-discovery-eureka-clients/",children:"Eureka"}),", ",(0,r.jsx)(n.a,{href:"https://docs.spring.io/spring-cloud-zookeeper/docs/current/reference/html/#spring-cloud-zookeeper-discovery",children:"Zookeeper"}),", ",(0,r.jsx)(n.a,{href:"https://docs.spring.io/spring-cloud-consul/docs/current/reference/html/#spring-cloud-consul-discovery",children:"Consul"}),", ",(0,r.jsx)(n.a,{href:"https://docs.spring.io/spring-cloud-kubernetes/docs/current/reference/html/#discoveryclient-for-kubernetes",children:"Kubernetes"}),", \u2026\u200b). You need to add it to the Spring Boot Admin Server and configure it properly. An ",(0,r.jsx)(n.a,{href:"/docs/index#discover-clients-via-spring-cloud-discovery",children:"example setup using Eureka"})," is shown above."]}),"\n",(0,r.jsx)(n.h3,{id:"converting-serviceinstances",children:"Converting ServiceInstances"}),"\n",(0,r.jsxs)(n.p,{children:["The information from the service registry are converted by the ",(0,r.jsx)(n.code,{children:"ServiceInstanceConverter"}),". Spring Boot Admin ships with a default and Eureka converter implementation. The correct one is selected by AutoConfiguration."]}),"\n",(0,r.jsx)(n.admonition,{type:"tip",children:(0,r.jsx)(n.p,{children:"You can modify how the information from the registry is used to register the application by using SBA Server configuration options and instance metadata. The values from the metadata takes precedence over the server config. If the plenty of options don\u2019t fit your needs you can provide your own ServiceInstanceConverter."})}),"\n",(0,r.jsx)(n.admonition,{type:"tip",children:(0,r.jsx)(n.p,{children:"When using Eureka, the healthCheckUrl known to Eureka is used for health-checking, which can be set on your client using eureka.instance.healthCheckUrl."})}),"\n",(0,r.jsx)(n.p,{children:(0,r.jsx)(n.strong,{children:"Instance metadata options"})}),"\n",(0,r.jsxs)(n.table,{children:[(0,r.jsx)(n.thead,{children:(0,r.jsxs)(n.tr,{children:[(0,r.jsx)(n.th,{children:"Key"}),(0,r.jsx)(n.th,{children:"Value"}),(0,r.jsx)(n.th,{children:"Default value"})]})}),(0,r.jsxs)(n.tbody,{children:[(0,r.jsxs)(n.tr,{children:[(0,r.jsx)(n.td,{children:"user.nameuser.password"}),(0,r.jsx)(n.td,{children:"Credentials being used to access the endpoints."}),(0,r.jsx)(n.td,{})]}),(0,r.jsxs)(n.tr,{children:[(0,r.jsx)(n.td,{children:"management.scheme"}),(0,r.jsx)(n.td,{children:"The scheme is substituted in the service URL and will be used for accessing the actuator endpoints."}),(0,r.jsx)(n.td,{})]}),(0,r.jsxs)(n.tr,{children:[(0,r.jsx)(n.td,{children:"management.address"}),(0,r.jsx)(n.td,{children:"The address is substituted in the service URL and will be used for accessing the actuator endpoints."}),(0,r.jsx)(n.td,{})]}),(0,r.jsxs)(n.tr,{children:[(0,r.jsx)(n.td,{children:"management.port"}),(0,r.jsx)(n.td,{children:"The port is substituted in the service URL and will be used for accessing the actuator endpoints."}),(0,r.jsx)(n.td,{})]}),(0,r.jsxs)(n.tr,{children:[(0,r.jsx)(n.td,{children:"management.context-path"}),(0,r.jsx)(n.td,{children:"The path is appended to the service URL and will be used for accessing the actuator endpoints."}),(0,r.jsx)(n.td,{children:"${spring.boot.admin.discovery.converter.management-context-path}"})]}),(0,r.jsxs)(n.tr,{children:[(0,r.jsx)(n.td,{children:"health.path"}),(0,r.jsx)(n.td,{children:"The path is appended to the service URL and will be used for the health-checking. Ignored by the EurekaServiceInstanceConverter."}),(0,r.jsx)(n.td,{children:"${spring.boot.admin.discovery.converter.health-endpoint}"})]}),(0,r.jsxs)(n.tr,{children:[(0,r.jsx)(n.td,{children:"group"}),(0,r.jsx)(n.td,{children:"The group is used to group services in the UI by the group name instead of application name."}),(0,r.jsx)(n.td,{})]})]})]}),"\n",(0,r.jsx)(n.p,{children:(0,r.jsx)(n.strong,{children:"Discovery configuration options"})}),"\n",(0,r.jsxs)(n.table,{children:[(0,r.jsx)(n.thead,{children:(0,r.jsxs)(n.tr,{children:[(0,r.jsx)(n.th,{children:"Property name"}),(0,r.jsx)(n.th,{children:"Description"}),(0,r.jsx)(n.th,{children:"Default value"})]})}),(0,r.jsxs)(n.tbody,{children:[(0,r.jsxs)(n.tr,{children:[(0,r.jsx)(n.td,{children:"spring.boot.admin.discovery.enabled"}),(0,r.jsx)(n.td,{children:"Enables the DiscoveryClient-support for the admin server."}),(0,r.jsx)(n.td,{children:"true"})]}),(0,r.jsxs)(n.tr,{children:[(0,r.jsx)(n.td,{children:"spring.boot.admin.discovery.converter.management-context-path"}),(0,r.jsx)(n.td,{children:"Will be appended to the service-url of the discovered service when the management-url is converted by the DefaultServiceInstanceConverter."}),(0,r.jsx)(n.td,{children:"/actuator"})]}),(0,r.jsxs)(n.tr,{children:[(0,r.jsx)(n.td,{children:"spring.boot.admin.discovery.converter.health-endpoint-path"}),(0,r.jsx)(n.td,{children:"Will be appended to the management-url of the discovered service when the health-url is converted by the DefaultServiceInstanceConverter."}),(0,r.jsx)(n.td,{children:'"health"'})]}),(0,r.jsxs)(n.tr,{children:[(0,r.jsx)(n.td,{children:"spring.boot.admin.discovery.ignored-services"}),(0,r.jsxs)(n.td,{children:['This services will be ignored when using discovery and not registered as application. Supports simple patterns (e.g. "foo*", "',(0,r.jsx)(n.em,{children:'bar", "foo'}),'bar*").']}),(0,r.jsx)(n.td,{})]}),(0,r.jsxs)(n.tr,{children:[(0,r.jsx)(n.td,{children:"spring.boot.admin.discovery.services"}),(0,r.jsxs)(n.td,{children:['This services will be included when using discovery and registered as application. Supports simple patterns (e.g. "foo*", "',(0,r.jsx)(n.em,{children:'bar", "foo'}),'bar*").']}),(0,r.jsx)(n.td,{children:'"*"'})]}),(0,r.jsxs)(n.tr,{children:[(0,r.jsx)(n.td,{children:"spring.boot.admin.discovery.ignored-instances-metadata"}),(0,r.jsx)(n.td,{children:'Instances of services will be ignored if they contain at least one metadata item that matches this list. (e.g. "discoverable=false")'}),(0,r.jsx)(n.td,{})]}),(0,r.jsxs)(n.tr,{children:[(0,r.jsx)(n.td,{children:"spring.boot.admin.discovery.instances-metadata"}),(0,r.jsx)(n.td,{children:'Instances of services will be included if they contain at least one metadata item that matches this list. (e.g. "discoverable=true")'}),(0,r.jsx)(n.td,{})]})]})]}),"\n",(0,r.jsx)(n.h3,{id:"cloudfoundry",children:"CloudFoundry"}),"\n",(0,r.jsxs)(n.p,{children:["If you are deploying your applications to CloudFoundry then ",(0,r.jsx)(n.code,{children:"vcap.application.application_id"})," and ",(0,r.jsx)(n.code,{children:"vcap.application.instance_index"})," ",(0,r.jsx)(n.strong,{children:(0,r.jsx)(n.em,{children:"must"})})," be added to the metadata for proper registration of applications with Spring Boot Admin Server. Here is a sample configuration for Eureka:"]}),"\n",(0,r.jsx)(n.pre,{children:(0,r.jsx)(n.code,{className:"language-yml",metastring:'title="application.yml"',children:"eureka:\n  instance:\n    hostname: ${vcap.application.uris[0]}\n    nonSecurePort: 80\n    metadata-map:\n      applicationId: ${vcap.application.application_id}\n      instanceId: ${vcap.application.instance_index}\n"})}),"\n",(0,r.jsx)(n.h2,{id:"clustering",children:"Clustering"}),"\n",(0,r.jsxs)(n.p,{children:["Spring Boot Admin Server supports cluster replication via Hazelcast. It is automatically enabled when a ",(0,r.jsx)(n.code,{children:"HazelcastConfig"}),"- or ",(0,r.jsx)(n.code,{children:"HazelcastInstance"}),"-Bean is present. You can also configure the Hazelcast instance to be persistent, to keep the status over restarts. Also have a look at the ",(0,r.jsx)(n.a,{href:"http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#boot-features-hazelcast/",children:"Spring Boot support for Hazelcast"}),"."]}),"\n",(0,r.jsxs)(n.ol,{children:["\n",(0,r.jsx)(n.li,{children:"Add Hazelcast to your dependencies:"}),"\n"]}),"\n",(0,r.jsx)(n.pre,{children:(0,r.jsx)(n.code,{className:"language-xml",metastring:'title="pom.xml"',children:"<dependency>  \n    <groupId>com.hazelcast</groupId>  \n    <artifactId>hazelcast</artifactId>  \n</dependency>  \n"})}),"\n",(0,r.jsxs)(n.ol,{start:"2",children:["\n",(0,r.jsx)(n.li,{children:"Instantiate a HazelcastConfig:"}),"\n"]}),"\n",(0,r.jsx)(n.pre,{children:(0,r.jsx)(n.code,{className:"language-java",metastring:'title="HazelcastConfig.java" ',children:'@Bean  \npublic Config hazelcastConfig() {  \n    // This map is used to store the events.  \n    // It should be configured to reliably hold all the data,  \n    // Spring Boot Admin will compact the events, if there are too many  \n    MapConfig eventStoreMap = new MapConfig(DEFAULT_NAME_EVENT_STORE_MAP).setInMemoryFormat(InMemoryFormat.OBJECT)  \n        .setBackupCount(1)  \n        .setMergePolicyConfig(new MergePolicyConfig(PutIfAbsentMergePolicy.class.getName(), 100));  \n    // This map is used to deduplicate the notifications.  \n    // If data in this map gets lost it should not be a big issue as it will atmost  \n    // lead to  \n    // the same notification to be sent by multiple instances  \n    MapConfig sentNotificationsMap = new MapConfig(DEFAULT_NAME_SENT_NOTIFICATIONS_MAP)  \n        .setInMemoryFormat(InMemoryFormat.OBJECT)  \n        .setBackupCount(1)  \n        .setEvictionConfig(  \n                new EvictionConfig().setEvictionPolicy(EvictionPolicy.LRU).setMaxSizePolicy(MaxSizePolicy.PER_NODE))  \n        .setMergePolicyConfig(new MergePolicyConfig(PutIfAbsentMergePolicy.class.getName(), 100));  \n    Config config = new Config();  \n    config.addMapConfig(eventStoreMap);  \n    config.addMapConfig(sentNotificationsMap);  \n    config.setProperty("hazelcast.jmx", "true");  \n    // WARNING: This setups a local cluster, you change it to fit your needs.  \n    config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);  \n    TcpIpConfig tcpIpConfig = config.getNetworkConfig().getJoin().getTcpIpConfig();  \n    tcpIpConfig.setEnabled(true);  \n    tcpIpConfig.setMembers(singletonList("127.0.0.1"));  \n    return config;  \n}  \n'})}),"\n",(0,r.jsx)(n.p,{children:(0,r.jsx)(n.strong,{children:"Hazelcast configuration options"})}),"\n",(0,r.jsxs)(n.table,{children:[(0,r.jsx)(n.thead,{children:(0,r.jsxs)(n.tr,{children:[(0,r.jsx)(n.th,{children:"Property name"}),(0,r.jsx)(n.th,{children:"Description"}),(0,r.jsx)(n.th,{children:"Default value"})]})}),(0,r.jsxs)(n.tbody,{children:[(0,r.jsxs)(n.tr,{children:[(0,r.jsx)(n.td,{children:"spring.boot.admin.hazelcast.enabled"}),(0,r.jsx)(n.td,{children:"Enables the Hazelcast support"}),(0,r.jsx)(n.td,{children:"true"})]}),(0,r.jsxs)(n.tr,{children:[(0,r.jsx)(n.td,{children:"spring.boot.admin.hazelcast.event-store"}),(0,r.jsx)(n.td,{children:"Name of the Hazelcast-map to store the events"}),(0,r.jsx)(n.td,{children:'"spring-boot-admin-event-store"'})]}),(0,r.jsxs)(n.tr,{children:[(0,r.jsx)(n.td,{children:"spring.boot.admin.hazelcast.sent-notifications"}),(0,r.jsx)(n.td,{children:"Name of the Hazelcast-map used to deduplicate the notifications."}),(0,r.jsx)(n.td,{children:'"spring-boot-admin-sent-notifications"'})]})]})]})]})}function h(e={}){const{wrapper:n}={...(0,s.R)(),...e.components};return n?(0,r.jsx)(n,{...e,children:(0,r.jsx)(l,{...e})}):l(e)}},8453:(e,n,t)=>{t.d(n,{R:()=>o,x:()=>a});var i=t(6540);const r={},s=i.createContext(r);function o(e){const n=i.useContext(s);return i.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function a(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(r):e.components||r:o(e.components),i.createElement(s.Provider,{value:n},e.children)}}}]);