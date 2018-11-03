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
  <div class="application-status">
    <font-awesome-icon :icon="icon" class="application-status__icon"
                       :class="`application-status__icon--${status}`"
    />
    <small v-if="date">
      <sba-time-ago :date="date" />
    </small>
  </div>
</template>

<script>
  import moment from 'moment';
  import sbaTimeAgo from './sba-time-ago';

  const icons = {
    'UP': 'check',
    'RESTRICTED': 'exclamation',
    'OUT_OF_SERVICE': 'ban',
    'DOWN': 'times-circle',
    'OFFLINE': 'minus-circle',
    'UNKNOWN': 'question-circle'
  };

  export default {
    // eslint-disable-next-line vue/no-unused-components
    components: {sbaTimeAgo},
    props: {
      status: {
        type: String,
        default: 'UNKNOWN'
      },
      date: {
        type: [String, Date, Number, moment],
        default: null
      },
    },
    computed: {
      icon() {
        return icons[this.status];
      }
    }
  }
</script>

<style lang="scss">
  @import "~@/assets/css/utilities";

  .application-status {
    text-align: center;
    line-height: 1rem;
    display: inline-flex;
    flex-direction: column;

    &__icon {
      color: gray;
      margin: 0 auto;

      &--UP {
        color: $success;
      }

      &--RESTRICTED {
        color: $warning;
      }

      &--OUT_OF_SERVICE,
      &--DOWN {
        color: $danger;
      }

      &--UNKNOWN,
      &--OFFLINE {
        color: $grey;
      }
    }
  }
</style>
