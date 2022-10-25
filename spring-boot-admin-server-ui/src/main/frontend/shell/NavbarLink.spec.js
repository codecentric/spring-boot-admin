import { screen } from '@testing-library/vue';
import { Text, h } from 'vue';

import NavbarLink from '@/shell/NavbarLink';
import { render } from '@/test-utils';

describe('NavbarLink', () => {
  it('renders just as text label', async () => {
    render(NavbarLink, {
      props: {
        view: {
          label: 'Label',
          name: 'Name',
        },
      },
    });
    expect(await screen.findByText('Label')).toBeDefined();
  });

  it('prints component as simple link', async () => {
    render(NavbarLink, {
      props: {
        view: {
          href: 'https://codecentric.de',
          handle: h('div', { innerHTML: '🚀' }),
        },
      },
    });
    expect(await screen.findByRole('link', { name: '🚀' })).toBeDefined();
  });

  it('prints component as router link', async () => {
    render(NavbarLink, {
      props: {
        view: {
          name: 'Router Link',
          handle: h(Text, '🚀'),
        },
      },
    });
    expect(await screen.findByRole('anchor', { name: '🚀' })).toBeDefined();
  });
});
