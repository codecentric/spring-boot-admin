"use strict";(self.webpackChunksite=self.webpackChunksite||[]).push([[514],{7552:(e,i,n)=>{n.r(i),n.d(i,{assets:()=>l,contentTitle:()=>a,default:()=>u,frontMatter:()=>r,metadata:()=>s,toc:()=>c});const s=JSON.parse('{"id":"customize/customize_ui","title":"Look and Feel","description":"You can set custom information in the header (i.e. displaying staging information or company name) by using following configuration properties:","source":"@site/docs/customize/01-customize_ui.md","sourceDirName":"customize","slug":"/customize/customize_ui","permalink":"/3.4.2-RC1/docs/customize/customize_ui","draft":false,"unlisted":false,"tags":[],"version":"current","sidebarPosition":1,"frontMatter":{"sidebar_custom_props":{"icon":"ui"}},"sidebar":"tutorialSidebar","previous":{"title":"Customizing","permalink":"/3.4.2-RC1/docs/customize/"},"next":{"title":"Extend the UI","permalink":"/3.4.2-RC1/docs/customize/extend_ui"}}');var o=n(4848),t=n(8453);const r={sidebar_custom_props:{icon:"ui"}},a="Look and Feel",l={},c=[{value:"Customizing Colors",id:"customizing-colors",level:2},{value:"Customizing Login Logo",id:"customizing-login-logo",level:2},{value:"Customizing Favicon",id:"customizing-favicon",level:2},{value:"Customizing Available Languages",id:"customizing-available-languages",level:2},{value:"Show or hide views",id:"show-or-hide-views",level:2}];function d(e){const i={code:"code",h1:"h1",h2:"h2",header:"header",li:"li",ol:"ol",p:"p",pre:"pre",strong:"strong",table:"table",tbody:"tbody",td:"td",th:"th",thead:"thead",tr:"tr",ul:"ul",...(0,t.R)(),...e.components};return(0,o.jsxs)(o.Fragment,{children:[(0,o.jsx)(i.header,{children:(0,o.jsx)(i.h1,{id:"look-and-feel",children:"Look and Feel"})}),"\n",(0,o.jsx)(i.p,{children:"You can set custom information in the header (i.e. displaying staging information or company name) by using following configuration properties:"}),"\n",(0,o.jsxs)(i.ul,{children:["\n",(0,o.jsxs)(i.li,{children:[(0,o.jsx)(i.strong,{children:"spring.boot.admin.ui.brand"}),": This HTML snippet is rendered in navigation header and defaults to ",(0,o.jsx)(i.code,{children:'<img src="assets/img/icon-spring-boot-admin.svg"><span>Spring Boot Admin</span>'}),". By default it shows the SBA logo followed by it\u2019s name. If you want to show a custom logo you can set: ",(0,o.jsx)(i.code,{children:'spring.boot.admin.ui.brand=<img src="custom/custom-icon.png">'}),". Either you just add the image to your jar-file in ",(0,o.jsx)(i.code,{children:"/META-INF/spring-boot-admin-server-ui/"})," (SBA registers a ",(0,o.jsx)(i.code,{children:"ResourceHandler"})," for this location by default), or you must ensure yourself that the image gets served correctly (e.g. by registering your own ",(0,o.jsx)(i.code,{children:"ResourceHandler"}),")"]}),"\n",(0,o.jsxs)(i.li,{children:[(0,o.jsx)(i.strong,{children:"spring.boot.admin.ui.title"}),": Use this option to customize the browsers window title."]}),"\n"]}),"\n",(0,o.jsx)(i.h2,{id:"customizing-colors",children:"Customizing Colors"}),"\n",(0,o.jsx)(i.p,{children:"You can provide a custom color theme to the application by overwriting the following properties:"}),"\n",(0,o.jsx)(i.pre,{children:(0,o.jsx)(i.code,{className:"language-yaml",metastring:'title="application.yml"',children:'spring:\n  boot:\n    admin:\n      ui:\n        theme:\n          color: "#4A1420"\n          palette:\n            50: "#F8EBE4"\n            100: "#F2D7CC"\n            200: "#E5AC9C"\n            300: "#D87B6C"\n            400: "#CB463B"\n            500: "#9F2A2A"\n            600: "#83232A"\n            700: "#661B26"\n            800: "#4A1420"\n            900: "#2E0C16"\n'})}),"\n",(0,o.jsxs)(i.table,{children:[(0,o.jsx)(i.thead,{children:(0,o.jsxs)(i.tr,{children:[(0,o.jsx)(i.th,{children:"Property name"}),(0,o.jsx)(i.th,{children:"Default"}),(0,o.jsx)(i.th,{children:"Usage"})]})}),(0,o.jsxs)(i.tbody,{children:[(0,o.jsxs)(i.tr,{children:[(0,o.jsx)(i.td,{children:"spring.boot.admin.ui.theme.color"}),(0,o.jsx)(i.td,{children:"#42d3a5"}),(0,o.jsx)(i.td,{children:"Used in meta tag for setting theme color for user agents. Used to customize the display of the page or of the surrounding user interface."})]}),(0,o.jsxs)(i.tr,{children:[(0,o.jsx)(i.td,{children:"spring.boot.admin.ui.theme.background-enabled"}),(0,o.jsx)(i.td,{children:"true"}),(0,o.jsx)(i.td,{children:"Disable background image in UI"})]}),(0,o.jsxs)(i.tr,{children:[(0,o.jsx)(i.td,{children:"spring.boot.admin.ui.theme.palette.[50,100,200,300,400,500,600,700,800,900]"}),(0,o.jsx)(i.td,{children:"50: #E8FBEF 100: #D0F7DF 200: #A1EFBD 300: #71E69C 400: #41DE7B 500: #22C55E 600: #1A9547 700: #116530 800: #09351A 900: #010603"}),(0,o.jsx)(i.td,{children:"Define a color palette that affects the colors in sidebar view (e.g shade 600 of palette is used as text color and shade 50 as background color.)"})]})]})]}),"\n",(0,o.jsx)(i.h2,{id:"customizing-login-logo",children:"Customizing Login Logo"}),"\n",(0,o.jsx)(i.p,{children:"You can set a custom image to be displayed on the login page."}),"\n",(0,o.jsxs)(i.ol,{children:["\n",(0,o.jsxs)(i.li,{children:["Put the image in a resource location which is served via http (e.g. ",(0,o.jsx)(i.code,{children:"/META-INF/spring-boot-admin-server-ui/assets/img/"}),")."]}),"\n",(0,o.jsxs)(i.li,{children:["Configure the icons to use using the following property:","\n",(0,o.jsxs)(i.ul,{children:["\n",(0,o.jsxs)(i.li,{children:[(0,o.jsx)(i.strong,{children:"spring.boot.admin.ui.login-icon"}),": Used as icon on login page. (e.g ",(0,o.jsx)(i.code,{children:"assets/img/custom-login-icon.svg"}),")"]}),"\n"]}),"\n"]}),"\n"]}),"\n",(0,o.jsx)(i.h2,{id:"customizing-favicon",children:"Customizing Favicon"}),"\n",(0,o.jsx)(i.p,{children:"It is possible to use a custom favicon, which is also used for desktop notifications. Spring Boot Admin uses a different icon when one or more application is down."}),"\n",(0,o.jsxs)(i.ol,{children:["\n",(0,o.jsxs)(i.li,{children:["Put the favicon (",(0,o.jsx)(i.code,{children:".png"})," with at least 192x192 pixels) in a resource location which is served via http (e.g. ",(0,o.jsx)(i.code,{children:"/META-INF/spring-boot-admin-server-ui/assets/img/"}),")."]}),"\n",(0,o.jsxs)(i.li,{children:["Configure the icons to use using the following properties:","\n",(0,o.jsxs)(i.ul,{children:["\n",(0,o.jsxs)(i.li,{children:[(0,o.jsx)(i.code,{children:"spring.boot.admin.ui.favicon"}),": Used as default icon. (e.g ",(0,o.jsx)(i.code,{children:"assets/img/custom-favicon.png"})]}),"\n",(0,o.jsxs)(i.li,{children:[(0,o.jsx)(i.code,{children:"spring.boot.admin.ui.favicon-danger"}),": Used when one or more service is down. (e.g ",(0,o.jsx)(i.code,{children:"assets/img/custom-favicon-danger.png"}),")"]}),"\n"]}),"\n"]}),"\n"]}),"\n",(0,o.jsx)(i.h2,{id:"customizing-available-languages",children:"Customizing Available Languages"}),"\n",(0,o.jsx)(i.p,{children:"To filter languages to a subset of all supported languages:"}),"\n",(0,o.jsxs)(i.ul,{children:["\n",(0,o.jsxs)(i.li,{children:[(0,o.jsx)(i.strong,{children:"spring.boot.admin.ui.available-languages"}),": Used as a filter of existing languages. (e.g ",(0,o.jsx)(i.code,{children:"en,de"})," out of existing ",(0,o.jsx)(i.code,{children:"de,en,fr,ko,pt-BR,ru,zh"}),")"]}),"\n"]}),"\n",(0,o.jsx)(i.h2,{id:"show-or-hide-views",children:"Show or hide views"}),"\n",(0,o.jsx)(i.p,{children:"You can very simply hide views in the navbar:"}),"\n",(0,o.jsx)(i.pre,{children:(0,o.jsx)(i.code,{className:"language-yaml",metastring:'title="application.yml"',children:'spring:\n  boot:\n    admin:\n      ui:\n        view-settings:\n          - name: "journal"\n            enabled: false\n'})})]})}function u(e={}){const{wrapper:i}={...(0,t.R)(),...e.components};return i?(0,o.jsx)(i,{...e,children:(0,o.jsx)(d,{...e})}):d(e)}},8453:(e,i,n)=>{n.d(i,{R:()=>r,x:()=>a});var s=n(6540);const o={},t=s.createContext(o);function r(e){const i=s.useContext(t);return s.useMemo((function(){return"function"==typeof e?e(i):{...i,...e}}),[i,e])}function a(e){let i;return i=e.disableParentContext?"function"==typeof e.components?e.components(o):e.components||o:r(e.components),s.createElement(t.Provider,{value:i},e.children)}}}]);