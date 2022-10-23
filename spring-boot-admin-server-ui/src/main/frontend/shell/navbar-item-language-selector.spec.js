import userEvent from '@testing-library/user-event';
import { screen } from '@testing-library/vue';
import { mount } from '@vue/test-utils';

import NavbarItemLanguageSelector from './navbar-item-language-selector';

import { render } from '@/test-utils';

describe('NavbarItemLanguageSelector', () => {
  let wrapper;

  beforeEach(() => {
    wrapper = render(NavbarItemLanguageSelector, {
      props: {
        availableLocales: ['de', 'fr'],
        currentLocale: 'de',
      },
    });
  });

  it('should print the locale with the country for selected language/locale', async () => {
    const buttons = await screen.findByText('Deutsch');
    expect(buttons).toBeDefined();
  });

  it('should print locale with the country for available language in menu', async () => {
    const languageButton = await screen.findByLabelText('Open menu');
    await userEvent.click(languageButton);

    expect(await screen.findByText('français')).toBeDefined();
  });

  it('should print the locale as label when it cannot be translated', async () => {
    render(NavbarItemLanguageSelector, {
      props: {
        availableLocales: ['zz'],
        currentLocale: 'zz',
      },
    });

    const htmlElement = await screen.findByText('zz');
    expect(htmlElement).toBeDefined();
  });

  it('should emit the selected locale', async () => {
    await userEvent.click(await screen.findByLabelText('Open menu'));
    await userEvent.click(await screen.findByText('français'));

    expect(wrapper.emitted().localeChanged[0]).toContain('fr');
  });

  it.each`
    locale     | expected
    ${'de'}    | ${'Deutsch'}
    ${'is'}    | ${'íslenska'}
    ${'de-DE'} | ${'Deutsch (Deutschland)'}
    ${'zh-CN'} | ${'简体中文'}
    ${'zh-TW'} | ${'繁體中文'}
  `("should show '$expected' for given '$locale'", ({ locale, expected }) => {
    let wrapper = mount(NavbarItemLanguageSelector, {
      propsData: {
        availableLocales: [],
        currentLocale: 'en',
      },
    });

    expect(wrapper.vm.mapLocale(locale).label).toEqual(expected);
  });
});
