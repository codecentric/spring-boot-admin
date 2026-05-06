export const useNumberFormatter = (options?: Intl.NumberFormatOptions) => {
  const userLocale = navigator.languages
    ? navigator.languages[0]
    : navigator.language;

  const numberFormat = new Intl.NumberFormat(userLocale, options);

  return {
    formatNumber: (value: number) => {
      return numberFormat.format(value);
    },
  };
};

export const usePercentFormatter = (options?: Intl.NumberFormatOptions) => {
  const { formatNumber } = useNumberFormatter({
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
    ...options,
    style: 'percent',
  });

  return {
    formatPercent: (value: number) => {
      return formatNumber(value);
    },
  };
};
