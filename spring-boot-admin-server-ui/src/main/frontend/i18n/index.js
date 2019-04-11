import i18n from 'i18next';
import I18nextBrowserLanguageDetector from 'i18next-browser-languagedetector';

import localesDE from './locales.de';

let resources = {
  de: {translation: localesDE}
};

i18n
  .use(I18nextBrowserLanguageDetector)
  .init({
    fallbackLng: 'en',
    debug: true,
    nsSeparator: ':::',
    keySeparator: '::',
    resources: resources
  });

export const AVAILABLE_LOCALES = Object.keys(resources);
export default i18n;
