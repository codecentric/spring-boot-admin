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
  <span
    class="w-6 inline-block text-center mr-1 rounded"
    :class="`thread-tag--${threadState.toLowerCase()}`"
    :title="threadState"
  />
</template>

<script>
export default {
  props: {
    threadState: {
      type: String,
      required: true,
    },
  },
};
</script>

<style lang="css" scoped>
.thread-tag--runnable {
  color: black !important;
  background-color: #48c78e !important;
}
.thread-tag--runnable:before {
  content: 'R';
}
.thread-tag--timed_waiting,
.thread-tag--waiting {
  color: black !important;
  background-color: #ffe08a !important;
}
.thread-tag--timed_waiting:before,
.thread-tag--waiting:before {
  content: 'W';
}
.thread-tag--blocked {
  color: #fff !important;
  background-color: #f14668 !important;
}
.thread-tag--blocked:before {
  content: 'B';
}
.thread-tag--terminated {
  color: black !important;
  background-color: #f5f5f5 !important;
}
.thread-tag--terminated:before {
  content: 'T';
}
.thread-tag--new {
  color: black !important;
  background-color: #f5f5f5 !important;
}
.thread-tag--new:before {
  content: 'N';
}
</style>
