"use strict";(self.webpackChunksite=self.webpackChunksite||[]).push([[472],{2450:(e,t,s)=>{s.r(t),s.d(t,{assets:()=>c,contentTitle:()=>i,default:()=>p,frontMatter:()=>a,metadata:()=>n,toc:()=>d});const n=JSON.parse('{"id":"customize/customize_http-headers","title":"HTTP Headers","description":"In case you need to inject custom HTTP headers into the requests made to the monitored application\u2019s actuator endpoints you can easily add a HttpHeadersProvider:","source":"@site/docs/customize/03-customize_http-headers.md","sourceDirName":"customize","slug":"/customize/customize_http-headers","permalink":"/3.4.3/docs/customize/customize_http-headers","draft":false,"unlisted":false,"tags":[],"version":"current","sidebarPosition":3,"frontMatter":{"sidebar_custom_props":{"icon":"http"}},"sidebar":"tutorialSidebar","previous":{"title":"Extend the UI","permalink":"/3.4.3/docs/customize/extend_ui"},"next":{"title":"HTTP Interceptors","permalink":"/3.4.3/docs/customize/customize_interceptors"}}');var r=s(4848),o=s(8453);const a={sidebar_custom_props:{icon:"http"}},i="HTTP Headers",c={},d=[];function u(e){const t={code:"code",h1:"h1",header:"header",p:"p",pre:"pre",...(0,o.R)(),...e.components};return(0,r.jsxs)(r.Fragment,{children:[(0,r.jsx)(t.header,{children:(0,r.jsx)(t.h1,{id:"http-headers",children:"HTTP Headers"})}),"\n",(0,r.jsxs)(t.p,{children:["In case you need to inject custom HTTP headers into the requests made to the monitored application\u2019s actuator endpoints you can easily add a ",(0,r.jsx)(t.code,{children:"HttpHeadersProvider"}),":"]}),"\n",(0,r.jsx)(t.pre,{children:(0,r.jsx)(t.code,{className:"language-java",metastring:'title="CustomHttpHeadersProvider.java"',children:'@Bean\npublic HttpHeadersProvider customHttpHeadersProvider() {\n    return (instance) -> {\n        HttpHeaders httpHeaders = new HttpHeaders();\n        httpHeaders.add("X-CUSTOM", "My Custom Value");\n        return httpHeaders;\n    };\n}\n'})})]})}function p(e={}){const{wrapper:t}={...(0,o.R)(),...e.components};return t?(0,r.jsx)(t,{...e,children:(0,r.jsx)(u,{...e})}):u(e)}},8453:(e,t,s)=>{s.d(t,{R:()=>a,x:()=>i});var n=s(6540);const r={},o=n.createContext(r);function a(e){const t=n.useContext(o);return n.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}function i(e){let t;return t=e.disableParentContext?"function"==typeof e.components?e.components(r):e.components||r:a(e.components),n.createElement(o.Provider,{value:t},e.children)}}}]);