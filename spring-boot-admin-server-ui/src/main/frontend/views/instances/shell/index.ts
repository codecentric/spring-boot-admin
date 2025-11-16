import InstanceShell from '@/views/instances/shell/InstanceShell.vue';

export default {
  install({ viewRegistry }: { viewRegistry: ViewRegistry }) {
    viewRegistry.addView({
      name: 'instances',
      path: '/instances/:instanceId',
      component: InstanceShell,
      isEnabled() {
        return false;
      },
    });
  },
};
