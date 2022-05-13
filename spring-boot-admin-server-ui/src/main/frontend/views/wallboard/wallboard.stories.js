import Wallboard from "./index.vue";
import {applications} from "../../mocks/applications/data.js";
import Application from "../../services/application.js";
import {HealthStatus} from "../../HealthStatus.js";

export default {
  component: Wallboard,
  title: 'Views/Wallboard',
};

const Template = (args) => ({
  components: {Wallboard},
  setup() {
    return {args};
  },
  template: '<Wallboard v-bind="args" />',
});


export const Default = Template.bind({})
const healthStatus = Object.keys(HealthStatus);
Default.args = {
  applications: shuffle([...healthStatus, ...healthStatus])
    .map((healthStatus) => {
      const application = new Application(applications[0]);
      application.statusTimestamp = Date.now();
      application.name = healthStatus;
      application.status = healthStatus;
      return application;
    }),
  applicationsInitialized: true
}


function shuffle(a) {
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]];
  }
  return a;
}
