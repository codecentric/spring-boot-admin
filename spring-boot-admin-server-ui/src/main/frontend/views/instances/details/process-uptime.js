/*
 * Copyright 2014-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import subscribing from '@/mixins/subscribing';
import {timer} from '@/utils/rxjs';
import moment from 'moment';

export default {
  props: ['value'],
  mixins: [subscribing],
  data: () => ({
    startTs: null,
    offset: null
  }),
  render() {
    return this._v(this.clock);
  },
  computed: {
    clock() {
      if (!this.value) {
        return null;
      }
      const duration = moment.duration(this.value + this.offset);
      return `${Math.floor(duration.asDays())}d ${duration.hours()}h ${duration.minutes()}m ${duration.seconds()}s`;
    }
  },
  watch: {
    value: 'subscribe'
  },
  methods: {
    createSubscription() {
      if (this.value) {
        const vm = this;
        vm.startTs = moment();
        vm.offset = 0;
        return timer(0, 1000).subscribe({
          next: () => {
            vm.offset = moment().valueOf() - vm.startTs.valueOf();
          }
        })
      }
    }
  }
}
