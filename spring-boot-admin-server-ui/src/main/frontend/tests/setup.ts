import '@testing-library/jest-dom';
import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/vue';
import { afterAll, afterEach, beforeAll, vi } from 'vitest';

import { server } from '@/mocks/server';
import sbaConfig from '@/sba-config';

global.IntersectionObserver = vi.fn().mockImplementation(function () {
  return {
    observe: vi.fn(),
    unobserve: vi.fn(),
    disconnect: vi.fn(),
  };
});
global.ResizeObserver = vi.fn().mockImplementation(function () {
  return {
    observe: vi.fn(),
    unobserve: vi.fn(),
    disconnect: vi.fn(),
  };
});
global.matchMedia = vi.fn().mockReturnValue({
  addEventListener: vi.fn(),
  removeEventListener: vi.fn(),
});
global.EventSource = class {
  constructor() {}
  close() {}
};

global.SBA = sbaConfig;

// Mock localStorage globally for all tests
Object.defineProperty(global, 'localStorage', {
  value: {
    getItem: vi.fn(),
    setItem: vi.fn(),
    removeItem: vi.fn(),
    clear: vi.fn(),
  },
  writable: true,
});

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterAll(() => server.close());
afterEach(() => server.resetHandlers());

// runs a cleanup after each test case (e.g. clearing jsdom)
afterEach(() => {
  vi.clearAllMocks();
  cleanup();
});
