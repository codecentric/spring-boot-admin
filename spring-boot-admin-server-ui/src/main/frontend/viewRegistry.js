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
import { h, markRaw, reactive } from 'vue';

import sbaConfig from './sba-config.js';
import { VIEW_GROUP } from './views/ViewGroup.js';

const createI18nTextVNode = (label) => {
  return {
    render() {
      const value = this.$t(label);
      return h('span', value);
    },
  };
};

export default class ViewRegistry {
  constructor() {
    this._views = reactive([]);
    this._redirects = [];
  }

  get views() {
    return this._views;
  }

  get routes() {
    return [
      ...this._toRoutes(
        this._views,
        (v) => v.path && (!v.parent || !v.isChildRoute)
      ),
      ...this._redirects,
    ];
  }

  getViewByName(name) {
    return Array.prototype.find.call(this._views, (v) => v.name === name);
  }

  addView(...views) {
    views.forEach((view) => this._addView(view));
  }

  addRedirect(path, redirect) {
    if (typeof redirect === 'string') {
      this._redirects.push({ path, redirect: { name: redirect } });
    } else {
      this._redirects.push({ path, redirect });
    }
  }

  _addView(view) {
    if (view.label && !view.handle) {
      view.handle = createI18nTextVNode(view.label);
    }
    if (!view.group) {
      view.group = VIEW_GROUP.NONE;
    }
    if (!view.name) {
      view.name = [view.parent, view.path].filter((p) => !!p).join('/');
    }
    if (view.handle) {
      view.handle = markRaw(view.handle);
    }
    if (view.component) {
      view.component = markRaw(view.component);
    }
    if (view.isChildRoute === undefined) {
      view.isChildRoute = true;
    }

    if (!view.isEnabled) {
      view.isEnabled = () => {
        let viewSettings = sbaConfig.uiSettings.viewSettings.find(
          (vs) => vs.name === view.name
        );
        return !viewSettings || viewSettings.enabled === true;
      };
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
        component: markRaw(p.component),
        props: p.props,
        meta: { view: p },
        children,
      };
    });
  }
}
