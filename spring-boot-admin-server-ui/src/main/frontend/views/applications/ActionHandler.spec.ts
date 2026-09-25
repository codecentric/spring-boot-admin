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
import { describe, expect, it, vi } from 'vitest';

import {
  ApplicationActionHandler,
  InstanceActionHandler,
} from '@/views/applications/ActionHandler';

// A minimal translation function that mimics vue-i18n's interpolation:
// it does NOT escape the injected {name}/{error} values, so any HTML
// contained in them ends up verbatim in the returned string - exactly
// like the real i18n messages that still contain literal <code> markup.
const t = (key: string, params: Record<string, any> = {}) => {
  const templates: Record<string, string> = {
    'applications.actions.unregister': 'Deregister?',
    'applications.actions.shutdown': 'Shutdown?',
    'applications.actions.restart': 'Restart?',
    'applications.unregister': 'Deregister application <code>{name}</code>?',
    'applications.unregister_successful':
      'Successfully deregistered application <code>{name}</code>.',
    'applications.unregister_failed':
      'Deregistration of application <code>{name}</code> failed ({error}).',
    'applications.shutdown': 'Shutdown application <code>{name}</code>?',
    'applications.shutdown_successful':
      'Successfully shutdown application {name}.',
    'applications.shutdown_failed': 'Failed to shutdown application {name}.',
    'applications.restart': 'Restart application <code>{name}</code>?',
    'applications.restarted':
      'Successfully restarted application <code>{name}</code>.',
    'applications.restart_failed':
      'Failed to restart application {name} ({error}).',
    'instances.unregister': 'Deregister instance <code>{name}</code>?',
    'instances.unregister_successful':
      'Successfully deregistered instance <code>{name}</code>.',
    'instances.unregister_failed':
      'Deregistration of instance <code>{name}</code> failed ({error}).',
    'instances.shutdown': 'Shutdown instance <code>{name}</code>?',
    'instances.shutdown_successful': 'Successfully shutdown instance {name}.',
    'instances.shutdown_failed': 'Failed to shutdown instance {name}.',
    'instances.restart': 'Restart instance <code>{name}</code>?',
    'instances.restarted':
      'Successfully restarted instance <code>{name}</code>.',
    'instances.restart_failed': 'Failed to restart instance {name} ({error}).',
  };
  let result = templates[key] ?? key;
  for (const [param, value] of Object.entries(params)) {
    result = result.replaceAll(`{${param}}`, String(value));
  }
  return result;
};

function createModalStub(confirmed = true) {
  return { confirm: vi.fn().mockResolvedValue(confirmed) };
}

function createNotificationCenterStub() {
  return { success: vi.fn(), error: vi.fn() };
}

