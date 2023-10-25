import userEvent from '@testing-library/user-event';
import { screen } from '@testing-library/vue';
import { describe, expect, it } from 'vitest';

import SbaNavLanguageSelector from '@/shell/sba-nav-language-selector.vue';
import { render } from '@/test-utils';

describe('NavbarItemLanguageSelector', () => {
  it('should print the locale with the country for selected language/locale', async () => {
    render(SbaNavLanguageSelector, {
      locale: 'de',
      props: {
        availableLocales: ['de', 'fr'],
      },
    });

    const buttons = await screen.findByText('Deutsch');
    expect(buttons).toBeDefined();
  });

  it('should print locale with the country for available language in menu', async () => {
    render(SbaNavLanguageSelector, {
      locale: 'de',
      props: {
        availableLocales: ['de', 'fr'],
      },
    });

    const languageButton = await screen.findByText('Deutsch');
    await userEvent.click(languageButton);

    expect(await screen.findByText('français')).toBeDefined();
  });

  it('should print the locale as label when it cannot be translated', async () => {
    render(SbaNavLanguageSelector, {
      locale: 'zz',
      props: {
        availableLocales: ['zz'],
      },
    });

    const htmlElement = await screen.findByText('zz');
    expect(htmlElement).toBeDefined();
  });

  it('should emit the selected locale', async () => {
    const wrapper = render(SbaNavLanguageSelector, {
      locale: 'de',
      props: {
        availableLocales: ['de', 'fr'],
      },
    });

    await userEvent.click(await screen.findByText('Deutsch'));
    await userEvent.click(await screen.findByText('français'));

    const emitted = wrapper.emitted();
    expect(emitted['locale-changed'][0]).toContain('fr');
  });

  it.each`
    locale     | expected
    ${'de'}    | ${'Deutsch'}
    ${'is'}    | ${'íslenska'}
    ${'de-DE'} | ${'Deutsch (Deutschland)'}
    ${'zh-CN'} | ${'简体中文'}
    ${'zh-TW'} | ${'繁體中文'}
  `(
    "should show '$expected' for given '$locale'",
    async ({ locale, expected }) => {
      render(SbaNavLanguageSelector, {
        locale,
        propsData: {
          availableLocales: ['de'],
        },
      });

      await userEvent.click(await screen.findByText(expected));
    },
  );
});
