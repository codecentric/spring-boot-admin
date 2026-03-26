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

package de.codecentric.boot.admin.sample;

import java.util.TimeZone;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Configuration for sample Quartz jobs and triggers to demonstrate the Quartz actuator
 * endpoint. This allows testing of job/trigger listing in Spring Boot Admin UI.
 */
@Configuration
public class QuartzJobsConfiguration {

	/**
	 * Creates job details for the sample job.
	 * @return job detail for the sample job
	 */
	@Bean
	public JobDetail sampleJobDetail() {
		return JobBuilder.newJob(SampleJob.class)
			.withIdentity("sampleJob", "samples")
			.withDescription("Sample job to demonstrate Quartz actuator endpoint")
			.storeDurably()
			.build();
	}

	/**
	 * Creates job details for the another sample job.
	 * @return job detail for the another sample job
	 */
	@Bean
	public JobDetail anotherSampleJobDetail() {
		return JobBuilder.newJob(AnotherSampleJob.class)
			.withIdentity("anotherJob", "samples")
			.withDescription("Another sample job for testing")
			.storeDurably()
			.build();
	}

	/**
	 * Creates a simple trigger that executes the sample job every 10 seconds.
	 * @return trigger for the sample job
	 */
	@Bean
	public Trigger sampleJobTrigger() {
		return TriggerBuilder.newTrigger()
			.forJob(sampleJobDetail())
			.withIdentity("sampleTrigger", "samples")
			.withDescription("Trigger that executes sample job every 10 seconds")
			.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(10).repeatForever())
			.build();
	}

	/**
	 * Creates a cron trigger that executes another sample job every day at 3am.
	 * @return trigger for the another sample job
	 */
	@Bean
	public Trigger anotherSampleJobTrigger() {
		return TriggerBuilder.newTrigger()
			.forJob(anotherSampleJobDetail())
			.withIdentity("dailyTrigger", "samples")
			.withDescription("Daily trigger at 3am")
			.withSchedule(CronScheduleBuilder.cronSchedule("0 0 3 * * ?").inTimeZone(TimeZone.getTimeZone("UTC")))
			.build();
	}

	/**
	 * Creates a simple trigger for testing purposes (every hour).
	 * @return trigger for hourly execution
	 */
	@Bean
	public Trigger hourlyTestTrigger() {
		return TriggerBuilder.newTrigger()
			.forJob(sampleJobDetail())
			.withIdentity("hourlyTrigger", "DEFAULT")
			.withDescription("Hourly trigger for testing")
			.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(1).repeatForever())
			.build();
	}

	/**
	 * Sample job that logs at regular intervals.
	 */
	public static class SampleJob extends QuartzJobBean {

		@Override
		protected void executeInternal(org.quartz.JobExecutionContext context) {
			System.out.println("Sample Quartz Job executed at " + new java.util.Date());
		}

	}

	/**
	 * Another sample job for demonstration.
	 */
	public static class AnotherSampleJob extends QuartzJobBean {

		@Override
		protected void executeInternal(org.quartz.JobExecutionContext context) {
			System.out.println("Another Quartz Job executed at " + new java.util.Date());
		}

	}

}
