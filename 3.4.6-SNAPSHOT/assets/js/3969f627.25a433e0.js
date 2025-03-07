"use strict";(self.webpackChunksite=self.webpackChunksite||[]).push([[841],{9043:(e,n,i)=>{i.r(n),i.d(n,{assets:()=>l,contentTitle:()=>r,default:()=>h,frontMatter:()=>o,metadata:()=>t,toc:()=>c});const t=JSON.parse('{"id":"customize/extend_ui","title":"Extend the UI","description":"Linking / Embedding External Pages in Navbar","source":"@site/docs/customize/02-extend_ui.md","sourceDirName":"customize","slug":"/customize/extend_ui","permalink":"/3.4.6-SNAPSHOT/docs/customize/extend_ui","draft":false,"unlisted":false,"tags":[],"version":"current","sidebarPosition":2,"frontMatter":{"sidebar_custom_props":{"icon":"ui"}},"sidebar":"tutorialSidebar","previous":{"title":"Look and Feel","permalink":"/3.4.6-SNAPSHOT/docs/customize/customize_ui"},"next":{"title":"HTTP Headers","permalink":"/3.4.6-SNAPSHOT/docs/customize/customize_http-headers"}}');var s=i(4848),a=i(8453);const o={sidebar_custom_props:{icon:"ui"}},r="Extend the UI",l={},c=[{value:"Linking / Embedding External Pages in Navbar",id:"linking--embedding-external-pages-in-navbar",level:2},{value:"Simple link",id:"simple-link",level:3},{value:"Dropdown with links",id:"dropdown-with-links",level:3},{value:"Dropdown as link having links as children",id:"dropdown-as-link-having-links-as-children",level:3},{value:"Custom Views",id:"custom-views",level:2},{value:"Override/Set custom group icons",id:"overrideset-custom-group-icons",level:4},{value:"Adding a Top-Level View",id:"adding-a-top-level-view",level:4},{value:"Example",id:"example",level:5},{value:"Visualizing a Custom Endpoint",id:"visualizing-a-custom-endpoint",level:3}];function d(e){const n={a:"a",admonition:"admonition",code:"code",h1:"h1",h2:"h2",h3:"h3",h4:"h4",h5:"h5",header:"header",li:"li",ol:"ol",p:"p",pre:"pre",...(0,a.R)(),...e.components};return(0,s.jsxs)(s.Fragment,{children:[(0,s.jsx)(n.header,{children:(0,s.jsx)(n.h1,{id:"extend-the-ui",children:"Extend the UI"})}),"\n",(0,s.jsx)(n.h2,{id:"linking--embedding-external-pages-in-navbar",children:"Linking / Embedding External Pages in Navbar"}),"\n",(0,s.jsxs)(n.p,{children:["Links will be opened in a new window/tab (i.e. ",(0,s.jsx)(n.code,{children:'target="_blank"'}),") having no access to opener and referrer (",(0,s.jsx)(n.code,{children:'rel="noopener noreferrer"'}),")."]}),"\n",(0,s.jsx)(n.h3,{id:"simple-link",children:"Simple link"}),"\n",(0,s.jsx)(n.p,{children:"To add a simple link to an external page, use the following snippet."}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-yaml",metastring:'title="application.yml"',children:'spring:\n  boot:\n    admin:\n      ui:\n        external-views:\n          - label: "\ud83d\ude80" #(1)\n            url: "https://codecentric.de" #(2)\n            order: 2000 #(3)\n'})}),"\n",(0,s.jsxs)(n.ol,{children:["\n",(0,s.jsx)(n.li,{children:"The label will be shown in the navbar"}),"\n",(0,s.jsx)(n.li,{children:"URL to the page you want to link to"}),"\n",(0,s.jsx)(n.li,{children:"Order that allows to specify position of item in navbar"}),"\n"]}),"\n",(0,s.jsx)(n.h3,{id:"dropdown-with-links",children:"Dropdown with links"}),"\n",(0,s.jsx)(n.p,{children:"To aggregate links below a single element, dropdowns can be configured as follows."}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-yaml",metastring:'title="application.yml"',children:'spring:\n  boot:\n    admin:\n      ui:\n        external-views:\n          - label: Link w/o children\n            children:\n              - label: "\ud83d\udcd6 Docs"\n                url: https://codecentric.github.io/spring-boot-admin/current/\n              - label: "\ud83d\udce6 Maven"\n                url: https://search.maven.org/search?q=g:de.codecentric%20AND%20a:spring-boot-admin-starter-server\n              - label: "\ud83d\udc19 GitHub"\n                url: https://github.com/codecentric/spring-boot-admin\n'})}),"\n",(0,s.jsx)(n.h3,{id:"dropdown-as-link-having-links-as-children",children:"Dropdown as link having links as children"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-yaml",metastring:'title="application.yml"',children:'spring:\n  boot:\n    admin:\n      ui:\n        external-views:\n          - label: Link w children\n            url: https://codecentric.de  #(1)\n            children:\n              - label: "\ud83d\udcd6 Docs"\n                url: https://codecentric.github.io/spring-boot-admin/current/\n              - label: "\ud83d\udce6 Maven"\n                url: https://search.maven.org/search?q=g:de.codecentric%20AND%20a:spring-boot-admin-starter-server\n              - label: "\ud83d\udc19 GitHub"\n                url: https://github.com/codecentric/spring-boot-admin\n          - label: "\ud83c\udf85 Is it christmas"\n            url: https://isitchristmas.com\n            iframe: true\n'})}),"\n",(0,s.jsx)(n.h2,{id:"custom-views",children:"Custom Views"}),"\n",(0,s.jsxs)(n.p,{children:["It is possible to add custom views to the ui. The views must be implemented as ",(0,s.jsx)(n.a,{href:"https://vuejs.org/",children:"Vue.js"})," components."]}),"\n",(0,s.jsxs)(n.p,{children:["The JavaScript-Bundle and CSS-Stylesheet must be placed on the classpath at ",(0,s.jsx)(n.code,{children:"/META-INF/spring-boot-admin-server-ui/extensions/{name}/"})," so the server can pick them up. The ",(0,s.jsx)(n.a,{href:"https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-custom-ui/",children:"spring-boot-admin-sample-custom-ui"})," module contains a sample which has the necessary maven setup to build such a module."]}),"\n",(0,s.jsx)(n.p,{children:"The custom extension registers itself by calling:"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-javascript",metastring:'title="custom-ui.js"',children:'SBA.use({\n  install({ viewRegistry, i18n }) {\n    viewRegistry.addView({\n      name: "custom", //(1)\n      path: "/custom", //(2)\n      component: custom, //(3)\n      group: "custom", //(4)\n      handle, //(5)\n      order: 1000, //(6)\n    });\n    i18n.mergeLocaleMessage("en", {\n      custom: {\n        label: "My Extensions", //(7)\n      },\n    });\n    i18n.mergeLocaleMessage("de", {\n      custom: {\n        label: "Meine Erweiterung",\n      },\n    });\n  },\n});\n'})}),"\n",(0,s.jsxs)(n.ol,{children:["\n",(0,s.jsx)(n.li,{children:"Name of the view and the route."}),"\n",(0,s.jsx)(n.li,{children:"Path in Vue router."}),"\n",(0,s.jsx)(n.li,{children:"The imported custom component, which will be rendered on the route."}),"\n",(0,s.jsx)(n.li,{children:'An optional group name that allows to bind views to a logical group (defaults to "none")'}),"\n",(0,s.jsx)(n.li,{children:"The handle for the custom view to be shown in the top navigation bar."}),"\n",(0,s.jsx)(n.li,{children:"Order for the view."}),"\n",(0,s.jsxs)(n.li,{children:["Using ",(0,s.jsx)(n.code,{children:"i18n.mergeLocaleMessage"})," allows to add custom translations."]}),"\n"]}),"\n",(0,s.jsx)(n.p,{children:"Views in the top navigation bar are sorted by ascending order."}),"\n",(0,s.jsxs)(n.p,{children:["If new top level routes are added to the frontend, they also must be known to the backend. Add a ",(0,s.jsx)(n.code,{children:"/META-INF/spring-boot-admin-server-ui/extensions/{name}/routes.txt"})," with all your new toplevel routes (one route per line)."]}),"\n",(0,s.jsx)(n.p,{children:"Groups are used in instance sidebar to aggregate multiple views into a collapsible menu entry showing the group\u2019s name. When a group contains just a single element, the label of the view is shown instead of the group\u2019s name."}),"\n",(0,s.jsx)(n.h4,{id:"overrideset-custom-group-icons",children:"Override/Set custom group icons"}),"\n",(0,s.jsxs)(n.p,{children:["In order to override or set icons for (custom) groups you can use the ",(0,s.jsx)(n.code,{children:"SBA.viewRegistry.setGroupIcon"})," function as follows:"]}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-javascript",metastring:'title="custom-ui.js"',children:"SBA.viewRegistry.setGroupIcon(\n  \"custom\", //(1)\n  `<svg xmlns='http://www.w3.org/2000/svg'\n        class='h-5  mr-3'\n        viewBox='0 0 576 512'><path d='M512 80c8.8 0 16 7.2 16 16V416c0 8.8-7.2 16-16 16H64c-8.8 0-16-7.2-16-16V96c0-8.8 7.2-16 16-16H512zM64 32C28.7 32 0 60.7 0 96V416c0 35.3 28.7 64 64 64H512c35.3 0 64-28.7 64-64V96c0-35.3-28.7-64-64-64H64zM200 208c14.2 0 27 6.1 35.8 16c8.8 9.9 24 10.7 33.9 1.9s10.7-24 1.9-33.9c-17.5-19.6-43.1-32-71.5-32c-53 0-96 43-96 96s43 96 96 96c28.4 0 54-12.4 71.5-32c8.8-9.9 8-25-1.9-33.9s-25-8-33.9 1.9c-8.8 9.9-21.6 16-35.8 16c-26.5 0-48-21.5-48-48s21.5-48 48-48zm144 48c0-26.5 21.5-48 48-48c14.2 0 27 6.1 35.8 16c8.8 9.9 24 10.7 33.9 1.9s10.7-24 1.9-33.9c-17.5-19.6-43.1-32-71.5-32c-53 0-96 43-96 96s43 96 96 96c28.4 0 54-12.4 71.5-32c8.8-9.9 8-25-1.9-33.9s-25-8-33.9 1.9c-8.8 9.9-21.6 16-35.8 16c-26.5 0-48-21.5-48-48z'/>\n  </svg>` //(2)\n);\n"})}),"\n",(0,s.jsxs)(n.ol,{children:["\n",(0,s.jsx)(n.li,{children:"Name of the group to set icon for"}),"\n",(0,s.jsx)(n.li,{children:"Arbitrary HTML code (e.g. SVG image) that is inserted and parsed as icon."}),"\n"]}),"\n",(0,s.jsx)(n.h4,{id:"adding-a-top-level-view",children:"Adding a Top-Level View"}),"\n",(0,s.jsx)(n.p,{children:"Here is a simple top level view just listing all registered applications:"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-html",metastring:'title="custom-ui.vue"',children:'<template>\n  <div class="m-4">\n    <template v-for="application in applications" :key="application.name">\n      <sba-panel :title="application.name">\n        This application has the following instances:\n\n        <ul>\n          <template v-for="instance in application.instances">\n            <li>\n              <span class="mx-1" v-text="instance.registration.name"></span>\n\n              \x3c!-- SBA components are registered globally and can be used without importing them! --\x3e\n              \x3c!-- They are defined in spring-boot-admin-server-ui --\x3e\n              <sba-status :status="instance.statusInfo.status" class="mx-1" />\n              <sba-tag :value="instance.id" class="mx-1" label="id" />\n            </li>\n          </template>\n        </ul>\n      </sba-panel>\n    </template>\n    <pre v-text="applications"></pre>\n  </div>\n</template>\n\n<script>\n/* global SBA */\nimport "./custom.css";\n\nexport default {\n  setup() {\n    const { applications } = SBA.useApplicationStore(); //(1)\n    return {\n      applications,\n    };\n  },\n  methods: {\n    stringify: JSON.stringify,\n  },\n};\n<\/script>\n'})}),"\n",(0,s.jsxs)(n.ol,{children:["\n",(0,s.jsxs)(n.li,{children:["By destructuring ",(0,s.jsx)(n.code,{children:"applications"})," of ",(0,s.jsx)(n.code,{children:"SBA.useApplicationStore()"}),", you have reactive access to registered applications."]}),"\n"]}),"\n",(0,s.jsx)(n.admonition,{type:"tip",children:(0,s.jsxs)(n.p,{children:["There are some helpful methods on the application and instances object available. Have a look at ",(0,s.jsx)(n.a,{href:"https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-server-ui/src/main/frontend/services/application.ts",children:"application.ts"})," and ",(0,s.jsx)(n.a,{href:"https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-server-ui/src/main/frontend/services/instance.ts",children:"instance.ts"})]})}),"\n",(0,s.jsx)(n.p,{children:"And this is how you register the top-level view."}),"\n",(0,s.jsx)(n.h5,{id:"example",children:"Example"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-javascript",metastring:'title="custom-ui.js"',children:'SBA.viewRegistry.addView({\n  name: "customSub",\n  parent: "custom", // (1)\n  path: "/customSub", // (2)\n  component: customSubitem,\n  label: "Custom Sub",\n  order: 1000,\n});\n'})}),"\n",(0,s.jsxs)(n.ol,{children:["\n",(0,s.jsx)(n.li,{children:"References the name of the parent view."}),"\n",(0,s.jsx)(n.li,{children:"Router path used to navigate to."}),"\n",(0,s.jsxs)(n.li,{children:["Define whether the path should be registered as child route in parent\u2019s route. When set to ",(0,s.jsx)(n.code,{children:"true"}),", the parent component has to implement ",(0,s.jsx)(n.code,{children:"<router-view>"})]}),"\n"]}),"\n",(0,s.jsxs)(n.p,{children:["The ",(0,s.jsx)(n.code,{children:"routes.txt"})," config with the added route:"]}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-text",metastring:'title="routes.txt"',children:"/custom/**\n/customSub/**\n"})}),"\n",(0,s.jsx)(n.h3,{id:"visualizing-a-custom-endpoint",children:"Visualizing a Custom Endpoint"}),"\n",(0,s.jsx)(n.p,{children:"Here is a view to show a custom endpoint:"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-html",metastring:'title="custom-ui.vue"',children:'<template>\n  <div class="custom">\n    <p>Instance: <span v-text="instance.id" /></p>\n    <p>Output: <span v-html="text" /></p>\n  </div>\n</template>\n\n<script>\nexport default {\n  props: {\n    instance: {\n      //(1)\n      type: Object,\n      required: true,\n    },\n  },\n  data: () => ({\n    text: "",\n  }),\n  async created() {\n    console.log(this.instance);\n    const response = await this.instance.axios.get("actuator/custom"); //(2)\n    this.text = response.data;\n  },\n};\n<\/script>\n\n<style lang="css" scoped>\n.custom {\n  font-size: 20px;\n  width: 80%;\n}\n</style>\n'})}),"\n",(0,s.jsxs)(n.ol,{children:["\n",(0,s.jsxs)(n.li,{children:["If you define a ",(0,s.jsx)(n.code,{children:"instance"})," prop the component will receive the instance the view should be rendered for."]}),"\n",(0,s.jsxs)(n.li,{children:["Each instance has a preconfigured ",(0,s.jsx)(n.a,{href:"https://github.com/axios/axios",children:"axios"})," instance to access the endpoints with the correct path and headers."]}),"\n"]}),"\n",(0,s.jsx)(n.p,{children:"Registering the instance view works like for the top-level view with some additional properties:"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-javascript",metastring:'title="custom-ui.js"',children:'SBA.viewRegistry.addView({\n  name: "instances/custom",\n  parent: "instances", // (1)\n  path: "custom",\n  component: customEndpoint,\n  label: "Custom",\n  group: "custom", // (2)\n  order: 1000,\n  isEnabled: ({ instance }) => {\n    return instance.hasEndpoint("custom");\n  }, // (3)\n});\n'})}),"\n",(0,s.jsxs)(n.ol,{children:["\n",(0,s.jsx)(n.li,{children:"The parent must be 'instances' in order to render the new custom view for a single instance."}),"\n",(0,s.jsx)(n.li,{children:"You can group views by assigning them to a group."}),"\n",(0,s.jsxs)(n.li,{children:["If you add a ",(0,s.jsx)(n.code,{children:"isEnabled"})," callback you can figure out dynamically if the view should be show for the particular instance."]}),"\n"]}),"\n",(0,s.jsx)(n.admonition,{type:"note",children:(0,s.jsx)(n.p,{children:"You can override default views by putting the same group and name as the one you want to override."})})]})}function h(e={}){const{wrapper:n}={...(0,a.R)(),...e.components};return n?(0,s.jsx)(n,{...e,children:(0,s.jsx)(d,{...e})}):d(e)}},8453:(e,n,i)=>{i.d(n,{R:()=>o,x:()=>r});var t=i(6540);const s={},a=t.createContext(s);function o(e){const n=t.useContext(a);return t.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function r(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(s):e.components||s:o(e.components),t.createElement(a.Provider,{value:n},e.children)}}}]);