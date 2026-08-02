import { describe, expect, it } from 'vitest';

import Application from '@/services/application';
import {
  buildInstanceDetailsUrl,
  findChangedInstanceId,
} from '@/notifications';

const createApplication = (
  name: string,
  status: string,
  instanceStatuses: string[],
) => {
  const instances = instanceStatuses.map((instanceStatus, index) => ({
    id: `instance-${index}`,
    statusInfo: { status: instanceStatus },
    registration: {
      name,
      healthUrl: `http://localhost:${8080 + index}/actuator/health`,
    },
  }));

  return new Application({
    name,
    status,
    instances,
  });
};

describe('notifications', () => {
  describe('buildInstanceDetailsUrl', () => {
    it('builds instance details path', () => {
      expect(buildInstanceDetailsUrl('abc123')).toBe('/instances/abc123/details');
    });
  });

  describe('findChangedInstanceId', () => {
    it('returns instance whose status changed', () => {
      const application = createApplication('app', 'DOWN', ['UP', 'DOWN']);
      const oldApplication = createApplication('app', 'UP', ['UP', 'UP']);

      expect(findChangedInstanceId(application, oldApplication)).toBe(
        'instance-1',
      );
    });

    it('returns sole instance id when only one instance exists', () => {
      const application = createApplication('app', 'DOWN', ['DOWN']);
      const oldApplication = createApplication('app', 'UP', ['UP']);

      expect(findChangedInstanceId(application, oldApplication)).toBe(
        'instance-0',
      );
    });

    it('returns undefined when multiple instances changed', () => {
      const application = createApplication('app', 'DOWN', ['DOWN', 'DOWN']);
      const oldApplication = createApplication('app', 'UP', ['UP', 'UP']);

      expect(
        findChangedInstanceId(application, oldApplication),
      ).toBeUndefined();
    });
  });
});
