/*
 * Copyright 2014-2021 the original author or authors.
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
import { screen, waitFor } from '@testing-library/vue';
import { describe, expect, it } from 'vitest';

import { render } from '@/test-utils';
import MBeanOperation from '@/views/instances/jolokia/m-bean-operation.vue';

describe('m-bean-operation.vue', () => {
  it('should open confirmation modal if operation contains no arguments', async () => {
    const wrapper = render(MBeanOperation, {
      props: {
        name: 'doIt()',
        descriptor: { args: [], ret: 'java.sth.doIt()' },
      },
    });

    await userEvent.click(screen.getByText('doIt()'));

    await waitFor(() => {
      screen.findByRole('dialog');
    });

    await screen.findAllByTestId('mBeanOperationModal');

    const buttonOK = screen.queryByRole('button', { name: 'OK' });
    await userEvent.click(buttonOK);

    expect(wrapper.emitted().click).toBeDefined();
  });

  it('should not open confirmation modal if operation contains arguments', async () => {
    render(MBeanOperation, {
      props: {
        name: 'doIt()',
        descriptor: { args: [{ property1: '' }], ret: 'java.sth.doIt()' },
      },
    });

    await userEvent.click(screen.getByText('doIt()'));
    await waitFor(() => {
      screen.findByRole('dialog');
    });

    expect(screen.queryByTestId('mBeanOperationModal')).toBeNull();
  });

  it('does not render HTML/script markup contained in the operation name', async () => {
    const maliciousName = '<img src=x onerror="window.__xss = 1">()';

    render(MBeanOperation, {
      props: {
        name: maliciousName,
        descriptor: { args: [], ret: 'void' },
      },
    });

    await userEvent.click(screen.getByText(maliciousName));

    await screen.findAllByTestId('mBeanOperationModal');

    // The malicious markup must show up as plain text, never be parsed as HTML.
    expect(document.querySelectorAll('img').length).toBe(0);
    expect((window as any).__xss).toBeUndefined();
  });
});
