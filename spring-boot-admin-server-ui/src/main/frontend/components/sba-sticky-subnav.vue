<template>
  <div
    id="subnavigation"
    class="sticky shadow-sm top-14 w-full bg-white py-1 px-2 md:px-6 backdrop-filter backdrop-blur bg-opacity-40 z-20 drop-shadow-lg"
  >
    <slot />
  </div>
</template>

<script>
export default {
  name: 'SbaStickySubnav',
};
</script>
