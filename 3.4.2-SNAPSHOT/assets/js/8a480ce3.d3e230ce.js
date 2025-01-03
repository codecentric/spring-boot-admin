"use strict";(self.webpackChunksite=self.webpackChunksite||[]).push([[225],{1577:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>a,contentTitle:()=>c,default:()=>p,frontMatter:()=>i,metadata:()=>s,toc:()=>u});const s=JSON.parse('{"id":"customize/customize_interceptors","title":"HTTP Interceptors","description":"You can intercept and modify requests and responses made to the monitored application\u2019s actuator endpoints by implementing the InstanceExchangeFilterFunction interface. This can be useful for auditing or adding some extra security checks.","source":"@site/docs/customize/04-customize_interceptors.md","sourceDirName":"customize","slug":"/customize/customize_interceptors","permalink":"/3.4.2-SNAPSHOT/docs/customize/customize_interceptors","draft":false,"unlisted":false,"tags":[],"version":"current","sidebarPosition":4,"frontMatter":{"sidebar_custom_props":{"icon":"http"}},"sidebar":"tutorialSidebar","previous":{"title":"HTTP Headers","permalink":"/3.4.2-SNAPSHOT/docs/customize/customize_http-headers"},"next":{"title":"Third Party Integrations","permalink":"/3.4.2-SNAPSHOT/docs/third-party/"}}');var r=n(4848),o=n(8453);const i={sidebar_custom_props:{icon:"http"}},c="HTTP Interceptors",a={},u=[];function d(e){const t={code:"code",h1:"h1",header:"header",p:"p",pre:"pre",...(0,o.R)(),...e.components};return(0,r.jsxs)(r.Fragment,{children:[(0,r.jsx)(t.header,{children:(0,r.jsx)(t.h1,{id:"http-interceptors",children:"HTTP Interceptors"})}),"\n",(0,r.jsxs)(t.p,{children:["You can intercept and modify requests and responses made to the monitored application\u2019s actuator endpoints by implementing the ",(0,r.jsx)(t.code,{children:"InstanceExchangeFilterFunction"})," interface. This can be useful for auditing or adding some extra security checks."]}),"\n",(0,r.jsx)(t.pre,{children:(0,r.jsx)(t.code,{className:"language-java",metastring:'title="CustomHttpInterceptor.java"',children:'@Bean\npublic InstanceExchangeFilterFunction auditLog() {\n    return (instance, request, next) -> next.exchange(request).doOnSubscribe((s) -> {\n        if (HttpMethod.DELETE.equals(request.method()) || HttpMethod.POST.equals(request.method())) {\n            log.info("{} for {} on {}", request.method(), instance.getId(), request.url());\n        }\n    });\n}\n'})})]})}function p(e={}){const{wrapper:t}={...(0,o.R)(),...e.components};return t?(0,r.jsx)(t,{...e,children:(0,r.jsx)(d,{...e})}):d(e)}},8453:(e,t,n)=>{n.d(t,{R:()=>i,x:()=>c});var s=n(6540);const r={},o=s.createContext(r);function i(e){const t=s.useContext(o);return s.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}function c(e){let t;return t=e.disableParentContext?"function"==typeof e.components?e.components(r):e.components||r:i(e.components),s.createElement(o.Provider,{value:t},e.children)}}}]);