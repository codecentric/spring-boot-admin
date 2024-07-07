// @vitest-environment happy-dom
import userEvent from '@testing-library/user-event';
import { screen } from '@testing-library/vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { applications } from '@/mocks/applications/data';
import Application from '@/services/application';
import { render } from '@/test-utils';
import Sbomdependencytrees from '@/views/instances/sbomdependencytrees/index.vue';

describe('Sbomdependencytrees', () => {
  const application = new Application(applications[0]);
  const instance = application.instances[0];

  beforeEach(async () => {
    render(Sbomdependencytrees, {
      props: {
        instance: instance,
      },
    });
  });

  it('renders correctly with given props', async () => {
    expect(
      await screen.findAllByText(/spring-boot-admin-sample-servlet/),
    ).toBeDefined();
    expect(
      await screen.findAllByText(/spring-boot-admin-sample-custom-ui/),
    ).toBeDefined();
    expect(
      await screen.findAllByText(/spring-boot-admin-starter-server/),
    ).toBeDefined();
  });

  it.skip('filters the tree correctly based on filter prop', async () => {
    expect(
      await screen.findAllByText(/spring-boot-admin-sample-servlet/),
    ).toBeDefined();

    const filterInput = screen.getByLabelText('Filter');

    await userEvent.type(filterInput, 'nothingtobefound');

    await vi.waitFor(
      async () =>
        expect(
          await screen.findAllByText(/spring-boot-admin-sample-servlet/),
        ).not.toBeDefined(),
      {
        timeout: 2000,
      },
    );
  });
});
