import '@testing-library/jest-dom';
import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/vue';
import { afterAll, afterEach, beforeAll, vi } from 'vitest';

import { server } from '@/mocks/server';
import sbaConfig from '@/sba-config';

global.IntersectionObserver = vi.fn().mockImplementation(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}));
global.ResizeObserver = vi.fn().mockImplementation(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}));

global.SBA = sbaConfig;

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterAll(() => server.close());
afterEach(() => server.resetHandlers());

// runs a cleanup after each test case (e.g. clearing jsdom)
afterEach(() => {
  vi.clearAllMocks();
  cleanup();
});
