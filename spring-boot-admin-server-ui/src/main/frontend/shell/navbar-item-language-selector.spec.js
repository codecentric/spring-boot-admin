import {render} from '@/test-utils';
import NavbarItemLanguageSelector from './navbar-item-language-selector';
import {screen} from '@testing-library/vue';
import userEvent from '@testing-library/user-event';
import {mount} from '@vue/test-utils';

describe('NavbarItemLanguageSelector', () => {
  let wrapper;

  beforeEach(() => {
    wrapper = render(NavbarItemLanguageSelector, {
      props: {
        availableLocales: [
          'de', 'fr'
        ],
        currentLocale: 'de'
      }
    })
  });

  it('should print the locale with the country for selected language/locale', async () => {
    const buttons = await screen.findAllByRole('button', {name: 'Deutsch'});
    expect(buttons[0]).toBeDefined()
  });

  it('should print locale with the country for available language in menu', async () => {
    const languageButton = await screen.findAllByRole('button', {name: 'Deutsch'});
    await userEvent.click(languageButton[0]);

    expect(await screen.findByRole('button', {name: 'français'})).toBeDefined()
  });

  it('should print the locale as label when it cannot be translated', async () => {
    render(NavbarItemLanguageSelector, {
      props: {
        availableLocales: ['zz'],
        currentLocale: 'zz'
      }
    })

    const htmlElement = await screen.findAllByRole('button', {name: 'zz'});
    expect(htmlElement[0]).toBeDefined()
  });

  it('should emit the selected locale', async () => {
    await userEvent.click(await screen.findByRole('button', {name: 'français'}));

    expect(wrapper.emitted().localeChanged[0]).toContain('fr')
  });

  it.each`
    locale      | expected
    ${'de'}     | ${'Deutsch'}
    ${'is'}     | ${'íslenska'}
    ${'de-DE'}  | ${'Deutsch (Deutschland)'}
    ${'zh-CN'}  | ${'简体中文'}
    ${'zh-TW'}  | ${'繁體中文'}
  `('should show \'$expected\' for given \'$locale\'', ({locale, expected}) => {
    let wrapper = mount(NavbarItemLanguageSelector, {
      propsData: {
        availableLocales: [],
        currentLocale: 'en'
      }
    });

    expect(wrapper.vm.mapLocale(locale).label).toEqual(expected);
  });
});
