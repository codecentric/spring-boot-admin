import ViewRegistry from '@/viewRegistry';
import handle from '@/views/mcp/Handle.vue';
import McpView from '@/views/mcp/McpView.vue';
import { McpService } from '@/views/mcp/mcp.service';

export default {
  install({ viewRegistry }: { viewRegistry: ViewRegistry }) {
    viewRegistry.addView({
      name: 'mcp',
      path: '/mcp',
      component: McpView,
      handle: handle,
      isEnabled: () => McpService.isAvailable(),
    });
  },
};
