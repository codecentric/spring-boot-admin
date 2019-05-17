/*
 * Copyright 2014-2019 the original author or authors.
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

package de.codecentric.boot.admin.server.cloud.discovery;

import org.springframework.cloud.client.ServiceInstance;

import static org.springframework.util.StringUtils.isEmpty;

public class KubernetesServiceInstanceConverter extends DefaultServiceInstanceConverter {

    @Override
    protected String getManagementPort(ServiceInstance instance) {
        String managementPort = instance.getMetadata().get("port.management");
        if (!isEmpty(managementPort)) {
            return managementPort;
        }
        return super.getManagementPort(instance);
    }
}
