import moment from 'moment/moment';
import { useI18n } from 'vue-i18n';

export const enum PrettyTimeUnit {
  years = 'years',
  months = 'months',
  weeks = 'weeks',
  days = 'days',
  hours = 'hours',
  minutes = 'minutes',
  seconds = 'seconds',
  milliseconds = 'milliseconds',
}

/**
 * Formats a Date or ISO string to a localized date-time string.
 * @param time - Date object or ISO string to format
 * @param locale - Optional locale string (defaults to browser default)
 * @returns Formatted date-time string
 */
export const formatDateTime = (
  time: Date | string | number,
  locale?: string,
) => {
  return new Intl.DateTimeFormat(locale, {
    dateStyle: 'medium',
    timeStyle: 'medium',
  }).format(new Date(time));
};

/**
 * usePrettyTime provides utility functions for formatting time durations and date-times.
 *
 * - formatTime: Converts a duration in milliseconds to a human-readable string (e.g., "2 days 3 hours").
 * - formatDateTime: Formats a Date or ISO string to a localized date-time string.
 *
 * Example:
 * const { formatTime, formatDateTime } = usePrettyTime();
 * formatTime(90061000); // "1 days 1 hours 1 minutes 1 seconds"
 * formatDateTime("2024-06-01T12:34:56Z"); // "Jun 1, 2024, 12:34:56 PM" (locale-dependent)
 */
export const usePrettyTime = () => {
  const { t, locale } = useI18n();

  const formatTime = (time: number) => {
    if (time < 0) {
      return t('time.unknown');
    }

    const duration = moment.duration(time);

    const output = {
      [PrettyTimeUnit.years]: Math.floor(duration.asYears()),
      [PrettyTimeUnit.months]: Math.floor(duration.asMonths()),
      [PrettyTimeUnit.weeks]: Math.floor(duration.asWeeks()),
      [PrettyTimeUnit.days]: Math.floor(duration.asDays()),
      [PrettyTimeUnit.hours]: duration.hours(),
      [PrettyTimeUnit.minutes]: duration.minutes(),
      [PrettyTimeUnit.seconds]: duration.seconds(),
      [PrettyTimeUnit.milliseconds]: duration.milliseconds(),
    };

    return Object.entries(output).reduce((acc, [key, value]) => {
      if (value > 0) {
        return acc + t(`time_short.${key}`, { count: value }) + ' ';
      }
      return acc;
    }, '');
  };

  return {
    formatTime,
    formatDateTime: (time: Date | string | number) =>
      formatDateTime(time, locale.value),
  };
};
