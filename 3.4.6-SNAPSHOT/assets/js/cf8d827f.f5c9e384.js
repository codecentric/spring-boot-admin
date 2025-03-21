"use strict";(self.webpackChunksite=self.webpackChunksite||[]).push([[373],{5608:(e,t,i)=>{i.r(t),i.d(t,{assets:()=>c,contentTitle:()=>o,default:()=>h,frontMatter:()=>d,metadata:()=>r,toc:()=>a});const r=JSON.parse('{"id":"server/server-properties","title":"Properties","description":"| Property name                                               | Description                                                                                                                                                                                                                                                                                                                                                                                                       | Default value                                                                                                                                                                |","source":"@site/docs/server/99-server-properties.md","sourceDirName":"server","slug":"/server/server-properties","permalink":"/3.4.6-SNAPSHOT/docs/server/server-properties","draft":false,"unlisted":false,"tags":[],"version":"current","sidebarPosition":99,"frontMatter":{"sidebar_custom_props":{"icon":"properties"}},"sidebar":"tutorialSidebar","previous":{"title":"Foster Security","permalink":"/3.4.6-SNAPSHOT/docs/server/security"},"next":{"title":"Spring Boot Admin Client","permalink":"/3.4.6-SNAPSHOT/docs/client/"}}');var s=i(4848),n=i(8453);const d={sidebar_custom_props:{icon:"properties"}},o="Properties",c={},a=[];function l(e){const t={a:"a",code:"code",h1:"h1",header:"header",strong:"strong",table:"table",tbody:"tbody",td:"td",th:"th",thead:"thead",tr:"tr",...(0,n.R)(),...e.components};return(0,s.jsxs)(s.Fragment,{children:[(0,s.jsx)(t.header,{children:(0,s.jsx)(t.h1,{id:"properties",children:"Properties"})}),"\n",(0,s.jsxs)(t.table,{children:[(0,s.jsx)(t.thead,{children:(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.th,{children:"Property name"}),(0,s.jsx)(t.th,{children:"Description"}),(0,s.jsx)(t.th,{children:"Default value"})]})}),(0,s.jsxs)(t.tbody,{children:[(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.server.enabled"})}),(0,s.jsx)(t.td,{children:"Enables the Spring Boot Admin Server."}),(0,s.jsx)(t.td,{children:"true"})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.context-path"})}),(0,s.jsx)(t.td,{children:"The context-path prefixes the path where the Admin Server\u2019s statics assets and API should be served. Relative to the Dispatcher-Servlet."}),(0,s.jsx)(t.td,{})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.monitor.status-interval"})}),(0,s.jsx)(t.td,{children:"Time interval to check the status of instances, must be greater than 1 second."}),(0,s.jsx)(t.td,{children:"10,000ms"})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.monitor.status-max-backoff"})}),(0,s.jsx)(t.td,{children:"The maximal backoff for status check retries (retry after error has exponential backoff, minimum backoff is 1 second)."}),(0,s.jsx)(t.td,{children:"60,000ms"})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.monitor.status-lifetime"})}),(0,s.jsx)(t.td,{children:"Lifetime of status. The status won\u2019t be updated as long the last status isn\u2019t expired."}),(0,s.jsx)(t.td,{children:"10,000ms"})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.monitor.info-interval"})}),(0,s.jsx)(t.td,{children:"Time interval to check the info of instances."}),(0,s.jsx)(t.td,{children:"1m"})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.monitor.info-max-backoff"})}),(0,s.jsx)(t.td,{children:"The maximal backoff for info check retries (retry after error has exponential backoff, minimum backoff is 1 second)."}),(0,s.jsx)(t.td,{children:"10m"})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.monitor.info-lifetime"})}),(0,s.jsx)(t.td,{children:"Lifetime of info. The info won\u2019t be updated as long the last info isn\u2019t expired."}),(0,s.jsx)(t.td,{children:"1m"})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.monitor.default-timeout"})}),(0,s.jsx)(t.td,{children:"Default timeout when making requests. Individual values for specific endpoints can be overridden using spring.boot.admin.monitor.timeout.*. However, for interval based tasks like statusUpdate (i.e. HealthCheck) there are some limitations: the default-timeout cannot be longer than the interval. If so, the specified value of the interval is used as timeout."}),(0,s.jsx)(t.td,{children:"10,000"})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsxs)(t.td,{children:[(0,s.jsx)(t.code,{children:"spring.boot.admin.monitor.timeout."}),"*"]}),(0,s.jsx)(t.td,{children:"Key-Value-Pairs with the timeout per endpointId. Defaults to default-timeout."}),(0,s.jsx)(t.td,{})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.monitor.default-retries"})}),(0,s.jsx)(t.td,{children:"Default number of retries for failed requests. Modifying requests (PUT, POST, PATCH, DELETE) are never retried. Individual values for specific endpoints can be overridden using spring.boot.admin.monitor.retries.*."}),(0,s.jsx)(t.td,{children:"0"})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsxs)(t.td,{children:[(0,s.jsx)(t.code,{children:"spring.boot.admin.monitor.retries."}),"*"]}),(0,s.jsx)(t.td,{children:"Key-Value-Pairs with the number of retries per endpointId. Defaults to default-retries. Modifying requests (PUT, POST, PATCH, DELETE) are never retried."}),(0,s.jsx)(t.td,{})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.metadata-keys-to-sanitize"})}),(0,s.jsxs)(t.td,{children:["Metadata values for the keys matching these regex patterns will be sanitized in all json output. Starting from Spring Boot 3, all actuator values are masked by default. Take a look at the Spring Boot documentation in order to configure unsanitizing of values (",(0,s.jsx)(t.a,{href:"https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto.actuator.sanitize-sensitive-values",children:"Sanitize Sensitive Values"}),")."]}),(0,s.jsxs)(t.td,{children:['".',(0,s.jsx)(t.strong,{children:'password$", ".*secret$", ".*key$", ".*token$", ".*credentials.'}),'", ".*vcap_services$"']})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.probed-endpoints"})}),(0,s.jsxs)(t.td,{children:["For Spring Boot 1.x client applications SBA probes for the specified endpoints using an OPTIONS request. If the path differs from the id you can specify this as id",":path"," (e.g. health",":ping",").."]}),(0,s.jsxs)(t.td,{children:['"health", "env", "metrics", "httptrace',":trace",'", "threaddump',":dump",'", "jolokia", "info", "logfile", "refresh", "flyway", "liquibase", "heapdump", "loggers", "auditevents"']})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.instance-auth.enabled"})}),(0,s.jsx)(t.td,{children:"Enable pulling credentials from spring configuration properties"}),(0,s.jsx)(t.td,{children:"true"})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.instance-auth.default-user-name"})}),(0,s.jsx)(t.td,{children:"A default user name used to authenticate to registered services. The spring.boot.admin.instance-auth.enabled property must be true."}),(0,s.jsx)(t.td,{children:"null"})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.instance-auth.default-password"})}),(0,s.jsx)(t.td,{children:"A default user password used to authenticate to registered services. The spring.boot.admin.instance-auth.enabled property must be true."}),(0,s.jsx)(t.td,{children:"null"})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsxs)(t.td,{children:[(0,s.jsx)(t.code,{children:"spring.boot.admin.instance-auth.service-map."}),"*.user-name"]}),(0,s.jsx)(t.td,{children:"A user name used to authenticate to the registered service with the specified name. The spring.boot.admin.instance-auth.enabled property must be true."}),(0,s.jsx)(t.td,{})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsxs)(t.td,{children:[(0,s.jsx)(t.code,{children:"spring.boot.admin.instance-auth.service-map."}),"*.user-password"]}),(0,s.jsx)(t.td,{children:"A user password used to authenticate to the registered service with the specified name. The spring.boot.admin.instance-auth.enabled property must be true."}),(0,s.jsx)(t.td,{})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.instance-proxy.ignored-headers"})}),(0,s.jsx)(t.td,{children:"Headers not to be forwarded when making requests to clients."}),(0,s.jsx)(t.td,{children:'"Cookie", "Set-Cookie", "Authorization"'})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.ui.public-url"})}),(0,s.jsx)(t.td,{children:"Base url to use to build the base href in the ui."}),(0,s.jsx)(t.td,{children:"If running behind a reverse proxy (using path rewriting) this can be used to make correct self references. If the host/port is omitted it will be inferred from the request."})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.ui.brand"})}),(0,s.jsx)(t.td,{children:"Brand to be shown in the navbar."}),(0,s.jsx)(t.td,{children:'"<img src="assets/img/icon-spring-boot-admin.svg"><span>Spring Boot Admin</span>"'})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.ui.title"})}),(0,s.jsx)(t.td,{children:"Page-Title to be shown."}),(0,s.jsx)(t.td,{children:'"Spring Boot Admin"'})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.ui.login-icon"})}),(0,s.jsx)(t.td,{children:"Icon used as image on login page."}),(0,s.jsx)(t.td,{children:'"assets/img/icon-spring-boot-admin.svg"'})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.ui.favicon"})}),(0,s.jsx)(t.td,{children:"Icon used as default favicon and icon for desktop notifications."}),(0,s.jsx)(t.td,{children:'"assets/img/favicon.png"'})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.ui.favicon-danger"})}),(0,s.jsx)(t.td,{children:"Icon used as favicon when one or more service is down and for desktop notifications."}),(0,s.jsx)(t.td,{children:'"assets/img/favicon-danger.png"'})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.ui.remember-me-enabled"})}),(0,s.jsx)(t.td,{children:"Switch to show/hide the remember-me checkbox on the login page."}),(0,s.jsx)(t.td,{children:"true"})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.ui.poll-timer.cache"})}),(0,s.jsx)(t.td,{children:"Polling duration in ms to fetch new cache data."}),(0,s.jsx)(t.td,{children:"2500"})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.ui.poll-timer.datasource"})}),(0,s.jsx)(t.td,{children:"Polling duration in ms to fetch new datasource data."}),(0,s.jsx)(t.td,{children:"2500"})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.ui.poll-timer.gc"})}),(0,s.jsx)(t.td,{children:"Polling duration in ms to fetch new gc data."}),(0,s.jsx)(t.td,{children:"2500"})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.ui.poll-timer.process"})}),(0,s.jsx)(t.td,{children:"Polling duration in ms to fetch new process data."}),(0,s.jsx)(t.td,{children:"2500"})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.ui.poll-timer.memory"})}),(0,s.jsx)(t.td,{children:"Polling duration in ms to fetch new memory data."}),(0,s.jsx)(t.td,{children:"2500"})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.ui.poll-timer.threads"})}),(0,s.jsx)(t.td,{children:"Polling duration in ms to fetch new threads data."}),(0,s.jsx)(t.td,{children:"2500"})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.ui.poll-timer.logfile"})}),(0,s.jsx)(t.td,{children:"Polling duration in ms to fetch new logfile data."}),(0,s.jsx)(t.td,{children:"1000"})]}),(0,s.jsxs)(t.tr,{children:[(0,s.jsx)(t.td,{children:(0,s.jsx)(t.code,{children:"spring.boot.admin.ui.enable-toasts"})}),(0,s.jsx)(t.td,{children:"Allows to enable toast notifications."}),(0,s.jsx)(t.td,{children:"false"})]})]})]})]})}function h(e={}){const{wrapper:t}={...(0,n.R)(),...e.components};return t?(0,s.jsx)(t,{...e,children:(0,s.jsx)(l,{...e})}):l(e)}},8453:(e,t,i)=>{i.d(t,{R:()=>d,x:()=>o});var r=i(6540);const s={},n=r.createContext(s);function d(e){const t=r.useContext(n);return r.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}function o(e){let t;return t=e.disableParentContext?"function"==typeof e.components?e.components(s):e.components||s:d(e.components),r.createElement(n.Provider,{value:t},e.children)}}}]);