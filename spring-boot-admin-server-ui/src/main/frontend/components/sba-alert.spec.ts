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

import SbaAlert from '@/components/sba-alert.vue';

import { render } from '@/test-utils';

describe('SbaAlert', () => {
  it('should render string error message', async () => {
    render(SbaAlert, {
      props: {
        error: 'Something went wrong',
      },
    });

    expect(await screen.findByText('Something went wrong')).toBeInTheDocument();
  });

  it('should preserve safe markup in sanitized message', async () => {
    render(SbaAlert, {
      props: {
        error: 'Request failed: <strong>timeout</strong>',
      },
    });

    const strong = await screen.findByText('timeout');
    expect(strong.tagName).toBe('STRONG');
  });

  it('should remove XSS vectors from string error', async () => {
    render(SbaAlert, {
      props: {
        error:
          '<img src=x onerror="window.__xss = 1"><script>window.__xss = 1</script>',
      },
    });

    await screen.findByRole('alert');
    expect(document.querySelectorAll('img').length).toBe(0);
    expect(document.querySelectorAll('script').length).toBe(0);
    expect((window as any).__xss).toBeUndefined();
  });

  it('should remove XSS vectors from Error message', async () => {
    render(SbaAlert, {
      props: {
        error: new Error('<img src=x onerror="window.__xss = 1">boom'),
      },
    });

    expect(await screen.findByText('boom')).toBeInTheDocument();
    expect(document.querySelectorAll('img').length).toBe(0);
    expect((window as any).__xss).toBeUndefined();
  });
});
