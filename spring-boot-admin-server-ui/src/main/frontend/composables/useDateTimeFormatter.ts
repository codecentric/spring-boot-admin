type DateFormatterOptions = {
  dateTimeFormat?: Intl.DateTimeFormatOptions;
  timeFormat?: Intl.DateTimeFormatOptions;
};

export const useDateTimeFormatter = (options?: DateFormatterOptions) => {
  const userLocale = navigator.languages
    ? navigator.languages[0]
    : navigator.language;

  const dateTimeFormat = new Intl.DateTimeFormat(userLocale, {
    ...{ dateStyle: 'medium', timeStyle: 'medium' },
    ...(options?.dateTimeFormat ?? {}),
  });

  return {
    formatDateTime: (date: Date) => {
      return dateTimeFormat.format(date);
    },
  };
};
