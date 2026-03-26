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
import { mount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';

import {
  mockJobDetail,
  mockJobDetailNoDescription,
} from './__mocks__/quartz-data';
import { createQuartzI18n } from './__mocks__/test-setup';
import JobsSection from './jobs-section.vue';

describe('JobsSection.vue', () => {
  const createWrapper = (jobs: any[] = []) => {
    return mount(JobsSection, {
      props: { jobs },
      global: {
        plugins: [createQuartzI18n()],
      },
    });
  };

  it('renders nothing when no jobs provided', () => {
    const wrapper = createWrapper([]);
    expect(wrapper.find('h2').exists()).toBe(false);
  });

  it('renders section header and displays all jobs', () => {
    const wrapper = createWrapper([mockJobDetail, mockJobDetailNoDescription]);

    expect(wrapper.text()).toContain('Jobs');
    expect(wrapper.text()).toContain('2');
    expect(wrapper.text()).toContain(mockJobDetail.name);
    expect(wrapper.text()).toContain(mockJobDetailNoDescription.name);
  });

  it('displays job descriptions', () => {
    const wrapper = createWrapper([mockJobDetail, mockJobDetailNoDescription]);

    expect(wrapper.text()).toContain(mockJobDetail.description);
  });

  it('displays job groups and durable status', () => {
    const wrapper = createWrapper([mockJobDetail, mockJobDetailNoDescription]);

    expect(wrapper.text()).toContain(mockJobDetail.group);
    expect(wrapper.text()).toContain('Yes');
    expect(wrapper.text()).toContain('No');
  });

  it('displays recovery status', () => {
    const wrapper = createWrapper([mockJobDetail, mockJobDetailNoDescription]);

    expect(wrapper.text()).toContain('Disabled');
    expect(wrapper.text()).toContain('Enabled');
  });

  it('displays all job data from actuator response', () => {
    const wrapper = createWrapper([mockJobDetail]);

    const text = wrapper.text();
    expect(text).toContain(mockJobDetail.name);
    expect(text).toContain(mockJobDetail.className);
    expect(text).toContain(mockJobDetail.description);
    expect(text).toContain(mockJobDetail.group);
    expect(text).toContain('Durable');
  });
});
