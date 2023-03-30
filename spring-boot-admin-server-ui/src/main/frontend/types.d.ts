import { Component, Raw, RenderFunction } from 'vue';

import ViewRegistry from '@/viewRegistry';

export {};

declare module '*.vue' {
  import type { DefineComponent } from 'vue';
  const component: DefineComponent<
    Record<string, unknown>,
    Record<string, unknown>,
    unknown
  >;
  export default component;
}

declare global {
  type ViewInstallFunctionParams = {
    viewRegistry: ViewRegistry;
  };

  type SbaViewDescriptor = {
    name: string;
    parent: string;
    handle: Component | RenderFunction;
    path: string;
    order: number;
  };

  type SbaView = {
    isEnabled: () => boolean;
    isChildRoute: boolean;
    component: Raw<any>;
    group: string;
    hasChildren: boolean;
  } & SbaViewDescriptor;

  type View = ComponentView | LinkView;

  interface ComponentView {
    name: string;
    path: string;
    label: string;
    order: number;
    component: Component;
  }

  interface LinkView {
    href: string;
    label: string;
    order: number;
  }

  type HealthStatus =
    | 'DOWN'
    | 'UP'
    | 'RESTRICTED'
    | 'UNKNOWN'
    | 'OUT_OF_SERVICE'
    | 'OFFLINE';
}
