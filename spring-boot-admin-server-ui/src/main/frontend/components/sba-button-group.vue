<template>
  <div class="btn-group">
    <slot />
  </div>
</template>

<style scoped>
.btn-group {
  @apply inline-flex z-0 relative -space-x-px;
}

.btn-group .btn,
.btn-group button {
  @apply items-center relative inline-flex rounded-none border focus:z-10;
}

.btn-group:deep(:first-child:not(:last-child)) {
  @apply rounded-r-none !important;
}

.btn-group:deep(:not(:first-child):not(:last-child)) {
  @apply rounded-none !important;
}

.btn-group:deep(:last-child:not(:first-child)) {
  @apply rounded-l-none !important;
}
</style>
