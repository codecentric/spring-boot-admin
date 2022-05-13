import {onBeforeMount, onBeforeUnmount, ref} from 'vue'
import ApplicationStore from "../store.js";

let applicationStore;

export function createApplicationStore() {
  applicationStore = new ApplicationStore();
  return applicationStore;
}

export function useApplicationStore() {
  const applications = ref(applicationStore.applications)
  const applicationsInitialized = ref(false)
  const error = ref(null)

  applicationStore.addEventListener('connected', () => {
    applicationsInitialized.value = true;
    error.value = null;
  });

  applicationStore.addEventListener('changed', (newApplications) => {
    applications.value = newApplications;
    applicationsInitialized.value = true;
    error.value = null;
  });

  applicationStore.addEventListener('error', (errorResponse) => {
    applicationsInitialized.value = true;
    error.value = errorResponse;
  });

  onBeforeMount(() => {
    return applicationStore.start();
  });

  onBeforeUnmount(() => {
    return applicationStore.stop();
  });

  return {applications, applicationsInitialized, error};
}
