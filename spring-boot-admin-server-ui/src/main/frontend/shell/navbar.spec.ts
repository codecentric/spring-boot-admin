import { screen } from '@testing-library/vue';
import { describe, expect, it, vi } from 'vitest';

import Navbar from '@/shell/navbar.vue';
import { render } from '@/test-utils';

vi.mock('@/sba-config');

describe('Navbar', function () {
  it('User menu is visible, when a user is logged in', async function () {
    const config = await vi.importActual('@/sba-config');
    return {
      default: {
        ...config.default,
        user: {
          name: 'mail@example.com',
        },
      },
    };

    render(Navbar);

    expect(screen.queryByText('mail@example.org')).toBeVisible();
  });

  it('User menu is hidden, when no user is logged in', async function () {
    global.SBA = {
      user: null,
    };

    render(Navbar);

    expect(screen.queryByText('mail@example.org')).not.toBeInTheDocument();
  });
});
