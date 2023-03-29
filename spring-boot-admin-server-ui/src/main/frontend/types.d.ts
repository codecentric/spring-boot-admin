import { Component, Raw, RenderFunction } from 'vue';

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
  type View = InternalView | ExternalView;

  type SbaView = {
    parent: string;
    isEnabled: () => boolean;
    isChildRoute: boolean;
    component: Raw<any>;
    name: string;
    group: string;
    hasChildren: boolean;
    handle: Component | RenderFunction;
  };

  interface InternalView {
    name: string;
    path: string;
    order: number;

    [key: string]: any;
  }

  interface ExternalView {
    href: string;
  }

  type HealthStatus =
    | 'DOWN'
    | 'UP'
    | 'RESTRICTED'
    | 'UNKNOWN'
    | 'OUT_OF_SERVICE'
    | 'OFFLINE';
}
