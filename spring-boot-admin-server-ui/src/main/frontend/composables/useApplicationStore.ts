import { ref } from 'vue';

import ApplicationStore from '../store.js';

let applicationStore;
const applications = ref([]);
const applicationsInitialized = ref(false);
const error = ref(null);

export function createApplicationStore() {
  if (applicationStore) throw new Error('ApplicationStore already created!');

  applicationStore = new ApplicationStore();
  return applicationStore;
}

export function useApplicationStore() {
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

  applicationStore.addEventListener('removed', () => {
    applicationsInitialized.value = false;
  });

  return { applications, applicationsInitialized, error, applicationStore };
}
