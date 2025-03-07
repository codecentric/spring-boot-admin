"use strict";(self.webpackChunksite=self.webpackChunksite||[]).push([[967],{6855:(e,t,r)=>{r.r(t),r.d(t,{assets:()=>l,contentTitle:()=>a,default:()=>h,frontMatter:()=>c,metadata:()=>n,toc:()=>u});const n=JSON.parse('{"id":"third-party/index","title":"Third Party Integrations","description":"","source":"@site/docs/third-party/index.md","sourceDirName":"third-party","slug":"/third-party/","permalink":"/3.4.6-SNAPSHOT/docs/third-party/","draft":false,"unlisted":false,"tags":[],"version":"current","frontMatter":{},"sidebar":"tutorialSidebar","previous":{"title":"HTTP Interceptors","permalink":"/3.4.6-SNAPSHOT/docs/customize/customize_interceptors"},"next":{"title":"Pyctuator","permalink":"/3.4.6-SNAPSHOT/docs/third-party/pyctuator"}}');var i=r(4848),s=r(8453),o=r(8810);const c={},a="Third Party Integrations",l={},u=[];function d(e){const t={h1:"h1",header:"header",...(0,s.R)(),...e.components};return(0,i.jsxs)(i.Fragment,{children:[(0,i.jsx)(t.header,{children:(0,i.jsx)(t.h1,{id:"third-party-integrations",children:"Third Party Integrations"})}),"\n",(0,i.jsx)(o.A,{})]})}function h(e={}){const{wrapper:t}={...(0,s.R)(),...e.components};return t?(0,i.jsx)(t,{...e,children:(0,i.jsx)(d,{...e})}):d(e)}},8810:(e,t,r)=>{r.d(t,{A:()=>S});var n=r(6540),i=r(4164),s=r(102),o=r(6289),c=r(797);const a=["zero","one","two","few","many","other"];function l(e){return a.filter((t=>e.includes(t)))}const u={locale:"en",pluralForms:l(["one","other"]),select:e=>1===e?"one":"other"};function d(){const{i18n:{currentLocale:e}}=(0,c.A)();return(0,n.useMemo)((()=>{try{return function(e){const t=new Intl.PluralRules(e);return{locale:e,pluralForms:l(t.resolvedOptions().pluralCategories),select:e=>t.select(e)}}(e)}catch(t){return console.error(`Failed to use Intl.PluralRules for locale "${e}".\nDocusaurus will fallback to the default (English) implementation.\nError: ${t.message}\n`),u}}),[e])}function h(){const e=d();return{selectMessage:(t,r)=>function(e,t,r){const n=e.split("|");if(1===n.length)return n[0];n.length>r.pluralForms.length&&console.error(`For locale=${r.locale}, a maximum of ${r.pluralForms.length} plural forms are expected (${r.pluralForms.join(",")}), but the message contains ${n.length}: ${e}`);const i=r.select(t),s=r.pluralForms.indexOf(i);return n[Math.min(s,n.length-1)]}(r,t,e)}}var p=r(2887),m=r(539),f=r(9303);const g={cardContainer:"cardContainer_S8oU",cardTitle:"cardTitle_HoSo",cardDescription:"cardDescription_c27F"};var x=r(7399),j=r(4848);const y={ui:(0,j.jsx)(x.In,{icon:"gg:ui-kit",height:"24"}),http:(0,j.jsx)(x.In,{icon:"mdi:web",height:"24"}),properties:(0,j.jsx)(x.In,{icon:"ion:options-outline",height:"24"}),server:(0,j.jsx)(x.In,{icon:"mdi:server-outline",height:"24"}),notifications:(0,j.jsx)(x.In,{icon:"carbon:notification",height:"24"}),python:(0,j.jsx)(x.In,{icon:"ion:logo-python",height:"24"}),features:(0,j.jsx)(x.In,{icon:"ri:function-add-line",height:"24"})};function N(e){let{href:t,children:r}=e;return(0,j.jsx)(o.A,{href:t,className:(0,i.A)("card padding--lg",g.cardContainer),children:r})}function w(e){let{href:t,icon:r,title:n,description:s}=e;return(0,j.jsxs)(N,{href:t,children:[(0,j.jsxs)(f.A,{as:"h2",className:(0,i.A)("text--truncate",g.cardTitle),title:n,children:[r," ",n]}),s&&(0,j.jsx)("p",{className:(0,i.A)("text--truncate",g.cardDescription),title:s,children:s})]})}function I(e){let{item:t}=e;const r=(0,s.Nr)(t),n=function(){const{selectMessage:e}=h();return t=>e(t,(0,m.T)({message:"1 item|{count} items",id:"theme.docs.DocCard.categoryDescription.plurals",description:"The default description for a category card in the generated index about how many items this category includes"},{count:t}))}();return r?(0,j.jsx)(w,{href:r,icon:"\ud83d\uddc3\ufe0f",title:t.label,description:t.description??n(t.items.length)}):null}function T(e){let{item:t}=e;const r=(0,s.cC)(t.docId??void 0);return(0,j.jsx)(w,{href:t.href,icon:y[t?.customProps?.icon]??((0,p.A)(t.href)?"\ud83d\udcc4\ufe0f":"\ud83d\udd17"),title:t.label,description:t.description??r?.description})}function A(e){let{item:t}=e;switch(t.type){case"link":return(0,j.jsx)(T,{item:t});case"category":return(0,j.jsx)(I,{item:t});default:throw new Error(`unknown item type ${JSON.stringify(t)}`)}}function b(e){let{className:t}=e;const r=(0,s.$S)();return(0,j.jsx)(S,{items:r.items,className:t})}function S(e){const{items:t,className:r}=e;if(!t)return(0,j.jsx)(b,{...e});const n=(0,s.d1)(t);return(0,j.jsx)("section",{className:(0,i.A)("row",r),children:n.map(((e,t)=>(0,j.jsx)("article",{className:"col col--6 margin-bottom--lg",children:(0,j.jsx)(A,{item:e})},t)))})}}}]);