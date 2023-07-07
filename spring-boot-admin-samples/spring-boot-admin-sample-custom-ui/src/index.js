/*
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* global SBA */
import customEndpoint from "./custom-endpoint.vue";
import customSubitem from "./custom-subitem.vue";
import custom from "./custom.vue";
import handle from "./handle.vue";

// tag::customization-ui-toplevel[]
SBA.use({
  install({ viewRegistry, i18n }) {
    viewRegistry.addView({
      name: "custom", //<1>
      path: "/custom", //<2>
      component: custom, //<3>
      group: "custom", //<4>
      handle, //<5>
      order: 1000, //<6>
    });
    i18n.mergeLocaleMessage("en", {
      custom: {
        label: "My Extensions", //<7>
      },
    });
    i18n.mergeLocaleMessage("de", {
      custom: {
        label: "Meine Erweiterung",
      },
    });
  },
});
// end::customization-ui-toplevel[]

// tag::customization-ui-child[]
SBA.viewRegistry.addView({
  name: "customSub",
  parent: "custom", // <1>
  path: "/customSub", // <2>
  component: customSubitem,
  label: "Custom Sub",
  order: 1000,
});
// end::customization-ui-child[]

SBA.viewRegistry.addView({
  name: "customSubUser",
  parent: "user", // <1>
  path: "/customSub", // <2>
  component: customSubitem,
  label: "Custom Sub In Usermenu",
  order: 1000,
});

// tag::customization-ui-endpoint[]
SBA.viewRegistry.addView({
  name: "instances/custom",
  parent: "instances", // <1>
  path: "custom",
  component: customEndpoint,
  label: "Custom",
  group: "custom", // <2>
  order: 1000,
  isEnabled: ({ instance }) => {
    return instance.hasEndpoint("custom");
  }, // <3>
});
// end::customization-ui-endpoint[]

// tag::customization-ui-groups[]
SBA.viewRegistry.setGroupIcon(
  "custom", //<1>
  `<svg xmlns='http://www.w3.org/2000/svg'
        class='h-5  mr-3'
        viewBox='0 0 576 512'><path d='M512 80c8.8 0 16 7.2 16 16V416c0 8.8-7.2 16-16 16H64c-8.8 0-16-7.2-16-16V96c0-8.8 7.2-16 16-16H512zM64 32C28.7 32 0 60.7 0 96V416c0 35.3 28.7 64 64 64H512c35.3 0 64-28.7 64-64V96c0-35.3-28.7-64-64-64H64zM200 208c14.2 0 27 6.1 35.8 16c8.8 9.9 24 10.7 33.9 1.9s10.7-24 1.9-33.9c-17.5-19.6-43.1-32-71.5-32c-53 0-96 43-96 96s43 96 96 96c28.4 0 54-12.4 71.5-32c8.8-9.9 8-25-1.9-33.9s-25-8-33.9 1.9c-8.8 9.9-21.6 16-35.8 16c-26.5 0-48-21.5-48-48s21.5-48 48-48zm144 48c0-26.5 21.5-48 48-48c14.2 0 27 6.1 35.8 16c8.8 9.9 24 10.7 33.9 1.9s10.7-24 1.9-33.9c-17.5-19.6-43.1-32-71.5-32c-53 0-96 43-96 96s43 96 96 96c28.4 0 54-12.4 71.5-32c8.8-9.9 8-25-1.9-33.9s-25-8-33.9 1.9c-8.8 9.9-21.6 16-35.8 16c-26.5 0-48-21.5-48-48z'/>
  </svg>` //<2>
);
// end::customization-ui-groups[]
