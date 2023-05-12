import matchers from '@testing-library/jest-dom/matchers';
import { cleanup } from '@testing-library/vue';
import { afterEach, expect, vi } from 'vitest';

// extends Vitest's expect method with methods from react-testing-library
expect.extend(matchers);

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

// runs a cleanup after each test case (e.g. clearing jsdom)
afterEach(() => {
  vi.clearAllMocks();
  cleanup();
});
