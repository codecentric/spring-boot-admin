<template>
  <div
    id="subnavigation"
    class="sticky shadow top-14 w-full bg-white/40 py-1 px-2 md:px-6 backdrop-filter backdrop-blur z-20 drop-shadow-lg"
  >
    <slot />
  </div>
</template>

<script>
export default {
  name: 'SbaStickySubnav',
};
</script>
