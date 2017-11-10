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
    <div class="pageloader is-loading  ">
    </div>
</template>

<script>
  import searchIcon from 'material-design-icons/action/svg/production/ic_search_24px.svg';

  export default {
    components: {
      searchIcon
    },

    props: {
      value: String,
    }
  }
</script>

<style lang="scss">
    @import "~@/assets/css/utilities";

    .pageloader {
        width: 100%;
        height: 100%;
        opacity: 0.5;
        background: $dark;
        transition: all $easing $speed;
        will-change: opacity;

        .is-active {
        }
    }
</style>
