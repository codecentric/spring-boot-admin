"use strict";(self.webpackChunksite=self.webpackChunksite||[]).push([[84],{9963:(e,n,o)=>{o.r(n),o.d(n,{assets:()=>a,contentTitle:()=>c,default:()=>l,frontMatter:()=>s,metadata:()=>t,toc:()=>p});const t=JSON.parse('{"type":"mdx","permalink":"/3.4.3-SNAPSHOT/faq","source":"@site/src/pages/faq.md","title":"FAQ","description":"Can I include spring-boot-admin into my business application?","frontMatter":{},"unlisted":false}');var i=o(4848),r=o(8453);const s={},c="FAQ",a={},p=[{value:"Can I include spring-boot-admin into my business application?",id:"can-i-include-spring-boot-admin-into-my-business-application",level:2},{value:"Can I change or reload Spring Boot properties at runtime?",id:"can-i-change-or-reload-spring-boot-properties-at-runtime",level:2}];function d(e){const n={a:"a",code:"code",h1:"h1",h2:"h2",header:"header",p:"p",strong:"strong",...(0,r.R)(),...e.components};return(0,i.jsxs)(i.Fragment,{children:[(0,i.jsx)(n.header,{children:(0,i.jsx)(n.h1,{id:"faq",children:"FAQ"})}),"\n",(0,i.jsx)(n.h2,{id:"can-i-include-spring-boot-admin-into-my-business-application",children:"Can I include spring-boot-admin into my business application?"}),"\n",(0,i.jsxs)(n.p,{children:[(0,i.jsx)(n.strong,{children:"tl;dr"})," You can, but you shouldn't. +\nYou can set ",(0,i.jsx)(n.code,{children:"spring.boot.admin.context-path"})," to alter the path where the UI and REST-API is served, but depending on the complexity of your application you might get in trouble. On the other hand in my opinion it makes no sense for an application to monitor itself. In case your application goes down your monitoring tool also does."]}),"\n",(0,i.jsx)(n.h2,{id:"can-i-change-or-reload-spring-boot-properties-at-runtime",children:"Can I change or reload Spring Boot properties at runtime?"}),"\n",(0,i.jsxs)(n.p,{children:["Yes, you can refresh the entire environment or set/update individual properties for both single instances as well as for the entire application.\nNote, however, that the Spring Boot application needs to have ",(0,i.jsx)(n.a,{href:"https://docs.spring.io/spring-cloud-commons/docs/current/reference/html/#endpoints%5BSpring",children:"https://docs.spring.io/spring-cloud-commons/docs/current/reference/html/#endpoints[Spring"})," Cloud Commons] and ",(0,i.jsx)(n.code,{children:"management.endpoint.env.post.enabled=true"})," in place.\nAlso check the details of ",(0,i.jsx)(n.code,{children:"@RefreshScope"})," ",(0,i.jsx)(n.a,{href:"https://docs.spring.io/spring-cloud-commons/docs/current/reference/html/#refresh-scope",children:"https://docs.spring.io/spring-cloud-commons/docs/current/reference/html/#refresh-scope"}),"."]})]})}function l(e={}){const{wrapper:n}={...(0,r.R)(),...e.components};return n?(0,i.jsx)(n,{...e,children:(0,i.jsx)(d,{...e})}):d(e)}},8453:(e,n,o)=>{o.d(n,{R:()=>s,x:()=>c});var t=o(6540);const i={},r=t.createContext(i);function s(e){const n=t.useContext(r);return t.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function c(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(i):e.components||i:s(e.components),t.createElement(r.Provider,{value:n},e.children)}}}]);