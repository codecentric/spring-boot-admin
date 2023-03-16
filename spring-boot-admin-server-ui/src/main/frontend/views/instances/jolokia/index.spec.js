import userEvent from '@testing-library/user-event';
import { screen, waitFor } from '@testing-library/vue';

import { applications } from '@/mocks/applications/data';
import Application from '@/services/application';
import { render } from '@/test-utils';
import Jolokia from '@/views/instances/jolokia/index.vue';

describe('Jolokia', () => {
  let renderResult;

  beforeEach(async () => {
    const application = new Application(applications[0]);
    const instance = application.instances[0];
    instance.isEnabled = jest.fn(() => true);

    renderResult = await render(Jolokia, {
      props: {
        application,
        instance,
      },
    });
  });

  it('DefaultDomain is selected as default', async () => {
    let button = await screen.findByRole('button', {
      name: 'DefaultDomain',
    });
    await waitFor(() => {
      expect(button).toHaveClass('is-active');
    });
  });

  it('click on specific domain opens JMX Beans list', async () => {
    let button = await screen.findByRole('button', {
      name: 'com.codecentric.boot.sample',
    });
    await userEvent.click(button);

    await waitFor(async () => {
      expect(await screen.findByText('StringMapManagedBean')).toBeVisible();
      expect(
        await screen.findByText('StringMapManagedBean.StringSetter')
      ).toBeVisible();
    });
  });

  describe('Attributes', () => {
    beforeEach(async () => {
      let button = await screen.findByRole('button', {
        name: 'com.codecentric.boot.sample',
      });
      await userEvent.click(button);

      await userEvent.click(await screen.findByText('stringMapManagedBean'));
    });

    it('opens attributes on default after click on bean header', async () => {
      let inputForTest = await screen.findByLabelText('Test');
      expect(inputForTest).toBeDisabled();
    });

    it('allows to edit writeable attributes', async () => {
      let inputForTest = await screen.findByText('Test');
      await userEvent.dblClick(inputForTest);
      expect(inputForTest).toBeEnabled();
    });
  });

  describe('Operations', () => {
    it('allows to open tab to display operations', async () => {
      let button = await screen.findByRole('button', {
        name: 'com.codecentric.boot.sample',
      });
      await userEvent.click(button);

      await userEvent.click(await screen.findByText('stringMapManagedBean'));
      await userEvent.click(await screen.findByText('Operations'));

      let inputForTest = await screen.findByText('getSize()');
      expect(inputForTest).toBeVisible();
    });
  });
});
