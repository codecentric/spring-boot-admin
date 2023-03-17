import ViewRegistry from '../viewRegistry.js';
import eventBus from '@/services/bus';
import { debounce } from 'lodash-es';

export const CUSTOM_ROUTES_ADDED_EVENT = 'custom-routes-added';

let viewRegistry;

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
    addView(view) {
      viewRegistry.addView(view);

      if (view.parent && view.isChildRoute) {
        viewRegistry.router.addRoute(view.parent, {
          path: view.path,
          name: view.name,
          component: view.component,
          props: view.props,
          meta: { view: view }
        });
      } else {
        viewRegistry.router.addRoute({
          path: view.path,
          name: view.name,
          component: view.component,
          props: view.props,
          meta: { view: view }
        });
      }

      emitCustomRouteAddedEvent();
    }
  };
}
