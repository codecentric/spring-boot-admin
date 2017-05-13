/*
 * Copyright 2013-2016 the original author or authors.
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
package spring.boot.admin.turbine.web;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.codecentric.boot.admin.web.AdminController;

/**
 * Provides informations for the turbine view. Only available clusters until now.
 * 
 * @author Johannes Edmeier
 * @author Jérôme Mirc
 */
@AdminController
@ResponseBody
@RequestMapping("/api/turbine")
public class TurbineController {
	private final DiscoveryClient discoveryClient;

	public TurbineController(DiscoveryClient discoveryClient) {
		this.discoveryClient = discoveryClient;
	}

	@RequestMapping(value = "/clusters", method = RequestMethod.GET)
	public Map<String, ?> getClusters() {
		String[] clusterNames = new String[]{"default"};

		List<String> services = discoveryClient.getServices();

		if (services != null) {
			clusterNames = services.toArray(new String[0]);
		}

		Arrays.sort(clusterNames);
		return Collections.singletonMap("clusters", clusterNames);
	}

}
