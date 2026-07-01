import { screen } from '@testing-library/vue';
import { describe, expect, it } from 'vitest';

import DetailsMetadata from './details-metadata.vue';

import { applications } from '@/mocks/applications/data';
import Application from '@/services/application';
import Instance from '@/services/instance';
import { render } from '@/test-utils';

describe('DetailsMetadata', () => {
  it('should render metadata table with keys and values', () => {
    const application = new Application(applications[0]);
    const instance = application.instances[0];

    render(DetailsMetadata, {
      props: { instance },
    });

    expect(screen.getByText('startup')).toBeVisible();
    expect(screen.getByText('2021-10-29T08:50:07.486289+02:00')).toBeVisible();
    expect(screen.getByText('tags.environment')).toBeVisible();
    expect(screen.getByText('test')).toBeVisible();
  });

  it('should show metadata count in title', () => {
    const application = new Application(applications[0]);
    const instance = application.instances[0];

    render(DetailsMetadata, {
      props: { instance },
    });

    expect(screen.getByText('(2)')).toBeVisible();
  });

  it('should show no metadata message if metadata is empty', () => {
    const application = new Application(applications[0]);
    const instance = new Instance({
      ...application.instances[0],
      registration: {
        ...application.instances[0].registration,
        metadata: {},
      },
    });

    render(DetailsMetadata, {
      props: { instance },
    });

    expect(
      screen.getByText('instances.details.metadata.no_data_provided'),
    ).toBeVisible();
  });

  it('should show count as (0) when metadata is empty', () => {
    const application = new Application(applications[0]);
    const instance = new Instance({
      ...application.instances[0],
      registration: {
        ...application.instances[0].registration,
        metadata: {},
      },
    });

    render(DetailsMetadata, {
      props: { instance },
    });

    expect(screen.getByText('(0)')).toBeVisible();
  });

  it('should sort metadata keys alphabetically', () => {
    const application = new Application(applications[0]);
    const instance = new Instance({
      ...application.instances[0],
      registration: {
        ...application.instances[0].registration,
        metadata: {
          zebra: 'value1',
          apple: 'value2',
          banana: 'value3',
        },
      },
    });

    render(DetailsMetadata, {
      props: { instance },
    });

    const keys = screen.getAllByRole('term');
    expect(keys[0]).toHaveTextContent('apple');
    expect(keys[1]).toHaveTextContent('banana');
    expect(keys[2]).toHaveTextContent('zebra');
  });
});
