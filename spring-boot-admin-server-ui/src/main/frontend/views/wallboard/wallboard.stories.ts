import { vueRouter } from 'storybook-vue3-router';

import Application from '../../services/application.js';
import Wallboard from './index.vue';

import { HealthStatus } from '@/HealthStatus';
import { useApplicationStore } from '@/composables/useApplicationStore';
import Instance from '@/services/instance';

export default {
  component: Wallboard,
  title: 'Views/Wallboard',
};

const Template = (args) => ({
  components: { Wallboard },
  setup() {
    const { applicationStore } = useApplicationStore();
    applicationStore._dispatchEvent(
      'changed',
      Object.keys(HealthStatus).map((status) => {
        return new Application({
          status: status,
          statusTimestamp: '2023-05-02',
          name: `controller${status}`,
          group: 'group-' + Math.floor(Math.random() * 10),
          buildVersion: '1.2.3',
          instances: [new Instance({ id: '123' })],
        });
      }),
    );
    return { args };
  },
  template: '<Wallboard />',
});

export const Default = {
  render: Template,
  decorators: [
    vueRouter(
      [
        {
          name: 'wallboard',
          path: '/',
          component: Template,
        },
      ],
      { initialRoute: '/' },
    ),
  ],
};
