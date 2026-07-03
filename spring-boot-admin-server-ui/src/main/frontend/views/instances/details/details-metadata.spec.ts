import { screen } from '@testing-library/vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { computed } from 'vue';

import DetailsMetadata from './details-metadata.vue';

import { useInstanceData } from '@/composables/useInstanceData';
import { instance as instanceData } from '@/mocks/applications/data';
import { render } from '@/test-utils';

vi.mock('@/composables/useInstanceData', () => ({
  useInstanceData: vi.fn(),
}));

function setupInstance(registrationOverrides = {}) {
  const registration = {
    ...instanceData.registration,
    ...registrationOverrides,
  };
  (useInstanceData as any).mockReturnValue({
    instance: computed(() => ({ ...instanceData, registration })),
    metadata: computed(() => registration.metadata),
    application: computed(() => null),
  });
}

describe('DetailsMetadata', () => {
  beforeEach(() => {
    setupInstance();
  });

  it('should render metadata table with keys and values', () => {
    render(DetailsMetadata, { props: { instanceId: instanceData.id } });

    expect(screen.getByText('startup')).toBeVisible();
    expect(screen.getByText('2021-10-29T08:50:07.486289+02:00')).toBeVisible();
    expect(screen.getByText('tags.environment')).toBeVisible();
    expect(screen.getByText('test')).toBeVisible();
  });

  it('should show metadata count in title', () => {
    render(DetailsMetadata, { props: { instanceId: instanceData.id } });
    expect(screen.getByText('(2)')).toBeVisible();
  });

  it('should show no metadata message if metadata is empty', () => {
    setupInstance({ metadata: {} });
    render(DetailsMetadata, { props: { instanceId: instanceData.id } });

    expect(
      screen.getByText('instances.details.metadata.no_data_provided'),
    ).toBeVisible();
  });

  it('should show count as (0) when metadata is empty', () => {
    setupInstance({ metadata: {} });
    render(DetailsMetadata, { props: { instanceId: instanceData.id } });
    expect(screen.getByText('(0)')).toBeVisible();
  });

  it('should sort metadata keys alphabetically', () => {
    setupInstance({
      metadata: { zebra: 'value1', apple: 'value2', banana: 'value3' },
    });
    render(DetailsMetadata, { props: { instanceId: instanceData.id } });

    const keys = screen.getAllByRole('term');
    expect(keys[0]).toHaveTextContent('apple');
    expect(keys[1]).toHaveTextContent('banana');
    expect(keys[2]).toHaveTextContent('zebra');
  });
});
