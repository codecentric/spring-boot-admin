import { Component, Raw, RenderFunction } from 'vue';

import ViewRegistry from '@/viewRegistry';

export {};

declare global {
  interface Window {
    SBA: SBASettings;
  }

  type JSExtension = {
    resourcePath: string;
    resourceLocation: string;
  };

  type UITheme = {
    color: string;
    palette: {
      shade50: string;
      shade100: string;
      shade200: string;
      shade300: string;
      shade400: string;
      shade500: string;
      shade600: string;
      shade700: string;
      shade800: string;
      shade900: string;
    };
  };

  type PollTimer = {
    cache: number;
    datasource: number;
    gc: number;
    process: number;
    memory: number;
    threads: number;
  };

  type ViewSettings = {
    name: string;
    enabled: boolean;
  };

  type ExternalView = {
    label: string;
    url: string;
    order: number;
    iframe: boolean;
    children: ExternalView[];
  };

  type UISettings = {
    title: string;
    brand: string;
    loginIcon: string;
    favicon: string;
    faviconDanger: string;
    pollTimer: PollTimer;
    uiTheme: UITheme;
    notificationFilterEnabled: boolean;
    rememberMeEnabled: boolean;
    availableLanguages: string[];
    routes: string[];
    externalViews: ExternalView[];
    viewSettings: ViewSettings[];
    enableToasts: boolean;
  };

  type SBASettings = {
    uiSettings: UISettings;
    user: {
      name: string;
      [key: string]: any;
    };
    extensions: JSExtension[];
    csrf: {
      headerName: string;
      parameterName: string;
    };
    [key: string]: any;
  };

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
    label?: string;
    handle?: Component | RenderFunction;
    order?: number;
    group?: string;
    component: Component;
    isEnabled?: () => boolean;
  }

  interface LinkView {
    href?: string;
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
