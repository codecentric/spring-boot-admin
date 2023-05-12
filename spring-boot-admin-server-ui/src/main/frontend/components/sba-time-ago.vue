/* * Copyright 2014-2018 the original author or authors. * * Licensed under the
Apache License, Version 2.0 (the "License"); * you may not use this file except
in compliance with the License. * You may obtain a copy of the License at * *
http://www.apache.org/licenses/LICENSE-2.0 * * Unless required by applicable law
or agreed to in writing, software * distributed under the License is distributed
on an "AS IS" BASIS, * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
express or implied. * See the License for the specific language governing
permissions and * limitations under the License. */
<template>
  {{ timeAgo }}
</template>

<script>
import moment from 'moment';

const minute = 60 * 1000;
const hour = 60 * minute;
const day = 24 * hour;
const week = 7 * day;

function shortFormat(aMoment, withoutPreOrSuffix, now = moment()) {
  let diff = Math.abs(aMoment.diff(now));
  let unit;
  if (diff < minute) {
    unit = 'seconds';
  } else if (diff < hour) {
    unit = 'minutes';
  } else if (diff < day) {
    unit = 'hours';
  } else if (diff < week) {
    unit = 'days';
  } else if (aMoment.year() !== now.year()) {
    return aMoment.format('MMM D, YYYY');
  } else {
    return aMoment.format('MMM D');
  }
  let num = Math.max(1, moment.duration(diff)[unit]());

  let result = num + unit.charAt(0);
  if (!withoutPreOrSuffix) {
    result = moment.localeData().pastFuture(aMoment.diff(now), result);
  }
  return result;
}

export default {
  props: {
    date: {
      type: [String, Date, Number, moment],
      default: null,
    },
  },
  data: () => ({
    now: moment(),
    timer: null,
  }),
  computed: {
    timeAgo() {
      return shortFormat(moment(this.date), true, this.now);
    },
  },
  created() {
    this.timer = window.setInterval(() => {
      this.now = moment();
    }, 1000);
  },
  beforeUnmount() {
    if (this.timer) {
      window.clearInterval(this.timer);
    }
  },
};
</script>
