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

import {VIEW_GROUP} from './views';

const createTextVNode = (label) => {
  return {
    render() {
      return this._v(this.$t(label))
    }
  }
};

export default class ViewRegistry {
  constructor() {
    this._views = [];
    this._redirects = [];
  }

  get views() {
    return this._views;
  }

  get routes() {
    return [
      ...this._toRoutes(this._views, v => v.path && !v.parent),
      ...this._redirects
    ]
  }

  addView(...views) {
    views.forEach(view => this._addView(view));
  }

  addRedirect(path, redirect) {
    if (typeof redirect === 'string') {
      this._redirects.push({path, redirect: {name: redirect}});
    } else {
      this._redirects.push({path, redirect});
    }
  }

  _addView(view) {
    if (view.label && !view.handle) {
      view.handle = createTextVNode(view.label);
    }
    if (!view.group) {
      view.group = VIEW_GROUP.NONE;
    }

    this._views.push(view);
  }

  _toRoutes(views, filter) {
    return views.filter(filter).map(
      p => {
        const children = this._toRoutes(views, v => v.parent === p.name);
        return ({
          path: p.path,
          name: p.name,
          component: p.component,
          props: p.props,
          meta: {view: p},
          children
        });
      }
    )
  }
}
