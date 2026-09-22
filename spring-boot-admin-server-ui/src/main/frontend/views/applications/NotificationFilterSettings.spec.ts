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
import { screen } from '@testing-library/vue';
import { describe, expect, it } from 'vitest';

import { render } from '@/test-utils';
import NotificationFilterSettings from '@/views/applications/NotificationFilterSettings.vue';

describe('NotificationFilterSettings', () => {
  it('does not render HTML/script markup contained in the object name', async () => {
    const maliciousName = '<img src=x onerror="window.__xss = 1">';

    render(NotificationFilterSettings, {
      props: {
        object: { name: maliciousName },
        notificationFilters: [],
      },
    });

    // The malicious markup must show up as plain text, never be parsed as HTML.
    expect(await screen.findByText(maliciousName)).toBeInTheDocument();

    // No <img> element must have been created from the payload.
    expect(document.querySelectorAll('img').length).toBe(0);
    expect((window as any).__xss).toBeUndefined();
  });

  it('renders the suppressed-for label with the instance/application id as plain text', async () => {
    const maliciousId = '<b>bold</b>';

    render(NotificationFilterSettings, {
      props: {
        object: { id: maliciousId },
        notificationFilters: [
          {
            affects: () => true,
            expiry: null,
          },
        ],
      },
    });

    expect(await screen.findByText(maliciousId)).toBeInTheDocument();
    expect(document.querySelectorAll('b').length).toBe(0);
  });
});
