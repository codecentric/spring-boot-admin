import ViewRegistry from '../viewRegistry.js';

let viewRegistry;

export function createViewRegistry() {
  if (viewRegistry) throw new Error('ViewRegistry already created!');

  viewRegistry = new ViewRegistry();
  return viewRegistry;
}

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
    },
  };
}
