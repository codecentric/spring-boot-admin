import { Ref, ref } from 'vue';

import ApplicationStore from '../store';

import Application from '@/services/application';

let applicationStore: ApplicationStore | null = null;
const applications: Ref<Application[]> = ref([]);
const applicationsInitialized = ref(false);
const error = ref(null);

export function createApplicationStore() {
  if (applicationStore) throw new Error('ApplicationStore already created!');

  applicationStore = new ApplicationStore();
  return applicationStore;
}

type ApplicationStoreValue = {
  applications: Ref<Application[]>;
  applicationsInitialized: Ref<boolean>;
  error: Ref<any>;
  applicationStore: ApplicationStore;
  findApplicationByInstanceId: (instanceId: string) => Ref<Application | null>;
};

export function useApplicationStore(): ApplicationStoreValue {
  if (!applicationStore) {
    throw new Error(
      'ApplicationStore not created yet! Call createApplicationStore() first.',
    );
  }

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

  return {
    applications,
    applicationsInitialized,
    error,
    applicationStore,
  };
}
