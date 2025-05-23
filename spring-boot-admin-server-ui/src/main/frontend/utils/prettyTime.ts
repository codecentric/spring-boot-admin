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

  const formatDateTime = (time: Date | string) => {
    return new Intl.DateTimeFormat(locale.value, {
      dateStyle: 'medium',
      timeStyle: 'medium',
    }).format(new Date(time));
  };

  return {
    formatTime,
    formatDateTime,
  };
};
