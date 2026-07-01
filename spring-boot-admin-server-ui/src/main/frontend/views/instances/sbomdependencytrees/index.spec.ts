import { screen, waitFor } from '@testing-library/vue';
import { describe, expect, it, vi } from 'vitest';

import Instance from '@/services/instance';
import { render } from '@/test-utils';
import SbomDependencyTrees from '@/views/instances/sbomdependencytrees/index.vue';

type Deferred<T> = {
  promise: Promise<T>;
  resolve: (value: T) => void;
  reject: (reason?: unknown) => void;
};

const deferred = <T>(): Deferred<T> => {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((res, rej) => {
    resolve = res;
    reject = rej;
  });
  return { promise, resolve, reject };
};

describe('SBOM dependency trees - empty state', () => {
  it('hides warning while fetchSbomIds() is pending and shows it when resolved with empty list', async () => {
    const d = deferred<{ data: { ids: string[] } }>();
    const instance = new Instance({
      id: '4711',
      registration: {
        name: 'test',
        healthUrl: 'http://localhost:8080/actuator/health',
        source: 'http-api',
      },
    });

    const fetchSbomIds = vi.fn().mockReturnValue(d.promise);
    instance.fetchSbomIds = fetchSbomIds;

    render(SbomDependencyTrees, {
      props: {
        instance,
      },
    });

    await waitFor(() => expect(fetchSbomIds).toHaveBeenCalledTimes(1));

    expect(
      screen.getByTestId('instance-section-loading-spinner'),
    ).toBeInTheDocument();
    expect(
      screen.queryByText('instances.dependencies.no_data_provided'),
    ).not.toBeInTheDocument();

    d.resolve({ data: { ids: [] } });

    await waitFor(() => {
      expect(
        screen.queryByTestId('instance-section-loading-spinner'),
      ).not.toBeInTheDocument();
      expect(
        screen.getByText('instances.dependencies.no_data_provided'),
      ).toBeInTheDocument();
    });
  });
});
