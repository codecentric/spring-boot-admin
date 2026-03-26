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

/**
 * Mock data for Quartz actuator API responses
 * Used for testing UI components with realistic Quartz data
 */

export const mockJobDetail = {
  className:
    'de.codecentric.boot.admin.sample.QuartzJobsConfiguration$SampleJob',
  data: {
    'job.key': 'job-value',
    'another.key': 'another-value',
  },
  description: 'Sample job to demonstrate Quartz actuator endpoint',
  durable: true,
  group: 'samples',
  name: 'sampleJob',
  requestRecovery: false,
  triggers: [
    {
      group: 'samples',
      name: 'sampleTrigger',
      previousFireTime: '2026-03-26T14:11:11.333Z',
      nextFireTime: '2026-03-26T14:11:21.333Z',
      priority: 5,
    },
    {
      group: 'DEFAULT',
      name: 'hourlyTrigger',
      previousFireTime: '2026-03-26T13:31:31.342Z',
      nextFireTime: '2026-03-26T14:31:31.342Z',
      priority: 5,
    },
  ],
};

export const mockJobDetailNoDescription = {
  className:
    'de.codecentric.boot.admin.sample.QuartzJobsConfiguration$AnotherSampleJob',
  data: {},
  durable: false,
  group: 'samples',
  name: 'anotherJob',
  requestRecovery: true,
  triggers: [],
};

export const mockJobGroupsResponse = {
  groups: {
    samples: {
      jobs: ['sampleJob', 'anotherJob'],
    },
  },
};

export const mockSimpleTriggerDetail = {
  group: 'samples',
  name: 'sampleTrigger',
  description: 'Trigger that executes sample job every 10 seconds',
  state: 'NORMAL',
  type: 'simple' as const,
  startTime: '2026-03-26T13:31:31.333Z',
  previousFireTime: '2026-03-26T14:11:21.333Z',
  nextFireTime: '2026-03-26T14:11:31.333Z',
  priority: 5,
  data: {
    'trigger.key': 'trigger-value',
  },
  simple: {
    interval: 10000,
    repeatCount: -1,
    timesTriggered: 240,
  },
};

export const mockCronTriggerDetail = {
  group: 'samples',
  name: 'dailyTrigger',
  description: 'Daily trigger at 3am',
  state: 'NORMAL',
  type: 'cron' as const,
  previousFireTime: '2026-03-26T03:00:00.000Z',
  nextFireTime: '2026-03-27T03:00:00.000Z',
  priority: 5,
  data: {},
  cron: {
    expression: '0 0 3 * * ?',
    timeZone: 'UTC',
  },
};

export const mockDailyTimeIntervalTriggerDetail = {
  group: 'testing',
  name: 'dailyTimeIntervalTrigger',
  description: 'Runs every weekday during business hours',
  state: 'PAUSED',
  type: 'dailyTimeInterval' as const,
  startTime: '2026-01-01T08:00:00.000Z',
  endTime: '2026-12-31T17:00:00.000Z',
  previousFireTime: '2026-03-26T16:00:00.000Z',
  nextFireTime: '2026-03-27T08:00:00.000Z',
  priority: 3,
  data: {},
  dailyTimeInterval: {
    interval: 60, // minutes
    daysOfWeek: [2, 3, 4, 5, 6], // Mon-Fri
    startTimeOfDay: '08:00:00',
    endTimeOfDay: '17:00:00',
    repeatCount: -1,
    timesTriggered: 156,
  },
};

export const mockCalendarIntervalTriggerDetail = {
  group: 'testing',
  name: 'monthlyTrigger',
  description: 'Runs monthly with DST handling',
  state: 'NORMAL',
  type: 'calendarInterval' as const,
  previousFireTime: '2026-02-26T10:00:00.000Z',
  nextFireTime: '2026-04-26T10:00:00.000Z',
  priority: 5,
  calendarName: 'businessDaysCalendar',
  data: {
    calendar: 'business-days',
  },
  calendarInterval: {
    interval: 1, // every 1 month
    timeZone: 'Europe/Berlin',
    timesTriggered: 3,
    preserveHourOfDayAcrossDaylightSavings: true,
    skipDayIfHourDoesNotExist: false,
  },
};

export const mockCustomTriggerDetail = {
  group: 'testing',
  name: 'customTrigger',
  description: 'Custom trigger with specific implementation',
  state: 'NORMAL',
  type: 'custom' as const,
  previousFireTime: '2026-03-26T12:00:00.000Z',
  nextFireTime: '2026-03-26T14:00:00.000Z',
  priority: 7,
  data: {},
  custom: {
    trigger: 'com.example.CustomQuartzTrigger',
  },
};

export const mockTriggerGroupsResponse = {
  groups: {
    samples: {
      paused: false,
      triggers: ['sampleTrigger', 'dailyTrigger'],
    },
    testing: {
      paused: true,
      triggers: ['dailyTimeIntervalTrigger', 'monthlyTrigger', 'customTrigger'],
    },
  },
};

export const mockTriggerDetailNoDescription = {
  group: 'samples',
  name: 'hourlyTrigger',
  state: 'NORMAL',
  type: 'simple' as const,
  priority: 5,
  previousFireTime: '2026-03-26T13:31:31.342Z',
  nextFireTime: '2026-03-26T14:31:31.342Z',
  data: {},
  simple: {
    interval: 3600000,
    repeatCount: -1,
    timesTriggered: 24,
  },
};
