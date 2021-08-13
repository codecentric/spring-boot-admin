import {render} from '@/test-utils';
import NavbarItemLanguageSelector from './navbar-item-language-selector';
import {screen} from '@testing-library/vue';
import userEvent from '@testing-library/user-event';

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
    const buttons = await screen.findAllByRole('button', {name: 'Deutschland (de)'});
    expect(buttons[0]).toBeDefined()
  });

  it('should print locale with the country for available language in menu', async () => {
    const languageButton = await screen.findAllByRole('button', {name: 'Deutschland (de)'});
    await userEvent.click(languageButton[0]);

    expect(await screen.findByRole('button', {name: 'France (fr)'})).toBeDefined()
  });

  it('should print the locale as label when it cannot be translated', async () => {
    render(NavbarItemLanguageSelector, {
      props: {
        availableLocales: ['zz'],
        currentLocale: 'zz'
      }
    })

    const htmlElement = await screen.findAllByRole('button', {name: 'zz (zz)'});
    expect(htmlElement[0]).toBeDefined()
  });

  it('should emit the selected locale', async () => {
    await userEvent.click(await screen.findByRole('button', {name: 'France (fr)'}));

    expect(wrapper.emitted().localeChanged[0]).toContain('fr')
  });
});
