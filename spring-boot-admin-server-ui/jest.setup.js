import '@testing-library/jest-dom';
import {server} from '@/mocks/server';

beforeAll(async () => {
  server.listen()
})

afterAll(done => {
  setTimeout(() => {
    server.close();
    done();
  }, 500);
})
