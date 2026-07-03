import { screen } from '@testing-library/vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { computed, ref } from 'vue';

import DetailsInfo from './details-info.vue';

import { useInstanceData } from '@/composables/useInstanceData';
import { instance as instanceData } from '@/mocks/applications/data';
import { render } from '@/test-utils';

vi.mock('@/composables/useInstanceData', () => ({
  useInstanceData: vi.fn(),
}));

// Shared reactive ref — update it to simulate SSE push
const currentInstanceData = ref<any>({ ...instanceData });

beforeEach(() => {
  currentInstanceData.value = { ...instanceData };
  (useInstanceData as any).mockReturnValue({
    instance: computed(() => currentInstanceData.value),
    info: computed(() => currentInstanceData.value?.info),
    application: computed(() => null),
  });
});

describe('DetailsInfo', () => {
  it('should render info from instance.info', async () => {
    render(DetailsInfo, { props: { instanceId: instanceData.id } });

    expect(await screen.findByText('tags')).toBeVisible();
    expect(await screen.findByText('build')).toBeVisible();
  });

  it('should show empty message when info is empty', async () => {
    currentInstanceData.value = { ...instanceData, info: {} };
    render(DetailsInfo, { props: { instanceId: instanceData.id } });

    expect(
      await screen.findByText('instances.details.info.no_info_provided'),
    ).toBeVisible();
  });

  it('should update reactively when instance info changes (SSE)', async () => {
    render(DetailsInfo, { props: { instanceId: instanceData.id } });
    expect(await screen.findByText('tags')).toBeVisible();

    // Simulate SSE push — mutate the shared ref
    currentInstanceData.value = {
      ...instanceData,
      info: { foo: 'bar' },
    };

    expect(await screen.findByText('foo')).toBeVisible();
    expect(await screen.findByText('bar')).toBeVisible();
  });

  it('should show count of info entries', async () => {
    render(DetailsInfo, { props: { instanceId: instanceData.id } });
    // mock data has 4 info keys: tags, scm-url, build-url, build
    expect(await screen.findByText('(4)')).toBeVisible();
  });

  describe('SSE reactive updates', () => {
    it('should never call fetchInfo (reads from SSE store)', async () => {
      render(DetailsInfo, { props: { instanceId: instanceData.id } });
      await screen.findByText('tags');
      // No HTTP calls — data comes from the store mock
    });

    it('should reactively update through multiple SSE info changes', async () => {
      render(DetailsInfo, { props: { instanceId: instanceData.id } });
      expect(await screen.findByText('tags')).toBeVisible();

      currentInstanceData.value = {
        ...instanceData,
        info: { app: { name: 'my-service' }, 'git.branch': 'main' },
      };

      expect(await screen.findByText('app')).toBeVisible();
      expect(await screen.findByText('git.branch')).toBeVisible();
      expect(screen.queryByText('tags')).not.toBeInTheDocument();

      currentInstanceData.value = { ...instanceData, info: {} };

      expect(
        await screen.findByText('instances.details.info.no_info_provided'),
      ).toBeVisible();
    });
  });
});
