/*
 * Copyright 2014-2025 the original author or authors.
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
import userEvent from '@testing-library/user-event';
import { screen } from '@testing-library/vue';
import { beforeEach, describe, expect, it } from 'vitest';

import { render } from '../test-utils';
import SbaAccordion from './sba-accordion.vue';

describe('sba-accordion.vue', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('should load open state from localStorage when id is provided', () => {
    const accordionId = 'test-accordion';
    localStorage.setItem(
      `de.codecentric.spring-boot-admin.accordion.${accordionId}.open`,
      'false',
    );

    const { emitted } = render(SbaAccordion, {
      props: {
        id: accordionId,
        modelValue: true,
      },
      slots: {
        title: 'Test Title',
        default: 'Test Content',
      },
    });

    expect(emitted()['update:modelValue'][0]).toContain(false);
  });

  it('should save open state to localStorage when title is clicked', async () => {
    const accordionId = 'test-accordion';

    render(SbaAccordion, {
      props: {
        id: accordionId,
        modelValue: true,
      },
      slots: {
        title: 'Test Title',
        default: 'Test Content',
      },
    });

    const title = screen.getByText('Test Title');
    await userEvent.click(title);

    const storedValue = localStorage.getItem(
      `de.codecentric.spring-boot-admin.accordion.${accordionId}.open`,
    );
    expect(storedValue).toBe('false');
  });

  it('should not interact with localStorage when id is not provided', async () => {
    render(SbaAccordion, {
      props: {
        modelValue: true,
      },
      slots: {
        title: 'Test Title',
        default: 'Test Content',
      },
    });

    const title = screen.getByText('Test Title');
    await userEvent.click(title);

    expect(localStorage.length).toBe(0);
  });

  it('should use default open state when no localStorage value exists', () => {
    const accordionId = 'test-accordion';

    const { emitted } = render(SbaAccordion, {
      props: {
        id: accordionId,
        modelValue: false,
      },
      slots: {
        title: 'Test Title',
        default: 'Test Content',
      },
    });

    expect(emitted()['update:modelValue']).toBeUndefined();
  });

  it('should toggle open state and save to localStorage', async () => {
    const accordionId = 'test-accordion';
    localStorage.setItem(
      `de.codecentric.spring-boot-admin.accordion.${accordionId}.open`,
      'true',
    );

    render(SbaAccordion, {
      props: {
        id: accordionId,
        modelValue: false,
      },
      slots: {
        title: 'Test Title',
        default: 'Test Content',
      },
    });

    const title = screen.getByText('Test Title');
    await userEvent.click(title);

    const storedValue = localStorage.getItem(
      `de.codecentric.spring-boot-admin.accordion.${accordionId}.open`,
    );
    expect(storedValue).toBe('false');
  });
});
