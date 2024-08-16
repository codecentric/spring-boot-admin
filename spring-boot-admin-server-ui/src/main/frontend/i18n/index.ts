import { isEmpty, merge } from 'lodash-es';
import { createI18n } from 'vue-i18n';

import sbaConfig from '@/sba-config';

const context = import.meta.glob('../**/(*.)?i18n.*.json', { eager: true });
const messages = Object.keys(context)
  .map((key) => {
    const localeFromFile = /\.*i18n\.?([^/]*)\.json$/.exec(key);
    const messages = (context[key] as { default: never }).default;
    if (localeFromFile[1]) {
      return {
        [localeFromFile[1]]: messages,
      };
    } else {
      return messages;
    }
  })
  .reduce((prev, cur) => merge(prev, cur), {});

export function getAvailableLocales() {
  const valueFromServer = sbaConfig.uiSettings.availableLanguages;

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
  locale: getAvailableLocales().includes(browserLanguage)
    ? browserLanguage
    : 'en',
  fallbackLocale: 'en',
  legacy: false,
  silentFallbackWarn: process.env.NODE_ENV === 'production',
  silentTranslationWarn: process.env.NODE_ENV === 'production',
  messages,
});

export default i18n;
