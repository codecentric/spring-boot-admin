import { isEmpty, merge } from 'lodash-es';
import { createI18n } from 'vue-i18n';

import sbaConfig from '../sba-config.js';

const context = import.meta.globEager('../**/(*.)?i18n.*.json');
const messages = Object.keys(context)
  .map((key) => {
    const localeFromFile = /\.*i18n\.?([^/]*)\.json$/.exec(key);
    const messages = context[key].default;
    if (localeFromFile[1]) {
      return {
        [localeFromFile[1]]: messages,
      };
    } else {
      return messages;
    }
  })
  .reduce((prev, cur) => merge(prev, cur), {});

export const AVAILABLE_LANGUAGES = getAvailableLocales();

function getAvailableLocales() {
  let valueFromServer = sbaConfig.uiSettings.availableLanguages;

  const strings = Object.keys(messages);
  return isEmpty(valueFromServer)
    ? strings
    : valueFromServer.filter((language) => strings.includes(language));
}

let browserLanguage = navigator.language;
if (!browserLanguage.includes('zh')) {
  browserLanguage = browserLanguage.split('-')[0];
}

const i18n = createI18n({
  locale: AVAILABLE_LANGUAGES.includes(browserLanguage)
    ? browserLanguage
    : 'en',
  fallbackLocale: 'en',
  legacy: false,
  silentFallbackWarn: process.env.NODE_ENV === 'production',
  silentTranslationWarn: process.env.NODE_ENV === 'production',
  messages,
});

export default i18n;
