/*
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { remove } from 'lodash-es';
import { Text, VNode, h, markRaw, reactive, shallowRef } from 'vue';
import { Router, createRouter, createWebHistory } from 'vue-router';

import sbaConfig from './sba-config';
import { VIEW_GROUP } from './views/ViewGroup.js';

let router: Router;

const createI18nTextVNode = (label: string) =>
  shallowRef({
    render(): VNode {
      return h(Text, this.$t(label));
    },
  });

// eslint-disable-next-line no-unused-vars
type ViewFilterFunction = (view: SbaView) => boolean;
type ViewConfig = {
  [key: string]: any;
};

export default class ViewRegistry {
  private readonly _redirects: any[] = [];

  private _views: SbaView[] = reactive([]);

  get views(): SbaView[] {
    return this._views;
  }

  get routes() {
    const routes = this._toRoutes(
      (view) => view.path && (!view.parent || !view.isChildRoute)
    );
    return [...routes, ...this._redirects];
  }

  get router(): Router {
    return router;
  }

  createRouter() {
    const routesKnownToBackend = sbaConfig.uiSettings.routes.map(
      (r) => new RegExp(`^${r.replace('/**', '(/.*)?')}$`)
    );
    const unknownRoutes = this.routes.filter(
      (vr) =>
        vr.path !== '/' && !routesKnownToBackend.some((br) => br.test(vr.path))
    );
    if (unknownRoutes.length > 0) {
      console.warn(
        `The routes ${JSON.stringify(
          unknownRoutes.map((r) => r.path)
        )} aren't known to the backend and may be not properly routed!`
      );
    }

    router = createRouter({
      history: createWebHistory(),
      linkActiveClass: 'is-active',
      routes: this.routes,
    });
    return router;
  }

  getViewByName(name) {
    return Array.prototype.find.call(this._views, (v) => v.name === name);
  }

  addView(...views: View[]) {
    views.forEach((view) => this._addView(view));
  }

  addRedirect(path: string, redirect: string | object) {
    if (typeof redirect === 'string') {
      this._redirects.push({ path, redirect: { name: redirect } });
    } else {
      this._redirects.push({ path, redirect });
    }
  }

  _addView(viewConfig: ViewConfig) {
    const view = { ...viewConfig } as SbaView;
    view.hasChildren = !!viewConfig.children;

    if (!viewConfig.name) {
      view.name = [viewConfig.parent, viewConfig.path]
        .filter((p) => !!p)
        .join('/');
    }

    if (viewConfig.label && !viewConfig.handle) {
      view.handle = createI18nTextVNode(viewConfig.label);
    }

    if (viewConfig.handle) {
      view.handle = markRaw(viewConfig.handle);
    }

    if (!viewConfig.group) {
      view.group = VIEW_GROUP.NONE;
    }
    if (viewConfig.component) {
      view.component = markRaw(viewConfig.component);
    }
    view.isChildRoute = viewConfig.isChildRoute === undefined;

    if (!viewConfig.isEnabled) {
      view.isEnabled = () => {
        const viewSettings = sbaConfig.uiSettings.viewSettings.find(
          (vs) => vs.name === viewConfig.name
        );
        return !viewSettings || viewSettings.enabled === true;
      };
    } else {
      view.isEnabled = viewConfig.isEnabled;
    }

    this._views.push(view);
  }

  _removeExistingView(view) {
    remove(this._views, (v) => {
      return v.name === view.name && v.group === view.group;
    });
  }

  _toRoutes(filter: ViewFilterFunction) {
    return this._views.filter(filter).map((view) => {
      const children = this._toRoutes(
        (childView) => childView.parent === view.name && childView.isChildRoute
      );

      return {
        path: view.path,
        name: view.name,
        component: view.component,
        props: view.props,
        meta: { view: view },
        children,
      };
    });
  }
}
