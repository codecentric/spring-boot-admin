import { ref } from 'vue';

import ApplicationStore from '../store.js';

let applicationStore;
const applications = ref([]);
const applicationsInitialized = ref(false);
const error = ref(null);

export function createApplicationStore() {
  applicationStore = new ApplicationStore();
  console.log('createApplicationStore');
  return applicationStore;
}

export function useApplicationStore() {
  applicationStore.addEventListener('connected', () => {
    console.log('connected');
    applicationsInitialized.value = true;
    error.value = null;
  });

  applicationStore.addEventListener('changed', (newApplications) => {
    console.log('changed');
    applicationsInitialized.value = true;
    applications.value = newApplications;
    error.value = null;
  });

  applicationStore.addEventListener('error', (errorResponse) => {
    console.log('error');
    applicationsInitialized.value = true;
    error.value = errorResponse;
  });

  applicationStore.addEventListener('removed', () => {
    console.log('removed');
    applicationsInitialized.value = false;
  });

  return { applications, applicationsInitialized, error, applicationStore };
}
