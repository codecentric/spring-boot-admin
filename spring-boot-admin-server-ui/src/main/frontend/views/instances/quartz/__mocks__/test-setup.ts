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
import { createI18n } from 'vue-i18n';

export const createQuartzI18n = () => {
  return createI18n({
    legacy: false,
    locale: 'en',
    messages: {
      en: {
        'instances.quartz.actions': 'Actions',
        'instances.quartz.active_triggers': 'Active Triggers',
        'instances.quartz.associated_triggers': 'Associated Triggers',
        'instances.quartz.associated_with_calendar': 'Associated with calendar',
        'instances.quartz.basic_information': 'Basic Information',
        'instances.quartz.calendar': 'Calendar',
        'instances.quartz.class': 'Class',
        'instances.quartz.configuration': 'Configuration',
        'instances.quartz.cron_expression': 'Cron Expression',
        'instances.quartz.custom_trigger': 'Custom Trigger',
        'instances.quartz.days_of_week': 'Days of Week',
        'instances.quartz.description': 'Description',
        'instances.quartz.details': 'Details',
        'instances.quartz.disabled': 'Disabled',
        'instances.quartz.durable': 'Durable',
        'instances.quartz.enabled': 'Enabled',
        'instances.quartz.end_time': 'End Time',
        'instances.quartz.final_fire_time': 'Final Fire Time',
        'instances.quartz.friday': 'Friday',
        'instances.quartz.group': 'Group',
        'instances.quartz.infinite': 'Infinite',
        'instances.quartz.interval': 'Interval',
        'instances.quartz.job_data': 'Job Data',
        'instances.quartz.job_name': 'Job Name',
        'instances.quartz.jobs': 'Jobs',
        'instances.quartz.key': 'Key',
        'instances.quartz.label': 'Quartz',
        'instances.quartz.last_fire': 'Last Fire',
        'instances.quartz.last_fire_time': 'Last Fire Time',
        'instances.quartz.monday': 'Monday',
        'instances.quartz.name': 'Name',
        'instances.quartz.next_fire': 'Next Fire',
        'instances.quartz.next_fire_time': 'Next Fire Time',
        'instances.quartz.no': 'No',
        'instances.quartz.no_data': 'No Quartz information available',
        'instances.quartz.paused_triggers': 'Paused Triggers',
        'instances.quartz.preserve_hour_of_day': 'Preserve Hour of Day',
        'instances.quartz.priority': 'Priority',
        'instances.quartz.recovery': 'Recovery',
        'instances.quartz.repeat_count': 'Repeat Count',
        'instances.quartz.request_recovery': 'Request Recovery',
        'instances.quartz.saturday': 'Saturday',
        'instances.quartz.schedule_configuration': 'Schedule Configuration',
        'instances.quartz.skip_day_if_hour_does_not_exist': 'Skip Day If Hour Does Not Exist',
        'instances.quartz.start_time': 'Start Time',
        'instances.quartz.state': 'State',
        'instances.quartz.sunday': 'Sunday',
        'instances.quartz.system_default': 'System Default',
        'instances.quartz.thursday': 'Thursday',
        'instances.quartz.time_window': 'Time Window',
        'instances.quartz.time_zone': 'Time Zone',
        'instances.quartz.times_triggered': 'Times Triggered',
        'instances.quartz.timing_information': 'Timing Information',
        'instances.quartz.total': 'total',
        'instances.quartz.trigger_data': 'Trigger Data',
        'instances.quartz.trigger_failed': 'Trigger Failed',
        'instances.quartz.trigger_failed_message': 'Failed to trigger job: {error}',
        'instances.quartz.trigger_failed_notification': 'Failed to trigger job "{jobName}": {error}',
        'instances.quartz.trigger_fire_times': 'Trigger Fire Times',
        'instances.quartz.trigger_job_now': 'Trigger Job Now',
        'instances.quartz.trigger_success_message': 'Job "{jobName}" triggered successfully',
        'instances.quartz.trigger_success_title': 'Job Triggered',
        'instances.quartz.triggers': 'Triggers',
        'instances.quartz.tuesday': 'Tuesday',
        'instances.quartz.type': 'Type',
        'instances.quartz.value': 'Value',
        'instances.quartz.wednesday': 'Wednesday',
        'instances.quartz.yes': 'Yes',
      },
    },
  });
};
