import { debounce } from 'lodash-es';

import ViewRegistry from '../viewRegistry.js';

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

      if (view.parent) {
        viewRegistry.router.addRoute(view.parent, {
          path: view.path,
          name: view.name,
          component: view.component,
          props: view.props,
          meta: { view: view },
        });
      } else {
        viewRegistry.router.addRoute({
          path: view.path,
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
