import { createRouter, createWebHistory } from 'vue-router';

import sbaConfig from './sba-config';

const router = (routes, sbaConfigRoutes = sbaConfig.uiSettings.routes) => {
  const routesKnownToBackend = sbaConfigRoutes.map(
    (r) => new RegExp(`^${r.replace('/**', '(/.*)?')}$`)
  );
  const unknownRoutes = routes.filter(
    (vr) =>
      vr.path !== '/' && !routesKnownToBackend.some((br) => br.test(vr.path))
  );
  if (unknownRoutes.length > 0) {
    console.warn(
      `The routes ${JSON.stringify(
        unknownRoutes.map((r) => r.path)
      )} aren't known to the backend and may be not properly routed!`
    );
  }

  return createRouter({
    history: createWebHistory(),
    linkActiveClass: 'is-active',
    routes,
  });
};

export default router;