describe('ApplicationActionHandler', () => {
  const maliciousName = '</code><img src=x onerror="window.__xss = 1">';

  it('sanitizes the application name in the shutdown confirmation dialog', async () => {
    const $sbaModal = createModalStub(false);
    const notificationCenter = createNotificationCenterStub();
    const handler = new ApplicationActionHandler(
      $sbaModal,
      t,
      notificationCenter,
    );

    await handler.shutdown({ name: maliciousName } as any);

    const body = $sbaModal.confirm.mock.calls[0][1];
    expect(body).not.toContain('<img');
    expect(body).not.toContain('onerror');
  });

  it('sanitizes the application name in the restart confirmation dialog', async () => {
    const $sbaModal = createModalStub(false);
    const notificationCenter = createNotificationCenterStub();
    const handler = new ApplicationActionHandler(
      $sbaModal,
      t,
      notificationCenter,
    );

    await handler.restart({ name: maliciousName } as any);

    const body = $sbaModal.confirm.mock.calls[0][1];
    expect(body).not.toContain('<img');
    expect(body).not.toContain('onerror');
  });

  it('sanitizes the application name in the unregister confirmation dialog and success toast', async () => {
    const $sbaModal = createModalStub(true);
    const notificationCenter = createNotificationCenterStub();
    const application = {
      name: maliciousName,
      unregister: vi.fn().mockResolvedValue(undefined),
    };
    const handler = new ApplicationActionHandler(
      $sbaModal,
      t,
      notificationCenter,
    );

    await handler.unregister(application as any);

    const confirmBody = $sbaModal.confirm.mock.calls[0][1];
    expect(confirmBody).not.toContain('<img');
    expect(confirmBody).not.toContain('onerror');

    const successMessage = notificationCenter.success.mock.calls[0][0];
    expect(successMessage).not.toContain('<img');
    expect(successMessage).not.toContain('onerror');
  });

  it('sanitizes the application name in the unregister error toast', async () => {
    const $sbaModal = createModalStub(true);
    const notificationCenter = createNotificationCenterStub();
    const application = {
      name: maliciousName,
      unregister: vi.fn().mockRejectedValue({ response: { status: 500 } }),
    };
    const handler = new ApplicationActionHandler(
      $sbaModal,
      t,
      notificationCenter,
    );

    await handler.unregister(application as any);

    const errorMessage = notificationCenter.error.mock.calls[0][0];
    expect(errorMessage).not.toContain('<img');
    expect(errorMessage).not.toContain('onerror');
  });

  it('preserves safe markup such as <code> around the sanitized name', async () => {
    const $sbaModal = createModalStub(false);
    const notificationCenter = createNotificationCenterStub();
    const handler = new ApplicationActionHandler(
      $sbaModal,
      t,
      notificationCenter,
    );

    await handler.shutdown({ name: 'my-app' } as any);

    const body = $sbaModal.confirm.mock.calls[0][1];
    expect(body).toBe('Shutdown application <code>my-app</code>?');
  });
});

describe('InstanceActionHandler', () => {
  const maliciousId = '</code><script>window.__xss = 1</script>';

  it('sanitizes the instance id in the shutdown confirmation dialog', async () => {
    const $sbaModal = createModalStub(false);
    const notificationCenter = createNotificationCenterStub();
    const handler = new InstanceActionHandler($sbaModal, t, notificationCenter);

    await handler.shutdown({ id: maliciousId } as any);

    const body = $sbaModal.confirm.mock.calls[0][1];
    expect(body).not.toContain('<script');
  });

  it('sanitizes the instance id in the restart confirmation dialog', async () => {
    const $sbaModal = createModalStub(false);
    const notificationCenter = createNotificationCenterStub();
    const handler = new InstanceActionHandler($sbaModal, t, notificationCenter);

    await handler.restart({ id: maliciousId } as any);

    const body = $sbaModal.confirm.mock.calls[0][1];
    expect(body).not.toContain('<script');
  });

  it('sanitizes the instance id in the unregister confirmation dialog and success toast', async () => {
    const $sbaModal = createModalStub(true);
    const notificationCenter = createNotificationCenterStub();
    const instance = {
      id: maliciousId,
      unregister: vi.fn().mockResolvedValue(undefined),
    };
    const handler = new InstanceActionHandler($sbaModal, t, notificationCenter);

    await handler.unregister(instance as any);

    const confirmBody = $sbaModal.confirm.mock.calls[0][1];
    expect(confirmBody).not.toContain('<script');

    const successMessage = notificationCenter.success.mock.calls[0][0];
    expect(successMessage).not.toContain('<script');
  });

  it('sanitizes the instance id in the unregister error toast', async () => {
    const $sbaModal = createModalStub(true);
    const notificationCenter = createNotificationCenterStub();
    const instance = {
      id: maliciousId,
      unregister: vi.fn().mockRejectedValue({ response: { status: 500 } }),
    };
    const handler = new InstanceActionHandler($sbaModal, t, notificationCenter);

    await handler.unregister(instance as any);

    const errorMessage = notificationCenter.error.mock.calls[0][0];
    expect(errorMessage).not.toContain('<script');
  });
});
