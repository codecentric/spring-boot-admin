/*
 * Copyright 2014-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { createI18n } from 'vue-i18n';

export const createQuartzI18n = () => {
  return createI18n({
    legacy: false,
    locale: 'en',
    messages: {
      en: {
        'instances.quartz.jobs': 'Jobs',
        'instances.quartz.triggers': 'Triggers',
        'instances.quartz.name': 'Name',
        'instances.quartz.description': 'Description',
        'instances.quartz.group': 'Group',
        'instances.quartz.durable': 'Durable',
        'instances.quartz.recovery': 'Recovery',
        'instances.quartz.state': 'State',
        'instances.quartz.type': 'Type',
        'instances.quartz.priority': 'Priority',
        'instances.quartz.next_fire_time': 'Next Fire Time',
        'instances.quartz.total': 'Total',
      },
    },
  });
};
