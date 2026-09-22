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
  it('strips dangerous markup contained in the object name', async () => {
    const maliciousName = '<img src=x onerror="window.__xss = 1">rogue-app';

    render(NotificationFilterSettings, {
      props: {
        object: { name: maliciousName },
        notificationFilters: [],
      },
    });

    // The application name still shows up as text ...
    expect(await screen.findByText(/rogue-app/)).toBeInTheDocument();

    // ... but the <img onerror=...> must have been stripped by sanitizeHtml,
    // it must never be parsed into a real element / fire its handler.
    expect(document.querySelectorAll('img').length).toBe(0);
    expect((window as any).__xss).toBeUndefined();
  });

  it('renders safe HTML markup contained in the object id', async () => {
    const nameWithMarkup = '<b>bold-instance</b>';

    render(NotificationFilterSettings, {
      props: {
        object: { id: nameWithMarkup },
        notificationFilters: [
          {
            affects: () => true,
            expiry: null,
          },
        ],
      },
    });

    // sanitize-html allows harmless formatting tags like <b> by default,
    // so they render as real elements ...
    const bold = await screen.findByText('bold-instance');
    expect(bold.tagName).toBe('B');
  });

  it('strips script tags contained in the object id', async () => {
    const maliciousId = '<script>window.__xss = 1</script>evil-instance';

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

    expect(await screen.findByText(/evil-instance/)).toBeInTheDocument();
    expect(document.querySelectorAll('script').length).toBe(0);
    expect((window as any).__xss).toBeUndefined();
  });
});
