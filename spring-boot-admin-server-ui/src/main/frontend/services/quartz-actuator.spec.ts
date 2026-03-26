/*
 * Copyright 2014-2024 the original author or authors.
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
import { beforeEach, describe, expect, it, vi } from 'vitest';

import {
  JobDetail,
  QuartzActuatorService,
  TriggerDetail,
} from './quartz-actuator';

// Mock Instance type
interface MockInstance {
  axios: {
    get: ReturnType<typeof vi.fn>;
    post?: ReturnType<typeof vi.fn>;
  };
}

const createMockInstance = (): MockInstance => ({
  axios: {
    get: vi.fn(),
  },
});

describe('QuartzActuatorService', () => {
  let mockInstance: MockInstance;

  beforeEach(() => {
    mockInstance = createMockInstance();
  });

  describe('fetchJobGroups', () => {
    it('should fetch job groups from correct endpoint', async () => {
      const mockResponse = {
        data: {
          groups: {
            samples: { jobs: ['job1', 'job2'] },
            tests: { jobs: ['job3'] },
          },
        },
      };

      mockInstance.axios.get.mockResolvedValue(mockResponse);

      const result = await QuartzActuatorService.fetchJobGroups(
        mockInstance as any,
      );

      expect(mockInstance.axios.get).toHaveBeenCalledWith(
        expect.stringContaining('actuator/quartz/jobs'),
        { headers: { Accept: 'application/json' } },
      );
      expect(result.data).toEqual(mockResponse.data);
    });

    it('should handle empty job groups', async () => {
      const mockResponse = { data: { groups: {} } };
      mockInstance.axios.get.mockResolvedValue(mockResponse);

      const result = await QuartzActuatorService.fetchJobGroups(
        mockInstance as any,
      );

      expect(result.data.groups).toEqual({});
    });

    it('should propagate errors from axios', async () => {
      const error = new Error('Network error');
      mockInstance.axios.get.mockRejectedValue(error);

      await expect(
        QuartzActuatorService.fetchJobGroups(mockInstance as any),
      ).rejects.toThrow('Network error');
    });
  });

  describe('fetchJobDetail', () => {
    it('should fetch job detail with correct path parameters', async () => {
      const mockResponse = {
        data: {
          group: 'samples',
          name: 'jobOne',
          className: 'org.springframework.scheduling.quartz.DelegatingJob',
          durable: false,
          requestRecovery: false,
          triggers: [],
        } as JobDetail,
      };

      mockInstance.axios.get.mockResolvedValue(mockResponse);

      const result = await QuartzActuatorService.fetchJobDetail(
        mockInstance as any,
        'samples',
        'jobOne',
      );

      expect(mockInstance.axios.get).toHaveBeenCalledWith(
        expect.stringContaining('actuator/quartz/jobs/samples/jobOne'),
        { headers: { Accept: 'application/json' } },
      );
      expect(result.data.name).toBe('jobOne');
    });

    it('should include job description when present', async () => {
      const mockResponse = {
        data: {
          group: 'samples',
          name: 'jobOne',
          description: 'A sample job',
          className: 'org.springframework.scheduling.quartz.DelegatingJob',
          durable: false,
          requestRecovery: false,
          triggers: [],
        } as JobDetail,
      };

      mockInstance.axios.get.mockResolvedValue(mockResponse);

      const result = await QuartzActuatorService.fetchJobDetail(
        mockInstance as any,
        'samples',
        'jobOne',
      );

      expect(result.data.description).toBe('A sample job');
    });

    it('should include job data map when present', async () => {
      const mockResponse = {
        data: {
          group: 'samples',
          name: 'jobOne',
          className: 'org.springframework.scheduling.quartz.DelegatingJob',
          durable: false,
          requestRecovery: false,
          data: { username: 'admin', apiKey: 'secret' },
          triggers: [],
        } as JobDetail,
      };

      mockInstance.axios.get.mockResolvedValue(mockResponse);

      const result = await QuartzActuatorService.fetchJobDetail(
        mockInstance as any,
        'samples',
        'jobOne',
      );

      expect(result.data.data).toEqual({
        username: 'admin',
        apiKey: 'secret',
      });
    });

    it('should include associated triggers', async () => {
      const mockResponse = {
        data: {
          group: 'samples',
          name: 'jobOne',
          className: 'org.springframework.scheduling.quartz.DelegatingJob',
          durable: false,
          requestRecovery: false,
          triggers: [
            {
              group: 'samples',
              name: 'every-day',
              nextFireTime: '2020-12-04T12:00:00.000Z',
              priority: 7,
            },
          ],
        } as JobDetail,
      };

      mockInstance.axios.get.mockResolvedValue(mockResponse);

      const result = await QuartzActuatorService.fetchJobDetail(
        mockInstance as any,
        'samples',
        'jobOne',
      );

      expect(result.data.triggers).toHaveLength(1);
      expect(result.data.triggers[0].name).toBe('every-day');
    });
  });

  describe('fetchAllJobs', () => {
    it('should fetch and aggregate all jobs from all groups', async () => {
      const mockGroupsResponse = {
        data: {
          groups: {
            samples: { jobs: ['job1', 'job2'] },
          },
        },
      };

      const mockJob1 = {
        data: {
          name: 'job1',
          group: 'samples',
          className: 'Job1',
          durable: false,
          requestRecovery: false,
          triggers: [],
        } as JobDetail,
      };

      const mockJob2 = {
        data: {
          name: 'job2',
          group: 'samples',
          className: 'Job2',
          durable: false,
          requestRecovery: false,
          triggers: [],
        } as JobDetail,
      };

      mockInstance.axios.get
        .mockResolvedValueOnce(mockGroupsResponse)
        .mockResolvedValueOnce(mockJob1)
        .mockResolvedValueOnce(mockJob2);

      const result = await QuartzActuatorService.fetchAllJobs(
        mockInstance as any,
      );

      expect(result).toHaveLength(2);
      expect(result[0]).toEqual(mockJob1.data);
      expect(result[1]).toEqual(mockJob2.data);
    });

    it('should handle jobs from multiple groups', async () => {
      const mockGroupsResponse = {
        data: {
          groups: {
            samples: { jobs: ['job1'] },
            tests: { jobs: ['job2', 'job3'] },
          },
        },
      };

      mockInstance.axios.get
        .mockResolvedValueOnce(mockGroupsResponse)
        .mockResolvedValueOnce({ data: { name: 'job1', group: 'samples' } })
        .mockResolvedValueOnce({ data: { name: 'job2', group: 'tests' } })
        .mockResolvedValueOnce({ data: { name: 'job3', group: 'tests' } });

      const result = await QuartzActuatorService.fetchAllJobs(
        mockInstance as any,
      );

      expect(result).toHaveLength(3);
    });

    it('should handle partial failures gracefully', async () => {
      const mockGroupsResponse = {
        data: {
          groups: {
            samples: { jobs: ['job1', 'job2'] },
          },
        },
      };

      mockInstance.axios.get
        .mockResolvedValueOnce(mockGroupsResponse)
        .mockResolvedValueOnce({
          data: {
            name: 'job1',
            group: 'samples',
            className: 'Job1',
            durable: false,
            requestRecovery: false,
            triggers: [],
          },
        })
        .mockRejectedValueOnce(new Error('Failed to fetch job2'));

      const result = await QuartzActuatorService.fetchAllJobs(
        mockInstance as any,
      );

      // Should return only successful job
      expect(result).toHaveLength(1);
      expect(result[0].name).toBe('job1');
    });

    it('should return empty array when no jobs exist', async () => {
      const mockGroupsResponse = {
        data: {
          groups: {},
        },
      };

      mockInstance.axios.get.mockResolvedValueOnce(mockGroupsResponse);

      const result = await QuartzActuatorService.fetchAllJobs(
        mockInstance as any,
      );

      expect(result).toHaveLength(0);
    });

    it('should make correct number of API calls', async () => {
      const mockGroupsResponse = {
        data: {
          groups: {
            samples: { jobs: ['job1', 'job2'] },
            tests: { jobs: ['job3'] },
          },
        },
      };

      mockInstance.axios.get
        .mockResolvedValueOnce(mockGroupsResponse)
        .mockResolvedValue({
          data: {
            className: 'Job',
            durable: false,
            requestRecovery: false,
            triggers: [],
          },
        });

      await QuartzActuatorService.fetchAllJobs(mockInstance as any);

      // 1 call for groups + 3 calls for individual jobs = 4 total
      expect(mockInstance.axios.get).toHaveBeenCalledTimes(4);
    });
  });

  describe('fetchTriggerGroups', () => {
    it('should fetch trigger groups from correct endpoint', async () => {
      const mockResponse = {
        data: {
          groups: {
            samples: { paused: false, triggers: ['trigger1', 'trigger2'] },
            DEFAULT: { paused: false, triggers: ['trigger3'] },
          },
        },
      };

      mockInstance.axios.get.mockResolvedValue(mockResponse);

      const result = await QuartzActuatorService.fetchTriggerGroups(
        mockInstance as any,
      );

      expect(mockInstance.axios.get).toHaveBeenCalledWith(
        expect.stringContaining('actuator/quartz/triggers'),
        { headers: { Accept: 'application/json' } },
      );
      expect(result.data).toEqual(mockResponse.data);
    });

    it('should include paused status for each group', async () => {
      const mockResponse = {
        data: {
          groups: {
            paused_group: { paused: true, triggers: ['trigger1'] },
            active_group: { paused: false, triggers: ['trigger2'] },
          },
        },
      };

      mockInstance.axios.get.mockResolvedValue(mockResponse);

      const result = await QuartzActuatorService.fetchTriggerGroups(
        mockInstance as any,
      );

      expect(result.data.groups['paused_group'].paused).toBe(true);
      expect(result.data.groups['active_group'].paused).toBe(false);
    });
  });

  describe('fetchTriggerDetail', () => {
    it('should fetch trigger detail with correct path parameters', async () => {
      const mockResponse = {
        data: {
          name: 'every-day',
          group: 'samples',
          type: 'simple' as const,
          state: 'NORMAL',
          priority: 7,
          simple: { interval: 86400000, repeatCount: -1, timesTriggered: 0 },
        } as TriggerDetail,
      };

      mockInstance.axios.get.mockResolvedValue(mockResponse);

      const result = await QuartzActuatorService.fetchTriggerDetail(
        mockInstance as any,
        'samples',
        'every-day',
      );

      expect(mockInstance.axios.get).toHaveBeenCalledWith(
        expect.stringContaining('actuator/quartz/triggers/samples/every-day'),
        { headers: { Accept: 'application/json' } },
      );
      expect(result.data.name).toBe('every-day');
    });

    it('should handle cron triggers with expression and timezone', async () => {
      const mockResponse = {
        data: {
          name: '3am-weekdays',
          group: 'samples',
          type: 'cron' as const,
          state: 'NORMAL',
          priority: 3,
          cron: {
            expression: '0 0 3 ? * 1,2,3,4,5',
            timeZone: 'Europe/Paris',
          },
        } as TriggerDetail,
      };

      mockInstance.axios.get.mockResolvedValue(mockResponse);

      const result = await QuartzActuatorService.fetchTriggerDetail(
        mockInstance as any,
        'samples',
        '3am-weekdays',
      );

      expect(result.data.cron?.expression).toBe('0 0 3 ? * 1,2,3,4,5');
      expect(result.data.cron?.timeZone).toBe('Europe/Paris');
    });

    it('should handle daily time interval triggers with days and times', async () => {
      const mockResponse = {
        data: {
          name: 'tue-thu',
          group: 'samples',
          type: 'dailyTimeInterval' as const,
          state: 'NORMAL',
          priority: 5,
          dailyTimeInterval: {
            interval: 3600000,
            daysOfWeek: [3, 5],
            startTimeOfDay: '09:00:00',
            endTimeOfDay: '18:00:00',
            repeatCount: -1,
            timesTriggered: 0,
          },
        } as TriggerDetail,
      };

      mockInstance.axios.get.mockResolvedValue(mockResponse);

      const result = await QuartzActuatorService.fetchTriggerDetail(
        mockInstance as any,
        'samples',
        'tue-thu',
      );

      expect(result.data.dailyTimeInterval?.daysOfWeek).toEqual([3, 5]);
      expect(result.data.dailyTimeInterval?.startTimeOfDay).toBe('09:00:00');
    });

    it('should handle calendar interval triggers', async () => {
      const mockResponse = {
        data: {
          name: 'once-a-week',
          group: 'samples',
          type: 'calendarInterval' as const,
          state: 'NORMAL',
          priority: 5,
          calendarInterval: {
            interval: 604800000,
            timeZone: 'Etc/UTC',
            timesTriggered: 0,
            preserveHourOfDayAcrossDaylightSavings: false,
            skipDayIfHourDoesNotExist: false,
          },
        } as TriggerDetail,
      };

      mockInstance.axios.get.mockResolvedValue(mockResponse);

      const result = await QuartzActuatorService.fetchTriggerDetail(
        mockInstance as any,
        'samples',
        'once-a-week',
      );

      expect(result.data.calendarInterval?.interval).toBe(604800000);
      expect(result.data.calendarInterval?.timeZone).toBe('Etc/UTC');
    });

    it('should handle custom triggers', async () => {
      const mockResponse = {
        data: {
          name: 'custom-trigger',
          group: 'samples',
          type: 'custom' as const,
          state: 'NORMAL',
          priority: 10,
          custom: { trigger: 'com.example.CustomTrigger@fdsfsd' },
        } as TriggerDetail,
      };

      mockInstance.axios.get.mockResolvedValue(mockResponse);

      const result = await QuartzActuatorService.fetchTriggerDetail(
        mockInstance as any,
        'samples',
        'custom-trigger',
      );

      expect(result.data.custom?.trigger).toContain('CustomTrigger');
    });
  });

  describe('fetchAllTriggers', () => {
    it('should fetch and aggregate all triggers from all groups', async () => {
      const mockGroupsResponse = {
        data: {
          groups: {
            samples: { paused: false, triggers: ['trigger1', 'trigger2'] },
          },
        },
      };

      const mockTrigger1 = {
        data: {
          name: 'trigger1',
          group: 'samples',
          type: 'simple' as const,
          state: 'NORMAL',
          priority: 7,
        } as TriggerDetail,
      };

      const mockTrigger2 = {
        data: {
          name: 'trigger2',
          group: 'samples',
          type: 'cron' as const,
          state: 'NORMAL',
          priority: 5,
        } as TriggerDetail,
      };

      mockInstance.axios.get
        .mockResolvedValueOnce(mockGroupsResponse)
        .mockResolvedValueOnce(mockTrigger1)
        .mockResolvedValueOnce(mockTrigger2);

      const result = await QuartzActuatorService.fetchAllTriggers(
        mockInstance as any,
      );

      expect(result).toHaveLength(2);
      expect(result[0]).toEqual(mockTrigger1.data);
      expect(result[1]).toEqual(mockTrigger2.data);
    });

    it('should handle triggers from multiple groups', async () => {
      const mockGroupsResponse = {
        data: {
          groups: {
            samples: { paused: false, triggers: ['trigger1'] },
            tests: { paused: false, triggers: ['trigger2', 'trigger3'] },
          },
        },
      };

      mockInstance.axios.get
        .mockResolvedValueOnce(mockGroupsResponse)
        .mockResolvedValue({
          data: {
            type: 'simple',
            state: 'NORMAL',
            priority: 5,
          } as TriggerDetail,
        });

      const result = await QuartzActuatorService.fetchAllTriggers(
        mockInstance as any,
      );

      expect(result).toHaveLength(3);
    });

    it('should handle partial failures gracefully', async () => {
      const mockGroupsResponse = {
        data: {
          groups: {
            samples: { paused: false, triggers: ['trigger1', 'trigger2'] },
          },
        },
      };

      mockInstance.axios.get
        .mockResolvedValueOnce(mockGroupsResponse)
        .mockResolvedValueOnce({
          data: {
            name: 'trigger1',
            group: 'samples',
            type: 'simple',
            state: 'NORMAL',
            priority: 7,
          },
        })
        .mockRejectedValueOnce(new Error('Failed to fetch trigger2'));

      const result = await QuartzActuatorService.fetchAllTriggers(
        mockInstance as any,
      );

      // Should return only successful trigger
      expect(result).toHaveLength(1);
      expect(result[0].name).toBe('trigger1');
    });

    it('should return empty array when no triggers exist', async () => {
      const mockGroupsResponse = {
        data: {
          groups: {},
        },
      };

      mockInstance.axios.get.mockResolvedValueOnce(mockGroupsResponse);

      const result = await QuartzActuatorService.fetchAllTriggers(
        mockInstance as any,
      );

      expect(result).toHaveLength(0);
    });

    it('should handle groups with empty trigger arrays', async () => {
      const mockGroupsResponse = {
        data: {
          groups: {
            samples: { paused: false, triggers: [] },
          },
        },
      };

      mockInstance.axios.get.mockResolvedValueOnce(mockGroupsResponse);

      const result = await QuartzActuatorService.fetchAllTriggers(
        mockInstance as any,
      );

      expect(result).toHaveLength(0);
    });

    it('should make correct number of API calls', async () => {
      const mockGroupsResponse = {
        data: {
          groups: {
            samples: { paused: false, triggers: ['t1', 't2'] },
            tests: { paused: false, triggers: ['t3'] },
          },
        },
      };

      mockInstance.axios.get
        .mockResolvedValueOnce(mockGroupsResponse)
        .mockResolvedValue({
          data: {
            type: 'simple',
            state: 'NORMAL',
            priority: 5,
          } as TriggerDetail,
        });

      await QuartzActuatorService.fetchAllTriggers(mockInstance as any);

      // 1 call for groups + 3 calls for individual triggers = 4 total
      expect(mockInstance.axios.get).toHaveBeenCalledTimes(4);
    });
  });
});
