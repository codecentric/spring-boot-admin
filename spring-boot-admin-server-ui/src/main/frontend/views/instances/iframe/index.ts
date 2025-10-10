import { VIEW_GROUP } from '@/views/ViewGroup';
import IframeView from '@/views/instances/iframe/IframeView.vue';

export default {
  install({ viewRegistry }) {
    viewRegistry.addView({
      name: 'instances/custom-link-view',
      parent: 'instances',
      path: 'custom-link/:url',
      component: IframeView,
      label: 'instances.custom-link.label',
      order: Number.MAX_SAFE_INTEGER,
      isEnabled: () => false,
    });
  },
};
