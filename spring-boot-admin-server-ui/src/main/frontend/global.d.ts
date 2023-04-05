import { Component, Raw, RenderFunction } from 'vue';

import ViewRegistry from '@/viewRegistry';

export {};

declare global {
  interface Window {
    SBA: any;
  }

  type ViewInstallFunctionParams = {
    viewRegistry: ViewRegistry;
  };

  type SbaView = {
    id: string;
    name?: string;
    parent: string;
    handle: string | Component | RenderFunction;
    path?: string;
    href?: string;
    order: number;
    isEnabled: () => boolean;
    isChildRoute: boolean;
    component: Raw<any>;
    group: string;
    hasChildren: boolean;
    props: any;
  };

  type View = ComponentView | LinkView;

  interface ComponentView {
    name: string;
    path: string;
    label: string;
    order?: number;
    group?: string;
    component: Component;
  }

  interface LinkView {
    href: string;
    label: string;
    order?: number;
  }

  type HealthStatus =
    | 'DOWN'
    | 'UP'
    | 'RESTRICTED'
    | 'UNKNOWN'
    | 'OUT_OF_SERVICE'
    | 'OFFLINE';
}
