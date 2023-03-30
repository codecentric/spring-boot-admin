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

const addIframeView = (viewRegistry: ViewRegistry, { url, label, order }) => {
  const urlWithoutScheme = url.replace(/^https?:[/][/]/, '');
  viewRegistry.addView({
    name: `external/${urlWithoutScheme}`,
    path: `/external/${urlWithoutScheme.replace(/[^a-zA-Z]+/g, '-')}`,
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

const addExternalLink = (viewRegistry: ViewRegistry, { url, label, order }) => {
  viewRegistry.addView({
    href: url,
    label,
    order,
  } as LinkView);
};

export default {
  install({ viewRegistry }) {
    sbaConfig.uiSettings.externalViews.forEach((view) => {
      if (view.iframe) {
        addIframeView(viewRegistry, view);
      } else {
        addExternalLink(viewRegistry, view);
      }
    });
  },
};
