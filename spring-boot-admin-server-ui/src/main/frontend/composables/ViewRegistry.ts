import { debounce } from 'lodash-es';

import ViewRegistry from '../viewRegistry';

import eventBus from '@/services/bus';

export const CUSTOM_ROUTES_ADDED_EVENT = 'custom-routes-added';

let viewRegistry: ViewRegistry;

export function createViewRegistry() {
  if (viewRegistry) throw new Error('ViewRegistry already created!');

  viewRegistry = new ViewRegistry();
  return viewRegistry;
}

const emitCustomRouteAddedEvent = debounce(() => {
  eventBus.emit(CUSTOM_ROUTES_ADDED_EVENT);
});

export function useViewRegistry() {
  return {
    views: viewRegistry.views,
    setGroupIcon(name, icon) {
      viewRegistry.setGroupIcon(name, icon);
    },
    addView(viewToAdd) {
      const view = viewRegistry.addView(viewToAdd)[0];

      // If router doesn't exist yet, the view will be added when createRouter() is called
      // via viewRegistry.routes which includes all views
      if (!viewRegistry.router) {
        return;
      }

      // Vue Router 5 requires paths to start with / for top-level routes
      const ensureAbsolutePath = (path: string) =>
        path.startsWith('/') ? path : `/${path}`;

      // Check if parent route exists in the router
      const parentRouteExists =
        view.parent && viewRegistry.router.hasRoute(view.parent);

      if (parentRouteExists) {
        viewRegistry.router.addRoute(view.parent, {
          path: view.path,
          name: view.name,
          component: view.component,
          props: view.props,
          meta: { view: view },
        });
      } else {
        // Add as top-level route when no parent or parent doesn't exist yet
        const path = view.parent
          ? ensureAbsolutePath(
              `${view.parent}/${view.path}`.replace(/\/+/g, '/'),
            )
          : ensureAbsolutePath(view.path);
        viewRegistry.router.addRoute({
          path,
          name: view.name,
          component: view.component,
          props: view.props,
          meta: { view: view },
        });
      }

      emitCustomRouteAddedEvent();
    },
    getViewByName(name: string) {
      return viewRegistry.views.find((view) => view.name === name);
    },
  };
}
