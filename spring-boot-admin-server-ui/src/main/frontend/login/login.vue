<template>
  <form
    method="post"
    class="w-5/6 md:1/2 max-w-lg"
  >
    <sba-panel>
      <input
        v-if="csrf"
        type="hidden"
        :name="csrf.parameterName"
        :value="csrf.token"
      >
      <div class="flex text-lg pb-3 items-center">
        <img
          v-if="icon"
          class="w-8 h-8 mr-2"
          :src="icon"
        >
        <h1
          class="title has-text-primary"
          v-text="title"
        />
      </div>
      <div class="relative border-t -ml-4 -mr-4 overflow-hidden">
        <sba-wave
          class="bg-wave--login"
        />
        <div class="ml-4 mr-4 pt-2 z-10 relative">
          <sba-alert
            :error="error"
          />
          <sba-alert
            :severity="Severity.INFO"
            :error="logout"
          />
          <div
            class="pb-4 form-group"
            :class="{'has-errors': error}"
          >
            <sba-input
              type="text"
              name="username"
              :label="$t('login.placeholder.username')"
            />
            <sba-input
              type="password"
              name="password"
              autocomplete="current-password"
              :label="$t('login.placeholder.password')"
            />
          </div>
        </div>
      </div>

      <template #footer>
        <div class="text-right">
          <sba-button>
            {{ $t('login.button_login') }}
          </sba-button>
        </div>
      </template>
    </sba-panel>
  </form>
</template>

<script>
import SbaPanel from "../components/sba-panel.vue";
import SbaInput from "../components/sba-input.vue";
import SbaButton from "../components/sba-button.vue";
import SbaAlert, {Severity} from "../components/sba-alert.vue";
import SbaWave from "../components/sba-wave.vue";

export default {
  components: {SbaWave, SbaAlert, SbaButton, SbaInput, SbaPanel},
  props: {
    param: {
      type: Object,
      default: () => ({})
    },
    icon: {
      type: String,
      default: undefined
    },
    title: {
      type: String,
      required: true
    },
    csrf: {
      type: Object,
      default: undefined
    },
    theme: {
      type: Object,
      default: undefined
    }
  },
  data() {
    return {
      Severity,
      background: this.theme?.background,
    }
  },
  computed: {
    error() {
      return (this.param.error !== undefined) ? this.$t('login.invalid_username_or_password') : undefined;
    },
    logout() {
      return (this.param.logout !== undefined) ? this.$t('login.logout_successful') : undefined;
    }
  }
}
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
