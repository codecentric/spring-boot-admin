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
import { Text, UnwrapNestedRefs, h, markRaw, reactive, shallowRef } from 'vue';
import { Router, createRouter, createWebHistory } from 'vue-router';

import sbaConfig from './sba-config';
import { VIEW_GROUP } from './views/ViewGroup.js';

let router: Router;

const createI18nTextVNode = (label) => {
  return shallowRef({
    render() {
      return h(Text, this.$t(label));
    },
  });
};

export default class ViewRegistry {
  private readonly _redirects: any[];
  private readonly _views: UnwrapNestedRefs<SbaView[]>;

  constructor() {
    this._views = reactive([]);
    this._redirects = [];
  }

  get views(): SbaViewDescriptor[] {
    return this._views
      .map((view) => {
        return {
          name: view.name,
          parent: view.parent,
          handle: view.handle,
          path: view.path,
          href: view.href,
          order: view.order,
        } as SbaViewDescriptor;
      })
      .sort((a, b) => a.order - b.order);
  }

  get routes() {
    let routes = this._toRoutes(
      this._views,
      (v) => v.path && (!v.parent || !v.isChildRoute)
    );
    return [...routes, ...this._redirects];
  }

  get router(): Router {
    return router;
  }

  createRouter() {
    console.log(this.routes);
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

  addRedirect(path, redirect) {
    if (typeof redirect === 'string') {
      this._redirects.push({ path, redirect: { name: redirect } });
    } else {
      this._redirects.push({ path, redirect });
    }
  }

  _addView(viewConfig) {
    const view = { ...viewConfig } as SbaView;

    view.parent = viewConfig.parent;
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
        let viewSettings = sbaConfig.uiSettings.viewSettings.find(
          (vs) => vs.name === viewConfig.name
        );
        return !viewSettings || viewSettings.enabled === true;
      };
    } else {
      view.isEnabled = viewConfig.isEnabled;
    }
    this._removeExistingView(view);
    this._views.push(view);
  }

  _removeExistingView(view) {
    remove(this._views, (v) => {
      return v.name === view.name && v.group === view.group;
    });
  }

  _toRoutes(views, filter) {
    return views.filter(filter).map((p) => {
      const children = this._toRoutes(
        views,
        (v) => v.parent === p.name && v.isChildRoute
      );
      return {
        path: p.path,
        name: p.name,
        component: p.component,
        props: p.props,
        meta: { view: p },
        children,
      };
    });
  }
}
