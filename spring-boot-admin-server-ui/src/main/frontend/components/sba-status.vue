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
        <component :is="icon"
                   class="application-status__icon" :class="`application-status__icon--${status}`"></component>
        <small v-if="date">
            <sba-time-ago :date="date"></sba-time-ago>
        </small>
    </div>
</template>

<script>
  import upIcon from 'material-design-icons/action/svg/production/ic_done_24px.svg';
  import unknownIcon from 'material-design-icons/action/svg/production/ic_help_outline_24px.svg';
  import outOfServiceIcon from 'material-design-icons/content/svg/production/ic_block_24px.svg';
  import downIcon from 'material-design-icons/content/svg/production/ic_clear_24px.svg';
  import offlineIcon from 'material-design-icons/content/svg/production/ic_remove_24px.svg';
  import restrictedIcon from 'material-design-icons/content/svg/production/ic_report_24px.svg';
  import sbaTimeAgo from './sba-time-ago';

  const icons = {
    'UP': upIcon,
    'RESTRICTED': restrictedIcon,
    'OUT_OF_SERVICE': outOfServiceIcon,
    'DOWN': downIcon,
    'OFFLINE': offlineIcon,
    'UNKNOWN': unknownIcon
  };

  export default {
    components: {
      sbaTimeAgo,
      upIcon,
      restrictedIcon,
      outOfServiceIcon,
      downIcon,
      offlineIcon,
      unknownIcon
    },
    props: {
      status: String,
      date: {},
    },
    computed: {
      icon() {
        return icons[this.status];
      }
    }
  }
</script>

<style lang="scss">
    .application-status {
        text-align: center;
        line-height: 1rem;
        display: inline-flex;
        flex-direction: column;

        &__icon {
            fill: gray;
            margin: 0 auto;

            &--UP {
                fill: green;
            }

            &--RESTRICTED {
                fill: orange;
            }

            &--OUT_OF_SERVICE,
            &--DOWN {
                fill: red;
            }

            &--UNKNOWN,
            &--OFFLINE {
                fill: grey;
            }
        }
    }
</style>