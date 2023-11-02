import { waitFor } from '@testing-library/vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { reactive } from 'vue';

import { useRouterState } from '@/utils/useRouterState';

const routerReplace = vi.fn();
const mockedQuery = vi.fn().mockReturnValue({});

vi.mock('vue-router', () => {
  return {
    useRouter: () => ({
      replace: routerReplace,
    }),
    useRoute: () =>
      reactive({
        get query() {
          return mockedQuery();
        },
      }),
  };
});

describe('useRouterState', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('should initialize routerState with the initial query parameters', async () => {
    mockedQuery.mockReturnValue({
      string: 'foo',
      boolean: 'true',
      number: '0',
      float: '0.123',
    });

    const routerState = useRouterState();

    await waitFor(() => {
      expect(routerState).toEqual({
        string: 'foo',
        boolean: true,
        number: 0,
        float: 0.123,
      });
    });
  });

  it('should call router with initial state', () => {
    useRouterState({
      foo: 'bar',
    });

    waitFor(() => {
      expect(routerReplace).toHaveBeenCalledWith({
        query: {
          foo: 'bar',
        },
      });
    });
  });

  it('should extend existing query params', () => {
    mockedQuery.mockReturnValue({
      foo: 'bar',
    });

    const routerState = useRouterState({
      bar: 'baz',
    });

    waitFor(() => {
      expect(routerReplace).toHaveBeenCalledWith({
        query: {
          foo: 'bar',
          bar: 'baz',
        },
      });
      expect(routerState).toEqual({
        foo: 'bar',
        bar: 'baz',
      });
    });
  });

  it('should override existing query params', () => {
    mockedQuery.mockReturnValue({
      foo: 'bar',
    });

    const routerState = useRouterState({
      foo: 'baz',
    });

    waitFor(() => {
      expect(routerReplace).toHaveBeenCalledWith({
        query: {
          foo: 'baz',
        },
      });
      expect(routerState).toEqual({
        foo: 'baz',
      });
    });
  });
});
