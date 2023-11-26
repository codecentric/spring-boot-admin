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

export const addIframeView = (
  viewRegistry: ViewRegistry,
  { url, label, order }: Omit<ExternalView, 'children'>,
) => {
  const urlWithoutScheme = url.replace(/^https?:[/][/]/, '');
  viewRegistry.addView({
    name: `external/${label}`,
    path: `/external/${encodeURIComponent(urlWithoutScheme)}`,
    label,
    order,
    component: {
      inheritAttrs: false,
      render() {
        return h('div', { class: 'external-view' }, [
          h('iframe', { src: url }),
        ]);
      },
    },
  } as ComponentView);
};

export const addExternalLink = (
  viewRegistry: ViewRegistry,
  { url, label, order, children }: Omit<ExternalView, 'iframe'>,
  parent?: string,
) => {
  const name = `external/${label}`;

  viewRegistry.addView({
    href: url,
    name,
    parent,
    label,
    order,
  } as LinkView);

  children?.forEach((child) => {
    addExternalLink(viewRegistry, child, name);
  });
};

export default {
  install({ viewRegistry }) {
    const externalViews = sbaConfig.uiSettings.externalViews;
    externalViews.forEach((view) => {
      if (view.iframe) {
        addIframeView(viewRegistry, view);
      } else {
        addExternalLink(viewRegistry, view);
      }
    });
  },
};
