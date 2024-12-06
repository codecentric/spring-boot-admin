"use strict";(self.webpackChunksite=self.webpackChunksite||[]).push([[399],{9348:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>l,contentTitle:()=>a,default:()=>h,frontMatter:()=>c,metadata:()=>i,toc:()=>u});const i=JSON.parse('{"id":"customize/index","title":"Customizing","description":"Spring Boot Admin provides flexibility for customizing the appearance and branding of the Admin Server\u2019s user interface. This allows you to tailor the UI to match your organization\'s branding, improve usability, or add custom functionality.","source":"@site/docs/customize/index.md","sourceDirName":"customize","slug":"/customize/","permalink":"/docs/customize/","draft":false,"unlisted":false,"tags":[],"version":"current","frontMatter":{},"sidebar":"tutorialSidebar","previous":{"title":"Properties","permalink":"/docs/client/client-properties"},"next":{"title":"Look and Feel","permalink":"/docs/customize/customize_ui"}}');var o=n(4848),r=n(8453),s=n(6151);const c={},a="Customizing",l={},u=[];function d(e){const t={h1:"h1",header:"header",li:"li",p:"p",strong:"strong",ul:"ul",...(0,r.R)(),...e.components};return(0,o.jsxs)(o.Fragment,{children:[(0,o.jsx)(t.header,{children:(0,o.jsx)(t.h1,{id:"customizing",children:"Customizing"})}),"\n",(0,o.jsx)(t.p,{children:"Spring Boot Admin provides flexibility for customizing the appearance and branding of the Admin Server\u2019s user interface. This allows you to tailor the UI to match your organization's branding, improve usability, or add custom functionality."}),"\n",(0,o.jsx)(t.p,{children:"You can easily modify key elements like logos, themes, and even extend the UI with custom views or components. The customization options include:"}),"\n",(0,o.jsxs)(t.ul,{children:["\n",(0,o.jsxs)(t.li,{children:[(0,o.jsx)(t.strong,{children:"Custom Logos and Branding"}),": Replace the default Spring Boot Admin logo with your own branding assets."]}),"\n",(0,o.jsxs)(t.li,{children:[(0,o.jsx)(t.strong,{children:"Theming"}),": Customize colors, fonts, and layout styles using custom CSS or by modifying the theme properties."]}),"\n",(0,o.jsxs)(t.li,{children:[(0,o.jsx)(t.strong,{children:"Custom Views"}),": Add new pages or extend existing ones by integrating custom JavaScript and HTML components."]}),"\n",(0,o.jsxs)(t.li,{children:[(0,o.jsx)(t.strong,{children:"Localization"}),": Support multiple languages by adding custom translations for the UI."]}),"\n"]}),"\n",(0,o.jsx)(t.p,{children:"By leveraging these customization options, you can create a tailored experience that aligns with your project or organization's needs, while still retaining the powerful monitoring and management capabilities of Spring Boot Admin."}),"\n",(0,o.jsx)(s.A,{})]})}function h(e={}){const{wrapper:t}={...(0,r.R)(),...e.components};return t?(0,o.jsx)(t,{...e,children:(0,o.jsx)(d,{...e})}):d(e)}},6151:(e,t,n)=>{n.d(t,{A:()=>C});var i=n(6540),o=n(4164),r=n(4718),s=n(8774),c=n(4586);const a=["zero","one","two","few","many","other"];function l(e){return a.filter((t=>e.includes(t)))}const u={locale:"en",pluralForms:l(["one","other"]),select:e=>1===e?"one":"other"};function d(){const{i18n:{currentLocale:e}}=(0,c.A)();return(0,i.useMemo)((()=>{try{return function(e){const t=new Intl.PluralRules(e);return{locale:e,pluralForms:l(t.resolvedOptions().pluralCategories),select:e=>t.select(e)}}(e)}catch(t){return console.error(`Failed to use Intl.PluralRules for locale "${e}".\nDocusaurus will fallback to the default (English) implementation.\nError: ${t.message}\n`),u}}),[e])}function h(){const e=d();return{selectMessage:(t,n)=>function(e,t,n){const i=e.split("|");if(1===i.length)return i[0];i.length>n.pluralForms.length&&console.error(`For locale=${n.locale}, a maximum of ${n.pluralForms.length} plural forms are expected (${n.pluralForms.join(",")}), but the message contains ${i.length}: ${e}`);const o=n.select(t),r=n.pluralForms.indexOf(o);return i[Math.min(r,i.length-1)]}(n,t,e)}}var m=n(6654),p=n(1312),g=n(1107);const f={cardContainer:"cardContainer_S8oU",cardTitle:"cardTitle_HoSo",cardDescription:"cardDescription_c27F"};var x=n(7399),j=n(4848);const y={ui:(0,j.jsx)(x.In,{icon:"gg:ui-kit",height:"24"}),http:(0,j.jsx)(x.In,{icon:"mdi:web",height:"24"}),properties:(0,j.jsx)(x.In,{icon:"ion:options-outline",height:"24"}),server:(0,j.jsx)(x.In,{icon:"mdi:server-outline",height:"24"}),notifications:(0,j.jsx)(x.In,{icon:"carbon:notification",height:"24"}),python:(0,j.jsx)(x.In,{icon:"ion:logo-python",height:"24"}),features:(0,j.jsx)(x.In,{icon:"ri:function-add-line",height:"24"})};function b(e){let{href:t,children:n}=e;return(0,j.jsx)(s.A,{href:t,className:(0,o.A)("card padding--lg",f.cardContainer),children:n})}function w(e){let{href:t,icon:n,title:i,description:r}=e;return(0,j.jsxs)(b,{href:t,children:[(0,j.jsxs)(g.A,{as:"h2",className:(0,o.A)("text--truncate",f.cardTitle),title:i,children:[n," ",i]}),r&&(0,j.jsx)("p",{className:(0,o.A)("text--truncate",f.cardDescription),title:r,children:r})]})}function z(e){let{item:t}=e;const n=(0,r.Nr)(t),i=function(){const{selectMessage:e}=h();return t=>e(t,(0,p.T)({message:"1 item|{count} items",id:"theme.docs.DocCard.categoryDescription.plurals",description:"The default description for a category card in the generated index about how many items this category includes"},{count:t}))}();return n?(0,j.jsx)(w,{href:n,icon:"\ud83d\uddc3\ufe0f",title:t.label,description:t.description??i(t.items.length)}):null}function v(e){let{item:t}=e;const n=(0,r.cC)(t.docId??void 0);return(0,j.jsx)(w,{href:t.href,icon:y[t?.customProps?.icon]??((0,m.A)(t.href)?"\ud83d\udcc4\ufe0f":"\ud83d\udd17"),title:t.label,description:t.description??n?.description})}function A(e){let{item:t}=e;switch(t.type){case"link":return(0,j.jsx)(v,{item:t});case"category":return(0,j.jsx)(z,{item:t});default:throw new Error(`unknown item type ${JSON.stringify(t)}`)}}function S(e){let{className:t}=e;const n=(0,r.$S)();return(0,j.jsx)(C,{items:n.items,className:t})}function C(e){const{items:t,className:n}=e;if(!t)return(0,j.jsx)(S,{...e});const i=(0,r.d1)(t);return(0,j.jsx)("section",{className:(0,o.A)("row",n),children:i.map(((e,t)=>(0,j.jsx)("article",{className:"col col--6 margin-bottom--lg",children:(0,j.jsx)(A,{item:e})},t)))})}}}]);