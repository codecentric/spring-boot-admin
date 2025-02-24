"use strict";(self.webpackChunksite=self.webpackChunksite||[]).push([[825],{3368:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>o,contentTitle:()=>a,default:()=>h,frontMatter:()=>d,metadata:()=>r,toc:()=>c});const r=JSON.parse('{"id":"client/client-properties","title":"Properties","description":"Spring Boot Admin Client configuration options","source":"@site/docs/client/client-properties.md","sourceDirName":"client","slug":"/client/client-properties","permalink":"/3.4.4/docs/client/client-properties","draft":false,"unlisted":false,"tags":[],"version":"current","frontMatter":{"sidebar_custom_props":{"icon":"properties"}},"sidebar":"tutorialSidebar","previous":{"title":"Client features","permalink":"/3.4.4/docs/client/client-features"},"next":{"title":"Customizing","permalink":"/3.4.4/docs/customize/"}}');var i=n(4848),s=n(8453);const d={sidebar_custom_props:{icon:"properties"}},a="Properties",o={},c=[];function l(e){const t={h1:"h1",header:"header",p:"p",strong:"strong",table:"table",tbody:"tbody",td:"td",th:"th",thead:"thead",tr:"tr",...(0,s.R)(),...e.components};return(0,i.jsxs)(i.Fragment,{children:[(0,i.jsx)(t.header,{children:(0,i.jsx)(t.h1,{id:"properties",children:"Properties"})}),"\n",(0,i.jsx)(t.p,{children:(0,i.jsx)(t.strong,{children:"Spring Boot Admin Client configuration options"})}),"\n",(0,i.jsxs)(t.table,{children:[(0,i.jsx)(t.thead,{children:(0,i.jsxs)(t.tr,{children:[(0,i.jsx)(t.th,{children:"Property name"}),(0,i.jsx)(t.th,{children:"Description"}),(0,i.jsx)(t.th,{children:"Default value"})]})}),(0,i.jsxs)(t.tbody,{children:[(0,i.jsxs)(t.tr,{children:[(0,i.jsx)(t.td,{children:"spring.boot.admin.client.enabled"}),(0,i.jsx)(t.td,{children:"Enables the Spring Boot Admin Client."}),(0,i.jsx)(t.td,{children:"true"})]}),(0,i.jsxs)(t.tr,{children:[(0,i.jsx)(t.td,{children:"spring.boot.admin.client.url"}),(0,i.jsxs)(t.td,{children:["Comma separated ordered list of URLs of the Spring Boot Admin server to register at. This triggers the AutoConfiguration. ",(0,i.jsx)(t.strong,{children:"Mandatory"}),"."]}),(0,i.jsx)(t.td,{})]}),(0,i.jsxs)(t.tr,{children:[(0,i.jsx)(t.td,{children:"spring.boot.admin.client.api-path"}),(0,i.jsx)(t.td,{children:"Http-path of registration endpoint at your admin server."}),(0,i.jsx)(t.td,{children:'"instances"'})]}),(0,i.jsxs)(t.tr,{children:[(0,i.jsx)(t.td,{children:"spring.boot.admin.client.usernamespring.boot.admin.client.password"}),(0,i.jsx)(t.td,{children:"Username and password in case the SBA Server api is protected with HTTP Basic authentication."}),(0,i.jsx)(t.td,{})]}),(0,i.jsxs)(t.tr,{children:[(0,i.jsx)(t.td,{children:"spring.boot.admin.client.period"}),(0,i.jsx)(t.td,{children:"Interval for repeating the registration (in ms)."}),(0,i.jsx)(t.td,{children:"10,000"})]}),(0,i.jsxs)(t.tr,{children:[(0,i.jsx)(t.td,{children:"spring.boot.admin.client.connect-timeout"}),(0,i.jsx)(t.td,{children:"Connect timeout for the registration (in ms)."}),(0,i.jsx)(t.td,{children:"5,000"})]}),(0,i.jsxs)(t.tr,{children:[(0,i.jsx)(t.td,{children:"spring.boot.admin.client.read-timeout"}),(0,i.jsx)(t.td,{children:"Read timeout for the registration (in ms)."}),(0,i.jsx)(t.td,{children:"5,000"})]}),(0,i.jsxs)(t.tr,{children:[(0,i.jsx)(t.td,{children:"spring.boot.admin.client.auto-registration"}),(0,i.jsx)(t.td,{children:"If set to true the periodic task to register the application is automatically scheduled after the application is ready."}),(0,i.jsx)(t.td,{children:"true"})]}),(0,i.jsxs)(t.tr,{children:[(0,i.jsx)(t.td,{children:"spring.boot.admin.client.auto-deregistration"}),(0,i.jsx)(t.td,{children:"Switch to enable auto-deregistration at Spring Boot Admin server when context is closed. If the value is unset the feature is active if a running CloudPlatform was detected."}),(0,i.jsx)(t.td,{children:"null"})]}),(0,i.jsxs)(t.tr,{children:[(0,i.jsx)(t.td,{children:"spring.boot.admin.client.register-once"}),(0,i.jsx)(t.td,{children:"If set to true the client will only register against one admin server (in order defined by spring.boot.admin.instance.url); if that admin server goes down, will automatically register against the next admin server. If false, will register against all admin servers."}),(0,i.jsx)(t.td,{children:"true"})]}),(0,i.jsxs)(t.tr,{children:[(0,i.jsx)(t.td,{children:"spring.boot.admin.client.instance.health-url"}),(0,i.jsx)(t.td,{children:"Health-url to register with. Can be overridden in case the reachable URL is different (e.g. Docker). Must be unique in registry."}),(0,i.jsx)(t.td,{children:"Guessed based on management-base-url and endpoints.health.id."})]}),(0,i.jsxs)(t.tr,{children:[(0,i.jsx)(t.td,{children:"spring.boot.admin.client.instance.management-base-url"}),(0,i.jsx)(t.td,{children:"Base url for computing the management-url to register with. The path is inferred at runtime, and appended to the base url."}),(0,i.jsx)(t.td,{children:"Guessed based on management.server.port, service-url and server.servlet-path."})]}),(0,i.jsxs)(t.tr,{children:[(0,i.jsx)(t.td,{children:"spring.boot.admin.client.instance.management-url"}),(0,i.jsx)(t.td,{children:"Management-url to register with. Can be overridden in case the reachable url is different (e.g. Docker)."}),(0,i.jsx)(t.td,{children:"Guessed based on management-base-url and management.server.base-path."})]}),(0,i.jsxs)(t.tr,{children:[(0,i.jsx)(t.td,{children:"spring.boot.admin.client.instance.service-base-url"}),(0,i.jsx)(t.td,{children:"Base url for computing the service-url to register with. The path is inferred at runtime, and appended to the base url. In Cloudfoundry environments you can switching to https like this: spring.boot.admin.client.instance.service-base-url=https://${vcap.application.uris[0]}"}),(0,i.jsx)(t.td,{children:"Guessed based on hostname, server.port."})]}),(0,i.jsxs)(t.tr,{children:[(0,i.jsx)(t.td,{children:"spring.boot.admin.client.instance.service-url"}),(0,i.jsx)(t.td,{children:"Service-url to register with. Can be overridden in case the reachable url is different (e.g. Docker)."}),(0,i.jsx)(t.td,{children:"Guessed based on service-base-url and server.context-path."})]}),(0,i.jsxs)(t.tr,{children:[(0,i.jsx)(t.td,{children:"spring.boot.admin.client.instance.service-path"}),(0,i.jsx)(t.td,{children:"Service-path to register with. Can be overridden in case the reachable path is different (e.g. context-path set programmatically)."}),(0,i.jsx)(t.td,{children:"/"})]}),(0,i.jsxs)(t.tr,{children:[(0,i.jsx)(t.td,{children:"spring.boot.admin.client.instance.name"}),(0,i.jsx)(t.td,{children:"Name to register with."}),(0,i.jsx)(t.td,{children:'${spring.application.name} if set, "spring-boot-application" otherwise.'})]}),(0,i.jsxs)(t.tr,{children:[(0,i.jsx)(t.td,{children:"spring.boot.admin.client.instance.service-host-type"}),(0,i.jsx)(t.td,{children:"Select which information should be considered when sending the host of a service:* IP: Uses the IP returned by InetAddress.getHostAddress()* HOST_NAME: Uses the host name of a single machine returned by InetAddress.getHostName()* CANONICAL_HOST_NAME: Uses the FQDN returned by InetAddress.geCanonicalHostName()If server.address or management.server.address is set in the service, the value will overrule this property."}),(0,i.jsx)(t.td,{children:"CANONICAL_HOST_NAME"})]}),(0,i.jsxs)(t.tr,{children:[(0,i.jsx)(t.td,{children:"spring.boot.admin.client.instance.metadata.*"}),(0,i.jsx)(t.td,{children:"Metadata key-value-pairs to be associated with this instance."}),(0,i.jsx)(t.td,{})]}),(0,i.jsxs)(t.tr,{children:[(0,i.jsx)(t.td,{children:"spring.boot.admin.client.instance.metadata.tags.*"}),(0,i.jsx)(t.td,{children:"Tags as key-value-pairs to be associated with this instance."}),(0,i.jsx)(t.td,{})]})]})]}),"\n",(0,i.jsx)(t.p,{children:(0,i.jsx)(t.strong,{children:"Instance metadata options"})}),"\n",(0,i.jsxs)(t.table,{children:[(0,i.jsx)(t.thead,{children:(0,i.jsxs)(t.tr,{children:[(0,i.jsx)(t.th,{children:"Key"}),(0,i.jsx)(t.th,{children:"Value"}),(0,i.jsx)(t.th,{children:"Default value"})]})}),(0,i.jsx)(t.tbody,{children:(0,i.jsxs)(t.tr,{children:[(0,i.jsx)(t.td,{children:"user.nameuser.password"}),(0,i.jsx)(t.td,{children:"Credentials being used to access the endpoints."}),(0,i.jsx)(t.td,{})]})})]})]})}function h(e={}){const{wrapper:t}={...(0,s.R)(),...e.components};return t?(0,i.jsx)(t,{...e,children:(0,i.jsx)(l,{...e})}):l(e)}},8453:(e,t,n)=>{n.d(t,{R:()=>d,x:()=>a});var r=n(6540);const i={},s=r.createContext(i);function d(e){const t=r.useContext(s);return r.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}function a(e){let t;return t=e.disableParentContext?"function"==typeof e.components?e.components(i):e.components||i:d(e.components),r.createElement(s.Provider,{value:t},e.children)}}}]);