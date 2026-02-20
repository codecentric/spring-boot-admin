// __mocks__/@stekoe/vue-toast-notificationcenter.js
import { vi } from 'vitest';

// Ensure errorSpy is available
if (!globalThis.errorSpy) {
  globalThis.errorSpy = vi.fn();
}

export const useNotificationCenter = () => ({ error: globalThis.errorSpy });

export default {
  install(app) {
    app.config.globalProperties.$nc = { error: globalThis.errorSpy };
  },
};
