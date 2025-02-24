<!--
  - Copyright 2014-2018 the original author or authors.
  -
  - Licensed under the Apache License, Version 2.0 (the "License");
  - you may not use this file except in compliance with the License.
  - You may obtain a copy of the License at
  -
  -     http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS,
  - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
  -->

<template>
  <div :aria-label="status" class="application-status">
    <font-awesome-icon
      :class="`application-status__icon--${status}`"
      :icon="icon"
      class="application-status__icon"
    />
    <small v-if="date" class="hidden md:block">
      <sba-time-ago :date="date" />
    </small>
  </div>
</template>

<script>
import moment from 'moment';

import sbaTimeAgo from '@/components/sba-time-ago';

const icons = {
  UP: 'check-circle',
  RESTRICTED: 'exclamation',
  OUT_OF_SERVICE: 'ban',
  DOWN: 'times-circle',
  OFFLINE: 'minus-circle',
  UNKNOWN: 'question-circle',
};

export default {
  components: { sbaTimeAgo },
  props: {
    status: {
      type: String,
      default: 'UNKNOWN',
    },
    date: {
      type: [String, Date, Number, moment],
      default: null,
    },
  },
  computed: {
    icon() {
      return icons[this.status];
    },
  },
};
</script>

<style>
.application-status {
  @apply text-center inline-flex flex-col;
}
.application-status__icon {
  @apply text-gray-500 mx-auto;
}
.application-status__icon--UP {
  color: #48c78e;
}
.application-status__icon--RESTRICTED {
  color: #ffe08a;
}
.application-status__icon--OUT_OF_SERVICE,
.application-status__icon--DOWN {
  color: #f14668;
}
.application-status__icon--UNKNOWN,
.application-status__icon--OFFLINE {
  color: #7a7a7a;
}
</style>
