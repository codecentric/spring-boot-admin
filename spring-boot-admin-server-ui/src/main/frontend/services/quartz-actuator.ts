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
import { AxiosResponse } from 'axios';

import uri from '../utils/uri';
import Instance from './instance';

/**
 * Quartz Actuator Service
 *
 * Provides static methods to interact with Spring Boot Quartz Actuator endpoint.
 * All methods accept an Instance parameter and return promises with typed responses.
 *
 * This service acts as a utility layer for Quartz operations, abstracting
 * the API communication and providing both low-level and high-level fetch methods.
 */
export class QuartzActuatorService {
  /**
   * Fetch all job groups with their job names
   *
   * @param instance The Instance to fetch from
   * @returns Promise with job groups and job names
   */
  static async fetchJobGroups(
    instance: Instance,
  ): Promise<AxiosResponse<JobGroupsResponse>> {
    return instance.axios.get(uri`actuator/quartz/jobs`, {
      headers: { Accept: 'application/json' },
    });
  }

  /**
   * Fetch details of a specific job
   *
   * @param instance The Instance to fetch from
   * @param groupName The job group name
   * @param jobName The job name
   * @returns Promise with job details including triggers
   */
  static async fetchJobDetail(
    instance: Instance,
    groupName: string,
    jobName: string,
  ): Promise<AxiosResponse<JobDetail>> {
    return instance.axios.get(
      uri`actuator/quartz/jobs/${groupName}/${jobName}`,
      { headers: { Accept: 'application/json' } },
    );
  }

  /**
   * Fetch all jobs in all groups with full details
   *
   * Fetches the list of job groups, then fetches each job individually,
   * and aggregates them into a single array.
   * Handles partial failures gracefully - returns successful jobs even if some fail.
   *
   * @param instance The Instance to fetch from
   * @returns Promise with array of all job details
   */
  static async fetchAllJobs(instance: Instance): Promise<JobDetail[]> {
    const response = await this.fetchJobGroups(instance);
    const jobList = response.data;
    const promises: Promise<AxiosResponse<JobDetail>>[] = [];

    for (const group in jobList.groups) {
      for (const jobName of jobList.groups[group].jobs) {
        promises.push(this.fetchJobDetail(instance, group, jobName));
      }
    }

    const results = await Promise.allSettled(promises);
    return results
      .filter((r) => r.status === 'fulfilled')
      .map(
        (r) =>
          (r as PromiseFulfilledResult<AxiosResponse<JobDetail>>).value.data,
      );
  }

  /**
   * Fetch all trigger groups with their trigger names and paused status
   *
   * @param instance The Instance to fetch from
   * @returns Promise with trigger groups, names, and paused status
   */
  static async fetchTriggerGroups(
    instance: Instance,
  ): Promise<AxiosResponse<TriggerGroupsResponse>> {
    return instance.axios.get(uri`actuator/quartz/triggers`, {
      headers: { Accept: 'application/json' },
    });
  }

  /**
   * Fetch details of a specific trigger
   *
   * @param instance The Instance to fetch from
   * @param groupName The trigger group name
   * @param triggerName The trigger name
   * @returns Promise with trigger details including type-specific configuration
   */
  static async fetchTriggerDetail(
    instance: Instance,
    groupName: string,
    triggerName: string,
  ): Promise<AxiosResponse<TriggerDetail>> {
    return instance.axios.get(
      uri`actuator/quartz/triggers/${groupName}/${triggerName}`,
      { headers: { Accept: 'application/json' } },
    );
  }

  /**
   * Fetch all triggers in all groups with full details
   *
   * Fetches the list of trigger groups, then fetches each trigger individually,
   * and aggregates them into a single array.
   * Handles partial failures gracefully - returns successful triggers even if some fail.
   *
   * @param instance The Instance to fetch from
   * @returns Promise with array of all trigger details
   */
  static async fetchAllTriggers(instance: Instance): Promise<TriggerDetail[]> {
    const response = await this.fetchTriggerGroups(instance);
    const groupList = response.data;
    const promises: Promise<AxiosResponse<TriggerDetail>>[] = [];

    for (const group in groupList.groups) {
      for (const triggerName of groupList.groups[group].triggers) {
        promises.push(this.fetchTriggerDetail(instance, group, triggerName));
      }
    }

    const results = await Promise.allSettled(promises);
    return results
      .filter((r) => r.status === 'fulfilled')
      .map(
        (r) =>
          (r as PromiseFulfilledResult<AxiosResponse<TriggerDetail>>).value
            .data,
      );
  }
}

/**
 * Response structure from GET /actuator/quartz/jobs
 */
export interface JobGroupsResponse {
  groups: { [groupName: string]: { jobs: string[] } };
}

/**
 * Response structure from GET /actuator/quartz/jobs/{groupName}/{jobName}
 */
export interface JobDetail {
  group: string;
  name: string;
  className: string;
  description?: string;
  durable: boolean;
  requestRecovery: boolean;
  data?: { [key: string]: string };
  triggers: Array<{
    group: string;
    name: string;
    previousFireTime?: string;
    nextFireTime?: string;
    priority: number;
  }>;
}

/**
 * Response structure from GET /actuator/quartz/triggers
 */
export interface TriggerGroupsResponse {
  groups: {
    [groupName: string]: {
      paused: boolean;
      triggers: string[];
    };
  };
}

/**
 * Response structure from GET /actuator/quartz/triggers/{groupName}/{triggerName}
 * Includes type-specific trigger details (cron, simple, dailyTimeInterval, calendarInterval, custom)
 */
export interface TriggerDetail {
  group: string;
  name: string;
  description?: string;
  state: string;
  type: 'cron' | 'simple' | 'dailyTimeInterval' | 'calendarInterval' | 'custom';
  priority: number;
  startTime?: string;
  endTime?: string;
  previousFireTime?: string;
  nextFireTime?: string;
  finalFireTime?: string;
  calendarName?: string;
  data?: { [key: string]: string };

  cron?: {
    expression: string;
    timeZone?: string;
  };
  simple?: {
    interval: number;
    repeatCount: number;
    timesTriggered: number;
  };
  dailyTimeInterval?: {
    interval: number;
    daysOfWeek: number[];
    startTimeOfDay?: string;
    endTimeOfDay?: string;
    repeatCount: number;
    timesTriggered: number;
  };
  calendarInterval?: {
    interval: number;
    timeZone?: string;
    timesTriggered: number;
    preserveHourOfDayAcrossDaylightSavings: boolean;
    skipDayIfHourDoesNotExist: boolean;
  };
  custom?: {
    trigger: string;
  };
}
