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
import FontAwesomeIcon from './font-awesome-icon.js';
import SbaActionButtonScoped from './sba-action-button-scoped.vue';
import SbaAlert from './sba-alert.vue';
import SbaButtonGroup from './sba-button-group.vue';
import SbaButton from './sba-button.vue';
import SbaCheckbox from './sba-checkbox.vue';
import SbaConfirmButton from './sba-confirm-button.vue';
import SbaFormattedObj from './sba-formatted-obj.vue';
import SbaIconButton from './sba-icon-button.vue';
import SbaInput from './sba-input.vue';
import SbaKeyValueTable from './sba-key-value-table.vue';
import SbaLoadingSpinner from './sba-loading-spinner.vue';
import SbaModal from './sba-modal.vue';
import SbaPaginationNav from './sba-pagination-nav.vue';
import SbaPanel from './sba-panel.vue';
import SbaSelect from './sba-select.vue';
import SbaStatusBadge from './sba-status-badge.vue';
import SbaStatus from './sba-status.vue';
import SbaStickySubnav from './sba-sticky-subnav.vue';
import SbaTag from './sba-tag.vue';
import SbaTags from './sba-tags.vue';
import SbaTimeAgo from './sba-time-ago.vue';
import SbaToggleScopeButton from './sba-toggle-scope-button.vue';
import SbaWave from './sba-wave.vue';

export const components = {
  'sba-action-button-scoped': SbaActionButtonScoped,
  'sba-alert': SbaAlert,
  'sba-button-group': SbaButtonGroup,
  'sba-button': SbaButton,
  'sba-checkbox': SbaCheckbox,
  'sba-confirm-button': SbaConfirmButton,
  'sba-formatted-obj': SbaFormattedObj,
  'sba-icon-button': SbaIconButton,
  'sba-input': SbaInput,
  'sba-select': SbaSelect,
  'sba-key-value-table': SbaKeyValueTable,
  'sba-loading-spinner': SbaLoadingSpinner,
  'sba-modal': SbaModal,
  'sba-pagination-nav': SbaPaginationNav,
  'sba-panel': SbaPanel,
  'sba-status-badge': SbaStatusBadge,
  'sba-status': SbaStatus,
  'sba-sticky-subnav': SbaStickySubnav,
  'sba-tag': SbaTag,
  'sba-tags': SbaTags,
  'sba-time-ago': SbaTimeAgo,
  'sba-toggle-scope-button': SbaToggleScopeButton,
  'font-awesome-icon': FontAwesomeIcon,
  'sba-wave': SbaWave,
};

export default {
  install(app) {
    for (const [name, component] of Object.entries(components)) {
      app.component(name, component);
    }
  },
};
