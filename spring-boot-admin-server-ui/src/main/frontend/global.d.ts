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

  type SbaViewDescriptor = {
    name: string;
    parent: string;
    handle: Component | RenderFunction;
    path?: string;
    href?: string;
    order: number;
  };

  type SbaView = {
    isEnabled: () => boolean;
    isChildRoute: boolean;
    component: Raw<any>;
    group: string;
    hasChildren: boolean;
    props: any;
  } & SbaViewDescriptor;

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
