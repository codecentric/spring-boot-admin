"use strict";(self.webpackChunksite=self.webpackChunksite||[]).push([[208],{1203:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>a,contentTitle:()=>l,default:()=>u,frontMatter:()=>c,metadata:()=>i,toc:()=>d});const i=JSON.parse('{"id":"client/index","title":"Spring Boot Admin Client","description":"The Spring Boot Admin Client is a Spring Boot application that registers itself with the Spring Boot Admin Server to","source":"@site/docs/client/index.md","sourceDirName":"client","slug":"/client/","permalink":"/3.4.2/docs/client/","draft":false,"unlisted":false,"tags":[],"version":"current","frontMatter":{},"sidebar":"tutorialSidebar","previous":{"title":"Properties","permalink":"/3.4.2/docs/server/server-properties"},"next":{"title":"Client features","permalink":"/3.4.2/docs/client/client-features"}}');var r=n(4848),o=n(8453),s=n(8810);const c={},l="Spring Boot Admin Client",a={},d=[];function h(e){const t={h1:"h1",header:"header",li:"li",p:"p",strong:"strong",ul:"ul",...(0,o.R)(),...e.components};return(0,r.jsxs)(r.Fragment,{children:[(0,r.jsx)(t.header,{children:(0,r.jsx)(t.h1,{id:"spring-boot-admin-client",children:"Spring Boot Admin Client"})}),"\n",(0,r.jsx)(t.p,{children:"The Spring Boot Admin Client is a Spring Boot application that registers itself with the Spring Boot Admin Server to\nenable monitoring and management. By including the Spring Boot Admin Client Starter dependency in your application, the\nSpring Boot Admin Server can automatically access health, metrics, and other management endpoints, depending on which\nActuator endpoints are accessible."}),"\n",(0,r.jsx)(t.p,{children:(0,r.jsx)(t.strong,{children:"Key Features:"})}),"\n",(0,r.jsxs)(t.ul,{children:["\n",(0,r.jsxs)(t.li,{children:[(0,r.jsx)(t.strong,{children:"Automatic Registration:"})," The client can self-register with the Admin Server by sending regular status updates."]}),"\n",(0,r.jsxs)(t.li,{children:[(0,r.jsx)(t.strong,{children:"Health and Metrics Exposure:"})," The client leverages Spring Boot Actuator to expose endpoints for monitoring health\nstatus, system metrics, application logs, and other runtime data."]}),"\n",(0,r.jsxs)(t.li,{children:[(0,r.jsx)(t.strong,{children:"Management Actions:"})," The Admin Server can interact with the client for actions such as restarting the application,\nclearing caches, or triggering log file downloads."]}),"\n",(0,r.jsxs)(t.li,{children:[(0,r.jsx)(t.strong,{children:"Secure Communication:"})," The client supports configuring authentication and SSL to ensure secure communication\nbetween the client and the Admin Server."]}),"\n"]}),"\n",(0,r.jsx)(t.p,{children:"By adding the Spring Boot Admin Client to your applications, they become discoverable by the Admin Server, enabling\ncentralized monitoring, alerting, and management. This makes it easy to monitor the health of your entire system in\nreal-time."}),"\n",(0,r.jsx)(s.A,{})]})}function u(e={}){const{wrapper:t}={...(0,o.R)(),...e.components};return t?(0,r.jsx)(t,{...e,children:(0,r.jsx)(h,{...e})}):h(e)}},8810:(e,t,n)=>{n.d(t,{A:()=>B});var i=n(6540),r=n(4164),o=n(102),s=n(6289),c=n(797);const l=["zero","one","two","few","many","other"];function a(e){return l.filter((t=>e.includes(t)))}const d={locale:"en",pluralForms:a(["one","other"]),select:e=>1===e?"one":"other"};function h(){const{i18n:{currentLocale:e}}=(0,c.A)();return(0,i.useMemo)((()=>{try{return function(e){const t=new Intl.PluralRules(e);return{locale:e,pluralForms:a(t.resolvedOptions().pluralCategories),select:e=>t.select(e)}}(e)}catch(t){return console.error(`Failed to use Intl.PluralRules for locale "${e}".\nDocusaurus will fallback to the default (English) implementation.\nError: ${t.message}\n`),d}}),[e])}function u(){const e=h();return{selectMessage:(t,n)=>function(e,t,n){const i=e.split("|");if(1===i.length)return i[0];i.length>n.pluralForms.length&&console.error(`For locale=${n.locale}, a maximum of ${n.pluralForms.length} plural forms are expected (${n.pluralForms.join(",")}), but the message contains ${i.length}: ${e}`);const r=n.select(t),o=n.pluralForms.indexOf(r);return i[Math.min(o,i.length-1)]}(n,t,e)}}var m=n(2887),p=n(539),g=n(9303);const f={cardContainer:"cardContainer_S8oU",cardTitle:"cardTitle_HoSo",cardDescription:"cardDescription_c27F"};var x=n(7399),j=n(4848);const S={ui:(0,j.jsx)(x.In,{icon:"gg:ui-kit",height:"24"}),http:(0,j.jsx)(x.In,{icon:"mdi:web",height:"24"}),properties:(0,j.jsx)(x.In,{icon:"ion:options-outline",height:"24"}),server:(0,j.jsx)(x.In,{icon:"mdi:server-outline",height:"24"}),notifications:(0,j.jsx)(x.In,{icon:"carbon:notification",height:"24"}),python:(0,j.jsx)(x.In,{icon:"ion:logo-python",height:"24"}),features:(0,j.jsx)(x.In,{icon:"ri:function-add-line",height:"24"})};function A(e){let{href:t,children:n}=e;return(0,j.jsx)(s.A,{href:t,className:(0,r.A)("card padding--lg",f.cardContainer),children:n})}function y(e){let{href:t,icon:n,title:i,description:o}=e;return(0,j.jsxs)(A,{href:t,children:[(0,j.jsxs)(g.A,{as:"h2",className:(0,r.A)("text--truncate",f.cardTitle),title:i,children:[n," ",i]}),o&&(0,j.jsx)("p",{className:(0,r.A)("text--truncate",f.cardDescription),title:o,children:o})]})}function b(e){let{item:t}=e;const n=(0,o.Nr)(t),i=function(){const{selectMessage:e}=u();return t=>e(t,(0,p.T)({message:"1 item|{count} items",id:"theme.docs.DocCard.categoryDescription.plurals",description:"The default description for a category card in the generated index about how many items this category includes"},{count:t}))}();return n?(0,j.jsx)(y,{href:n,icon:"\ud83d\uddc3\ufe0f",title:t.label,description:t.description??i(t.items.length)}):null}function v(e){let{item:t}=e;const n=(0,o.cC)(t.docId??void 0);return(0,j.jsx)(y,{href:t.href,icon:S[t?.customProps?.icon]??((0,m.A)(t.href)?"\ud83d\udcc4\ufe0f":"\ud83d\udd17"),title:t.label,description:t.description??n?.description})}function w(e){let{item:t}=e;switch(t.type){case"link":return(0,j.jsx)(v,{item:t});case"category":return(0,j.jsx)(b,{item:t});default:throw new Error(`unknown item type ${JSON.stringify(t)}`)}}function C(e){let{className:t}=e;const n=(0,o.$S)();return(0,j.jsx)(B,{items:n.items,className:t})}function B(e){const{items:t,className:n}=e;if(!t)return(0,j.jsx)(C,{...e});const i=(0,o.d1)(t);return(0,j.jsx)("section",{className:(0,r.A)("row",n),children:i.map(((e,t)=>(0,j.jsx)("article",{className:"col col--6 margin-bottom--lg",children:(0,j.jsx)(w,{item:e})},t)))})}}}]);