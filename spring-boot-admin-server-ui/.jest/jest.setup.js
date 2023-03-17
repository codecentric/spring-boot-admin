import '@testing-library/jest-dom';

import { server } from '@/mocks/server';

// Add IntersectionObserver and ResizeObserver to global. HeadlessUI relies on this.
// IntersectionObserver isn't available in test environment
global.IntersectionObserver = jest.fn().mockImplementation(() => ({
  observe: jest.fn(),
  unobserve: jest.fn(),
  disconnect: jest.fn()
}));
global.ResizeObserver = jest.fn().mockImplementation(() => ({
  observe: jest.fn(),
  unobserve: jest.fn(),
  disconnect: jest.fn()
}));

afterEach(() => {
  document.body.innerHTML = '';
});

beforeAll(async () => {
  server.listen();
});

afterAll((done) => {
  setTimeout(() => {
    server.close();
    done();
  }, 500);
});

afterEach(() => server.resetHandlers());
