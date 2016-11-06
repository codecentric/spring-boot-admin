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
package spring.boot.admin.turbine.config;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Condition for enabling the Turbine components.
 * 
 * @author Johannes Edmeier
 */
public class TurbineEnabledCondition extends AllNestedConditions {

	public TurbineEnabledCondition() {
		super(ConfigurationPhase.PARSE_CONFIGURATION);
	}

	@ConditionalOnProperty(value = "spring.boot.admin.turbine.enabled", matchIfMissing = true)
	static class EnabledProperty {
	}

	@ConditionalOnProperty(value = "spring.boot.admin.turbine.url", matchIfMissing = false)
	static class TurbinUrlProperty {
	}

}