/*
 * Copyright 2014-2020 the original author or authors.
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
import { h } from 'vue';

import './style.css';

import sbaConfig from '@/sba-config';
import ViewRegistry from '@/viewRegistry';

function addExternalView(
  viewRegistry: ViewRegistry,
  view: ExternalView,
  parent?: string,
) {
  if (view.iframe) {
    addIframeView(viewRegistry, view, parent);
  } else {
    addExternalLink(viewRegistry, view, parent);
  }
}

function getViewOpts(view: ExternalView, parent?: string) {
  const safeLabel = view.label.replace(/[^a-zA-Z0-9-_]/g, '');
  const name = `/external/${safeLabel}`;

  return {
    name,
    path: name,
    parent,
    label: view.label,
    order: view.order,
  };
}

export const addIframeView = (
  viewRegistry: ViewRegistry,
  view: ExternalView,
  parent?: string,
) => {
  const viewOpts = {
    ...getViewOpts(view, parent),
    component: {
      inheritAttrs: false,
      render() {
        return h('div', { class: 'external-view' }, [
          h('iframe', { src: view.url }),
        ]);
      },
    },
  } as ComponentView;

  viewRegistry.addView(viewOpts);

  view.children?.forEach((view) => {
    addExternalView(viewRegistry, view, viewOpts.name);
  });
};

export const addExternalLink = (
  viewRegistry: ViewRegistry,
  view: ExternalView,
  parent?: string,
) => {
  const viewOpts = {
    ...getViewOpts(view, parent),
    href: view.url,
  } as LinkView;

  viewRegistry.addView(viewOpts);

  view.children?.forEach((view) => {
    addExternalView(viewRegistry, view, viewOpts.name);
  });
};

export default {
  install({ viewRegistry }) {
    const views = sbaConfig.uiSettings.externalViews;
    views.forEach((view) => {
      addExternalView(viewRegistry, view);
    });
  },
};
