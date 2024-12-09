"use strict";(self.webpackChunksite=self.webpackChunksite||[]).push([[514],{6277:(e,n,i)=>{i.r(n),i.d(n,{assets:()=>l,contentTitle:()=>r,default:()=>h,frontMatter:()=>t,metadata:()=>d,toc:()=>c});var s=i(4848),o=i(8453);const t={sidebar_custom_props:{icon:"ui"}},r="Look and Feel",d={id:"customize/customize_ui",title:"Look and Feel",description:"You can set custom information in the header (i.e. displaying staging information or company name) by using following",source:"@site/docs/customize/01-customize_ui.md",sourceDirName:"customize",slug:"/customize/customize_ui",permalink:"/docs/customize/customize_ui",draft:!1,unlisted:!1,tags:[],version:"current",sidebarPosition:1,frontMatter:{sidebar_custom_props:{icon:"ui"}},sidebar:"tutorialSidebar",previous:{title:"Customizing",permalink:"/docs/customize/"},next:{title:"Extend the UI",permalink:"/docs/customize/extend_ui"}},l={},c=[{value:"Customizing Colors",id:"customizing-colors",level:2},{value:"Customizing Login Logo",id:"customizing-login-logo",level:2},{value:"Customizing Favicon",id:"customizing-favicon",level:2},{value:"Customizing Available Languages",id:"customizing-available-languages",level:2},{value:"Show or hide views",id:"show-or-hide-views",level:2},{value:"Hide Service URLs",id:"hide-service-urls",level:2}];function a(e){const n={code:"code",h1:"h1",h2:"h2",header:"header",li:"li",ol:"ol",p:"p",pre:"pre",strong:"strong",table:"table",tbody:"tbody",td:"td",th:"th",thead:"thead",tr:"tr",ul:"ul",...(0,o.R)(),...e.components};return(0,s.jsxs)(s.Fragment,{children:[(0,s.jsx)(n.header,{children:(0,s.jsx)(n.h1,{id:"look-and-feel",children:"Look and Feel"})}),"\n",(0,s.jsx)(n.p,{children:"You can set custom information in the header (i.e. displaying staging information or company name) by using following\nconfiguration properties:"}),"\n",(0,s.jsxs)(n.ul,{children:["\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"spring.boot.admin.ui.brand"}),": This HTML snippet is rendered in navigation header and defaults to\n",(0,s.jsx)(n.code,{children:'<img src="assets/img/icon-spring-boot-admin.svg"><span>Spring Boot Admin</span>'}),". By default it shows the SBA logo\nfollowed by it\u2019s name. If you want to show a custom logo you can set:\n",(0,s.jsx)(n.code,{children:'spring.boot.admin.ui.brand=<img src="custom/custom-icon.png">'}),". Either you just add the image to your jar-file in\n",(0,s.jsx)(n.code,{children:"/META-INF/spring-boot-admin-server-ui/"})," (SBA registers a ",(0,s.jsx)(n.code,{children:"ResourceHandler"})," for this location by default), or you must\nensure yourself that the image gets served correctly (e.g. by registering your own ",(0,s.jsx)(n.code,{children:"ResourceHandler"}),")"]}),"\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"spring.boot.admin.ui.title"}),": Use this option to customize the browsers window title."]}),"\n"]}),"\n",(0,s.jsx)(n.h2,{id:"customizing-colors",children:"Customizing Colors"}),"\n",(0,s.jsx)(n.p,{children:"You can provide a custom color theme to the application by overwriting the following properties:"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-yaml",metastring:'title="application.yml"',children:'spring:\n  boot:\n    admin:\n      ui:\n        theme:\n          color: "#4A1420"\n          palette:\n            50: "#F8EBE4"\n            100: "#F2D7CC"\n            200: "#E5AC9C"\n            300: "#D87B6C"\n            400: "#CB463B"\n            500: "#9F2A2A"\n            600: "#83232A"\n            700: "#661B26"\n            800: "#4A1420"\n            900: "#2E0C16"\n'})}),"\n",(0,s.jsxs)(n.table,{children:[(0,s.jsx)(n.thead,{children:(0,s.jsxs)(n.tr,{children:[(0,s.jsx)(n.th,{children:"Property name"}),(0,s.jsx)(n.th,{children:"Default"}),(0,s.jsx)(n.th,{children:"Usage"})]})}),(0,s.jsxs)(n.tbody,{children:[(0,s.jsxs)(n.tr,{children:[(0,s.jsx)(n.td,{children:"spring.boot.admin.ui.theme.color"}),(0,s.jsx)(n.td,{children:"#42d3a5"}),(0,s.jsx)(n.td,{children:"Used in meta tag for setting theme color for user agents. Used to customize the display of the page or of the surrounding user interface."})]}),(0,s.jsxs)(n.tr,{children:[(0,s.jsx)(n.td,{children:"spring.boot.admin.ui.theme.background-enabled"}),(0,s.jsx)(n.td,{children:"true"}),(0,s.jsx)(n.td,{children:"Disable background image in UI"})]}),(0,s.jsxs)(n.tr,{children:[(0,s.jsx)(n.td,{children:"spring.boot.admin.ui.theme.palette.[50,100,200,300,400,500,600,700,800,900]"}),(0,s.jsx)(n.td,{children:"50: #E8FBEF 100: #D0F7DF 200: #A1EFBD 300: #71E69C 400: #41DE7B 500: #22C55E 600: #1A9547 700: #116530 800: #09351A 900: #010603"}),(0,s.jsx)(n.td,{children:"Define a color palette that affects the colors in sidebar view (e.g shade 600 of palette is used as text color and shade 50 as background color.)"})]})]})]}),"\n",(0,s.jsx)(n.h2,{id:"customizing-login-logo",children:"Customizing Login Logo"}),"\n",(0,s.jsx)(n.p,{children:"You can set a custom image to be displayed on the login page."}),"\n",(0,s.jsxs)(n.ol,{children:["\n",(0,s.jsxs)(n.li,{children:["Put the image in a resource location which is served via http (e.g.\n",(0,s.jsx)(n.code,{children:"/META-INF/spring-boot-admin-server-ui/assets/img/"}),")."]}),"\n",(0,s.jsxs)(n.li,{children:["Configure the icons to use using the following property:","\n",(0,s.jsxs)(n.ul,{children:["\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"spring.boot.admin.ui.login-icon"}),": Used as icon on login page. (e.g ",(0,s.jsx)(n.code,{children:"assets/img/custom-login-icon.svg"}),")"]}),"\n"]}),"\n"]}),"\n"]}),"\n",(0,s.jsx)(n.h2,{id:"customizing-favicon",children:"Customizing Favicon"}),"\n",(0,s.jsx)(n.p,{children:"It is possible to use a custom favicon, which is also used for desktop notifications. Spring Boot Admin uses a different\nicon when one or more application is down."}),"\n",(0,s.jsxs)(n.ol,{children:["\n",(0,s.jsxs)(n.li,{children:["Put the favicon (",(0,s.jsx)(n.code,{children:".png"})," with at least 192x192 pixels) in a resource location which is served via http (e.g.\n",(0,s.jsx)(n.code,{children:"/META-INF/spring-boot-admin-server-ui/assets/img/"}),")."]}),"\n",(0,s.jsxs)(n.li,{children:["Configure the icons to use using the following properties:","\n",(0,s.jsxs)(n.ul,{children:["\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.code,{children:"spring.boot.admin.ui.favicon"}),": Used as default icon. (e.g ",(0,s.jsx)(n.code,{children:"assets/img/custom-favicon.png"})]}),"\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.code,{children:"spring.boot.admin.ui.favicon-danger"}),": Used when one or more service is down. (e.g\n",(0,s.jsx)(n.code,{children:"assets/img/custom-favicon-danger.png"}),")"]}),"\n"]}),"\n"]}),"\n"]}),"\n",(0,s.jsx)(n.h2,{id:"customizing-available-languages",children:"Customizing Available Languages"}),"\n",(0,s.jsx)(n.p,{children:"To filter languages to a subset of all supported languages:"}),"\n",(0,s.jsxs)(n.ul,{children:["\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"spring.boot.admin.ui.available-languages"}),": Used as a filter of existing languages. (e.g ",(0,s.jsx)(n.code,{children:"en,de"})," out of existing\n",(0,s.jsx)(n.code,{children:"de,en,fr,ko,pt-BR,ru,zh"}),")"]}),"\n"]}),"\n",(0,s.jsx)(n.h2,{id:"show-or-hide-views",children:"Show or hide views"}),"\n",(0,s.jsx)(n.p,{children:"You can very simply hide views in the navbar:"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-yaml",metastring:'title="application.yml"',children:'spring:\n  boot:\n    admin:\n      ui:\n        view-settings:\n          - name: "journal"\n            enabled: false\n'})}),"\n",(0,s.jsx)(n.h2,{id:"hide-service-urls",children:"Hide Service URLs"}),"\n",(0,s.jsx)(n.p,{children:"To hide service URLs in Spring Boot Admin UI entirely, set the following property in your Server's configuration:"}),"\n",(0,s.jsxs)(n.table,{children:[(0,s.jsx)(n.thead,{children:(0,s.jsxs)(n.tr,{children:[(0,s.jsx)(n.th,{children:"Property name"}),(0,s.jsx)(n.th,{children:"Default"}),(0,s.jsx)(n.th,{children:"Usage"})]})}),(0,s.jsx)(n.tbody,{children:(0,s.jsxs)(n.tr,{children:[(0,s.jsx)(n.td,{children:(0,s.jsx)(n.code,{children:"spring.boot.admin.ui.hide-instance-url"})}),(0,s.jsx)(n.td,{children:(0,s.jsx)(n.code,{children:"false"})}),(0,s.jsxs)(n.td,{children:["Set to ",(0,s.jsx)(n.code,{children:"true"})," to hide service URLs as well as actions that require them in UI (e.g. jump to /health or /actuator)."]})]})})]}),"\n",(0,s.jsxs)(n.p,{children:["If you want to hide the URL for specific instances oncly, you can set the ",(0,s.jsx)(n.code,{children:"hide-url"})," property in the instance metadata\nwhile registering a service.\nWhen using Spring Boot Admin Client you can set the property ",(0,s.jsx)(n.code,{children:"spring.boot.admin.client.metadata.hide-url=true"})," in the\ncorresponding config file.\nThe value set in ",(0,s.jsx)(n.code,{children:"metadata"})," does not have any effect, when the URLs are disabled in Server."]})]})}function h(e={}){const{wrapper:n}={...(0,o.R)(),...e.components};return n?(0,s.jsx)(n,{...e,children:(0,s.jsx)(a,{...e})}):a(e)}},8453:(e,n,i)=>{i.d(n,{R:()=>r,x:()=>d});var s=i(6540);const o={},t=s.createContext(o);function r(e){const n=s.useContext(t);return s.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function d(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(o):e.components||o:r(e.components),s.createElement(t.Provider,{value:n},e.children)}}}]);