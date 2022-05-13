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

import ApplicationsListItem from "./applications-list-item.vue";
import {applications} from "../../mocks/applications/data.js";
import Application from "../../services/application.js";
import SbaPanel from "../../components/sba-panel.vue";
import withVueRouter from "storybook-vue3-router";

const application = applications[0];
const routes = [
  {path: "/journal", name: "journal"}
];

export default {
  component: ApplicationsListItem,
  title: 'Views/Applications/ApplicationListItem',
};

const Template = (args) => ({
  components: {ApplicationsListItem, SbaPanel},
  setup() {
    return {
      args
    };
  },
  template: `
    <sba-panel :seamless="true">
      <applications-list-item v-bind="args"/>
    </sba-panel>`,
});

export const OneInstance = Template.bind({});
OneInstance.decorators = [
  withVueRouter(routes)
];
OneInstance.args = {
  application: new Application(application)
}

export const OneInstanceExpanded = Template.bind({});
OneInstance.decorators = [
  withVueRouter(routes)

];
OneInstanceExpanded.args = {
  ...OneInstance.args,
  isExpanded: true,
}

export const MultipleInstances = Template.bind({});
MultipleInstances.decorators = [
  withVueRouter(routes)

];
const firstInstance = {...application.instances[0]};
const secondInstance = {...application.instances[0]};
secondInstance.statusTimestamp = Date.now();
MultipleInstances.args = {
  application: new Application({
    ...application,
    instances: [
      firstInstance,
      secondInstance
    ]
  })
}

export const MultipleInstancesExpanded = Template.bind({});
MultipleInstancesExpanded.decorators = [
  withVueRouter(routes)
];
MultipleInstancesExpanded.args = {
  ...MultipleInstances.args,
  isExpanded: true,
}
