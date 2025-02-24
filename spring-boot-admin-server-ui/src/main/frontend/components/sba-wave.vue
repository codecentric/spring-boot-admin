<template>
  <div v-if="backgroundEnabled" class="bg-wave">
    <svg
      height="100%"
      preserveAspectRatio="none"
      viewBox="0 0 2000 240"
      width="100%"
      xmlns="http://www.w3.org/2000/svg"
      xmlns:xlink="http://www.w3.org/1999/xlink"
    >
      <defs>
        <linearGradient
          :id="'bg-wave-a-' + id"
          gradientTransform="matrix(1680, 0, 0, -916.53, -3571233, 453859.52)"
          gradientUnits="userSpaceOnUse"
          x1="2125.74"
          x2="2126.93"
          y1="495.08"
          y2="495.08"
        >
          <stop class="bg-color-start" offset="0" />
          <stop class="bg-color-stop" offset="1" />
        </linearGradient>
        <linearGradient
          :id="'bg-wave-b-' + id"
          gradientTransform="matrix(1680, 0, 0, -925.87, -3571233, 458484.7)"
          gradientUnits="userSpaceOnUse"
          x1="2125.74"
          x2="2126.93"
          y1="495.08"
          y2="495.08"
        >
          <stop class="bg-color-start" offset="0" />
          <stop class="bg-color-stop" offset="1" />
        </linearGradient>
        <linearGradient
          :id="'bg-wave-c-' + id"
          gradientTransform="matrix(1680, 0, 0, -956.3, -3571233, 473553.87)"
          gradientUnits="userSpaceOnUse"
          x1="2125.74"
          x2="2126.93"
          y1="495.07"
          y2="495.07"
        >
          <stop class="bg-color-start" offset="0" />
          <stop class="bg-color-stop" offset="1" />
        </linearGradient>
      </defs>
      <path
        :style="`fill:url('#bg-wave-a-${id}')`"
        d="M0,0V142.67q200,73.8,400,42.5T800,139q200-15,400,36.8t400-5.6q200-57.45,400-18.9V0Z"
        style="opacity: 0.2"
      />
      <path
        :style="`fill:url('#bg-wave-b-${id}')`"
        d="M0,0V152.67q200,64.5,800,29.5a1115.6,1115.6,0,0,1,400,2.4q200,37.35,400,19.1a3681.66,3681.66,0,0,1,400-14.6q200,3.6,400-45.5V0Z"
        style="opacity: 0.2"
      />
      <path
        :style="`fill:url('#bg-wave-c-${id}')`"
        d="M0,0V180.77q200,7.35,400-34.1T800,190q200,84.75,400-8.4t400,11.9q200,105,400-26.9V0Z"
        style="opacity: 0.2"
      />
    </svg>
  </div>
</template>

<script>
import sbaConfig from '@/sba-config';

export default {
  name: 'SbaWave',
  data() {
    return {
      id: this._.uid,
      backgroundEnabled: sbaConfig.uiSettings.theme.backgroundEnabled,
    };
  },
};
</script>

<style scoped>
.bg-wave {
  @apply w-full h-40 fixed;
  z-index: -10;
  pointer-events: none;
}
</style>
