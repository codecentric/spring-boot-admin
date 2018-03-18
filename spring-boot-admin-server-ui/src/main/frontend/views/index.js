/*
 * Copyright 2014-2018 the original author or authors.
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

import {view as aboutView} from './about';
import {view as applicationView} from './applications';
import instanceViews from './instances';
import {view as journalView} from './journal';
import {view as wallboardView} from './wallboard';

export default router => {
  const views = [];
  views.register = view => {
    if (view.handle) {
      if (typeof view.handle === 'string') {
        const label = view.handle;
        view.handle = {
          render() {
            return this._v(label)
          }
        }
      }
      views.push(view);
    }

    if (view.component) {
      router.addRoutes([{
          path: view.path,
          children: view.children,
          component: view.component,
          props: view.props,
          name: view.name
        }]
      )
    }
  };

  views.register(applicationView);
  views.register(journalView);
  views.register(aboutView);
  views.register(wallboardView);
  instanceViews.forEach(views.register);
  views.sort((a, b) => a.order - b.order);

  router.addRoutes([{path: '/', redirect: {name: 'applications'}}]);

  return views;
}
