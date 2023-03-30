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
import custom from "./custom.vue";
import handle from "./handle.vue";

// tag::customization-ui-toplevel[]
SBA.use({
  install({ viewRegistry, i18n }) {
    viewRegistry.addView({
      name: "custom", //<1>
      path: "/custom", //<2>
      component: custom, //<3>
      handle, //<4>
      order: 1000, //<5>
    });
    i18n.mergeLocaleMessage("en", {
      custom: {
        label: "My Extensions", //<6>
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
