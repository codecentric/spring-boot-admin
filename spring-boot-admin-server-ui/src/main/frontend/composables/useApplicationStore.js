import { ref } from 'vue';

import ApplicationStore from '../store.js';

let applicationStore;

export function createApplicationStore() {
  applicationStore = new ApplicationStore();
  return applicationStore;
}

export function useApplicationStore() {
  const applications = ref([]);
  const applicationsInitialized = ref(false);
  const error = ref(null);

  applicationStore.addEventListener('connected', () => {
    applicationsInitialized.value = true;
    error.value = null;
  });

  applicationStore.addEventListener('changed', (newApplications) => {
    applicationsInitialized.value = true;
    applications.value = newApplications;
    error.value = null;
  });

  applicationStore.addEventListener('error', (errorResponse) => {
    applicationsInitialized.value = true;
    error.value = errorResponse;
  });

  return { applications, applicationsInitialized, error, applicationStore };
}
