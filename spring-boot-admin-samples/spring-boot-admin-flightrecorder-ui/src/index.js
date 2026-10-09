import flightrecorderEndpoint from "./flightrecorder-endpoint.vue";

import { library } from '@fortawesome/fontawesome-svg-core';
import { fas } from "@fortawesome/free-solid-svg-icons";

library.add(fas);

// tag::customization-ui-endpoint[]
SBA.viewRegistry.addView({
  name: "instances/flightrecorder",
  parent: "instances",
  path: "flightrecorder",
  component: flightrecorderEndpoint,
  label: "Flight Recorder",
  group: "jvm",
  order: 1000,
  isEnabled: ({ instance }) => {
    return instance.hasEndpoint("flightrecorder");
  },
});
// end::customization-ui-endpoint[]
