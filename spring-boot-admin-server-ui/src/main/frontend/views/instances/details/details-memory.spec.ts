import { screen } from '@testing-library/vue';
import rxjs from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { render } from '@/test-utils';
import DetailsMemory from '@/views/instances/details/details-memory.vue';

vi.mock('@/sba-config', async () => {
  const sbaConfig: any = await vi.importActual('@/sba-config');
  return {
    default: {
      ...sbaConfig.default,
      uiSettings: {
        pollTimer: {
          memory: 1234,
        },
      },
    },
  };
});

describe('DetailsMemory', () => {
  it('should call timer with configured amount', async () => {
    const timer = vi.spyOn(rxjs, 'timer');

    const Instance = (await import('@/services/instance')).default;
    render(DetailsMemory, {
      stubs: {
        MemChart: true,
      },
      props: {
        instance: new Instance({ id: '1' }),
        type: 'heap',
      },
    });

    expect(timer).toHaveBeenCalledWith(0, 1234);
  });

  describe('when type is heap', async () => {
    const Instance = (await import('@/services/instance')).default;

    beforeEach(() => {
      render(DetailsMemory, {
        stubs: {
          MemChart: true,
        },
        props: {
          instance: new Instance({ id: '1' }),
          type: 'heap',
        },
      });
    });

    it('should render memory used', async () => {
      expect(
        await screen.findByLabelText('instances.details.memory.used'),
      ).toHaveTextContent('115 MB');
    });

    it('should render memory size', async () => {
      expect(
        await screen.findByLabelText('instances.details.memory.size'),
      ).toHaveTextContent('197 MB');
    });

    it('should render memory max', async () => {
      expect(
        await screen.findByLabelText('instances.details.memory.max'),
      ).toHaveTextContent('8.59 GB');
    });

    it('should not render memory metaspace', async () => {
      expect(
        screen.queryByLabelText('instances.details.memory.metaspace'),
      ).not.toBeInTheDocument();
    });
  });

  describe('when type is nonheap', async () => {
    const Instance = (await import('@/services/instance')).default;

    beforeEach(() => {
      render(DetailsMemory, {
        stubs: {
          MemChart: true,
        },
        props: {
          instance: new Instance({ id: '1' }),
          type: 'nonheap',
        },
      });
    });

    it('should render memory used', async () => {
      expect(
        await screen.findByLabelText('instances.details.memory.used'),
      ).toHaveTextContent('115 MB');
    });

    it('should render memory size', async () => {
      expect(
        await screen.findByLabelText('instances.details.memory.size'),
      ).toHaveTextContent('197 MB');
    });

    it('should render memory max', async () => {
      expect(
        await screen.findByLabelText('instances.details.memory.max'),
      ).toHaveTextContent('8.59 GB');
    });

    it('should render memory metaspace', async () => {
      expect(
        await screen.findByLabelText('instances.details.memory.metaspace'),
      ).toHaveTextContent('115 MB');
    });
  });
});
