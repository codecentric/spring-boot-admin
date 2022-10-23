import Application from '../../services/application.js';
import Wallboard from './index.vue';

import { HealthStatus } from '@/HealthStatus';
import { useApplicationStore } from '@/composables/useApplicationStore';
import { applications } from '@/mocks/applications/data';

export default {
  component: Wallboard,
  title: 'Views/Wallboard',
};

const Template = (args) => ({
  components: { Wallboard },
  setup() {
    let { applicationStore } = useApplicationStore();
    applicationStore._dispatchEvent(
      'changed',
      shuffle([...healthStatus, ...healthStatus]).map((healthStatus) => {
        const application = new Application(applications[0]);
        application.statusTimestamp = Date.now();
        application.name = healthStatus;
        application.status = healthStatus;
        return application;
      })
    );
    return { args };
  },
  template: '<Wallboard v-bind="args" />',
});

export const Default = Template.bind({});
const healthStatus = Object.keys(HealthStatus);
Default.args = {
  applications: shuffle([...healthStatus, ...healthStatus]).map(
    (healthStatus) => {
      const application = new Application(applications[0]);
      application.statusTimestamp = Date.now();
      application.name = healthStatus;
      application.status = healthStatus;
      return application;
    }
  ),
  applicationsInitialized: true,
};

function shuffle(a) {
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]];
  }
  return a;
}
