import { PrimeVueConfiguration } from '@primevue/core/config';
import { all } from 'primelocale';

export const PrimeLocale = {
  setLocale(primevue: PrimeVueConfiguration, locale: string) {
    const primeLocale = all[locale];
    primevue.config.locale = primeLocale ?? all.en;
  },
};
