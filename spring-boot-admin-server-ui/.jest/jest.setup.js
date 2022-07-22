import '@testing-library/jest-dom';
import {server} from '@/mocks/server';

beforeAll(async () => {
  server.listen();

  // IntersectionObserver isn't available in test environment
  const mockIntersectionObserver = jest.fn();
  mockIntersectionObserver.mockReturnValue({
    observe: () => null,
    unobserve: () => null,
    disconnect: () => null
  });
  window.IntersectionObserver = mockIntersectionObserver;
})

afterAll(done => {
  setTimeout(() => {
    server.close();
    done();
  }, 500);
})
