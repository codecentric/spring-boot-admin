import i18n from 'i18next';
import I18nextBrowserLanguageDetector from 'i18next-browser-languagedetector';

import localesEN from './locales.en';

let resources = {
  // en: {name: 'English', locale: 'en_GB', translation: localesEN},
  de: {name: 'Deutsch', locale: 'de_DE', translation: {}}
};

i18n
  .use(I18nextBrowserLanguageDetector)
  .init({
    saveMissing: true,
    fallbackLng: 'en',
    debug: true,
    resources: resources
  });

export const AVAILABLE_LANGUAGES = Object.keys(resources).map(lang => ({
  id: lang,
  name: resources[lang].name,
  locale: resources[lang].locale
}));

export function getReadableLanguage(locale) {
  let language = AVAILABLE_LANGUAGES.find(lang => lang.locale === locale || lang.id === locale);
  if (language) {
    return language.name;
  }
  return locale;
}

export default i18n;
