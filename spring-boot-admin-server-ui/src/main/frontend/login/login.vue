<template>
  <form class="w-5/6 md:1/2 max-w-lg" method="post">
    <sba-panel>
      <input
        v-if="csrf"
        :name="csrf.parameterName"
        :value="csrf.token"
        type="hidden"
      />
      <div class="flex text-lg pb-3 items-center">
        <img v-if="icon" :src="icon" class="w-8 h-8 mr-2" />
        <h1 class="title has-text-primary" v-text="title" />
      </div>
      <div class="relative border-t -ml-4 -mr-4 overflow-hidden">
        <sba-wave class="bg-wave--login" />
        <div class="ml-4 mr-4 pt-2 z-10 relative">
          <sba-alert :error="error" />
          <sba-alert :error="logout" :severity="Severity.INFO" />
          <div :class="{ 'has-errors': error }" class="pb-4 form-group">
            <sba-input
              :label="t('login.placeholder.username')"
              autocomplete="username"
              name="username"
              type="text"
              autofocus
            />
            <sba-input
              :label="t('login.placeholder.password')"
              autocomplete="current-password"
              name="password"
              type="password"
            />
            <sba-checkbox
              v-if="rememberMeEnabled"
              :label="t('login.remember_me')"
              class="justify-end"
              name="remember-me"
            />
          </div>
        </div>
      </div>

      <template #footer>
        <div class="text-right">
          <sba-button>
            {{ t('login.button_login') }}
          </sba-button>
        </div>
      </template>
    </sba-panel>
  </form>
</template>

<script setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import SbaAlert, { Severity } from '@/components/sba-alert';
import SbaButton from '@/components/sba-button';
import SbaCheckbox from '@/components/sba-checkbox';
import SbaInput from '@/components/sba-input';
import SbaPanel from '@/components/sba-panel';
import SbaWave from '@/components/sba-wave';

const i18n = useI18n();
const t = i18n.t;

const props = defineProps({
  param: {
    type: Object,
    default: () => ({}),
  },
  icon: {
    type: String,
    default: undefined,
  },
  title: {
    type: String,
    required: true,
  },
  csrf: {
    type: Object,
    default: undefined,
  },
  theme: {
    type: Object,
    default: undefined,
  },
});

const { rememberMeEnabled } = window.uiSettings;

const error = computed(() => {
  let errors = props.param.error;

  if (Array.isArray(errors)) {
    if (errors.includes('401')) {
      return t('login.error.login_required', { code: errors[0] });
    } else {
      return t('login.error.invalid_username_or_password');
    }
  } else {
    return undefined;
  }
});

const logout = computed(() => {
  return props.param.logout !== undefined
    ? t('login.logout_successful')
    : undefined;
});
</script>

<style scoped>
.bg-wave--login {
  @apply z-0 absolute left-0;
  min-width: 100%;
  height: 4rem;
}

.form-group {
  @apply grid grid-cols-1 gap-2;
}

.form-group.has-errors label {
  @apply text-red-500;
}

.form-group.has-errors input {
  @apply focus:ring-red-500 focus:border-red-500 border-red-400;
}
</style>
