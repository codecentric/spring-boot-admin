import i18n from 'i18next';
import * as moment from 'moment';
import I18nextBrowserLanguageDetector from 'i18next-browser-languagedetector';

import localesEN from './default.en';
import localesDE from './default.de';

let resources = {
  en: {name: 'English', locale: 'en', translation: localesEN},
  de: {name: 'Deutsch', locale: 'de', translation: localesDE}
};

i18n
  .use(I18nextBrowserLanguageDetector)
  .on('languageChanged', (lang) => moment.locale(lang))
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
