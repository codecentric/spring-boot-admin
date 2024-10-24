import { screen } from '@testing-library/vue';
import { Mocked, afterEach, describe, expect, it, vi } from 'vitest';

import { getAvailableLocales } from '@/i18n';
import { getCurrentUser } from '@/sba-config';
import Navbar from '@/shell/navbar.vue';
import { render } from '@/test-utils';

vi.mock('@/sba-config', async () => {
  const sbaConfig =
    await vi.importActual<typeof import('@/sba-config')>('@/sba-config');

  return {
    ...sbaConfig,
    getCurrentUser: vi.fn(),
    getAvailableLanguages: vi.fn(),
  };
});

vi.mock('@/i18n', async () => {
  const i18n = await vi.importActual<typeof import('@/i18n')>('@/i18n');

  return {
    ...i18n,
    getAvailableLocales: vi.fn().mockReturnValue([]),
  };
});

describe('Navbar', function () {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it('User menu is visible, when a user is logged in', async function () {
    (getCurrentUser as Mocked<any>).mockReturnValue({
      name: 'mail@example.org',
    });

    render(Navbar);

    expect(screen.getByTestId('usermenu')).toBeVisible();
    expect(screen.getByText('mail@example.org')).toBeVisible();
  });

  it.each`
    user
    ${null}
    ${{}}
  `("User menu is hidden, when user object is '$user'", async function (user) {
    (getCurrentUser as Mocked<any>).mockReturnValue(user);

    render(Navbar);

    expect(screen.queryByTestId('usermenu')).not.toBeInTheDocument();
  });

  it('Language menu is visible, when more than one language is available', async function () {
    (getAvailableLocales as Mocked<any>).mockReturnValue(['de', 'en', 'fr']);

    render(Navbar, { locale: 'de' });

    expect(screen.queryByText('Deutsch')).toBeInTheDocument();
  });

  it('Language menu is hidden, when just one language is available', async function () {
    (getAvailableLocales as Mocked<any>).mockReturnValue(['de']);

    render(Navbar, { locale: 'de' });

    expect(screen.queryByText('Deutsch')).not.toBeInTheDocument();
  });
});
