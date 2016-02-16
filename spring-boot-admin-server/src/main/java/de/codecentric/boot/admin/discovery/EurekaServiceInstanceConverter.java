/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin.discovery;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient.EurekaServiceInstance;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.netflix.appinfo.InstanceInfo;

import de.codecentric.boot.admin.model.Application;

/**
 * Converts {@link EurekaServiceInstance}s to {@link Application}s
 *
 * @author Johannes Edmeier
 */
public class EurekaServiceInstanceConverter implements ServiceInstanceConverter {
	@Override
	public Application convert(ServiceInstance instance) {
		Assert.isInstanceOf(EurekaServiceInstance.class, instance,
				"serviceInstance must be of type EurekaServiceInstance");

		return convert(((EurekaServiceInstance) instance).getInstanceInfo());
	}

	private Application convert(InstanceInfo instanceInfo) {
		String mgmtUrl = instanceInfo.getHomePageUrl();
		String mgmtPath = instanceInfo.getMetadata().get("management.context-path");
		if (StringUtils.hasText(mgmtPath)) {
			mgmtUrl = append(mgmtUrl, mgmtPath);
		}
		return Application.create(instanceInfo.getAppName())
				.withHealthUrl(instanceInfo.getHealthCheckUrl()).withManagementUrl(mgmtUrl)
				.withServiceUrl(instanceInfo.getHomePageUrl()).build();
	}

	private String append(String mgmtUrl, String mgmtPath) {
		if (mgmtUrl.endsWith("/")) {
			mgmtUrl = mgmtUrl.substring(0, mgmtUrl.length() - 1);
		}

		if (!mgmtPath.startsWith("/")) {
			mgmtPath = "/" + mgmtPath;
		}

		return mgmtUrl + mgmtPath;
	}
}
