import ViewRegistry from '../viewRegistry.js';

let viewRegistry;

export function createViewRegistry() {
  viewRegistry = new ViewRegistry();
  return viewRegistry;
}

export function useViewRegistry() {
  return {
    addView: viewRegistry.addView.bind(viewRegistry),
    views: viewRegistry.views,
  };
}
