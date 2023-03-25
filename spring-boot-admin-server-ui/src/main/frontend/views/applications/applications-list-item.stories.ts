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
import withVueRouter from 'storybook-vue3-router';

import SbaPanel from '../../components/sba-panel.vue';
import { applications } from '../../mocks/applications/data.js';
import Application from '../../services/application.js';
import ApplicationsListItem from './applications-list-item.vue';

const application = applications[0];
const routes = [{ path: '/journal', name: 'journal' }];

const firstInstance = { ...application.instances[0] };
const secondInstance = { ...application.instances[0] };
secondInstance.statusTimestamp = Date.now();

const instanceWithoutRestart = { ...firstInstance };
instanceWithoutRestart.endpoints = instanceWithoutRestart.endpoints.filter(
  (e) => e.id === 'restart'
);
const instanceWithABunchOfTags = { ...firstInstance };
instanceWithABunchOfTags.tags = {
  tag0: 'Tag value',
  tag1: 'Tag value',
  tag2: 'Tag value',
  tag3: 'Tag value',
  tag4: 'Tag value',
  tag5: 'Tag value',
};

export default {
  component: ApplicationsListItem,
  title: 'Views/Applications/ApplicationListItem',
};

const Template = (args) => ({
  components: { ApplicationsListItem, SbaPanel },
  setup() {
    return {
      args,
    };
  },
  template: `
    <sba-panel :seamless="true">
    <applications-list-item v-bind="args"/>
    </sba-panel>`,
});

export const OneInstance = Template.bind({});
OneInstance.decorators = [withVueRouter(routes)];
OneInstance.args = {
  application: new Application(application),
};

export const OneInstanceExpanded = Template.bind({});
OneInstance.decorators = [withVueRouter(routes)];
OneInstanceExpanded.args = {
  ...OneInstance.args,
  isExpanded: true,
};

export const MultipleInstances = Template.bind({});
MultipleInstances.decorators = [withVueRouter(routes)];

MultipleInstances.args = {
  application: new Application({
    ...application,
    instances: [firstInstance, secondInstance],
  }),
};

export const MultipleInstancesExpanded = Template.bind({});
MultipleInstancesExpanded.decorators = [withVueRouter(routes)];
MultipleInstancesExpanded.args = {
  ...MultipleInstances.args,
  application: new Application({
    ...application,
    instances: [
      firstInstance,
      secondInstance,
      instanceWithoutRestart,
      instanceWithABunchOfTags,
    ],
  }),
  isExpanded: true,
};

const TemplateWithMultipleApplications = (args) => ({
  components: { ApplicationsListItem, SbaPanel },
  setup() {
    return {
      args,
    };
  },
  template: `
    <sba-panel :seamless="true">
    <applications-list-item
      v-for="application in args.applications"
      :application="application"
    />
    </sba-panel>`,
});

export const MultipleApplications = TemplateWithMultipleApplications.bind({});
MultipleApplications.decorators = [withVueRouter(routes)];

MultipleApplications.args = {
  applications: [
    new Application({
      ...application,
    }),
    new Application({
      ...application,
      instances: [instanceWithoutRestart],
    }),
  ],
};
