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
package de.codecentric.boot.admin.event;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;

/**
 * This event gets emitted when an application is registered.
 *
 * @author Johannes Stelzer
 */
public class ClientApplicationStatusChangedEvent extends ClientApplicationEvent {
	private static final long serialVersionUID = 1L;
	private final StatusInfo from;
	private final StatusInfo to;
	private final Map<String, Map<String, Object>> details;

	public ClientApplicationStatusChangedEvent(Application application, StatusInfo from,
			StatusInfo to) {
		super(application, "STATUS_CHANGE");
		this.from = from;
		this.to = to;
		this.details = null;
	}

	public ClientApplicationStatusChangedEvent(Application application, StatusInfo from,
			StatusInfo to, Map<String, Map<String, Object>> details) {
		super(application, "STATUS_CHANGE");
		this.from = from;
		this.to = to;
		this.details = details;
	}

	public StatusInfo getFrom() {
		return from;
	}

	public StatusInfo getTo() {
		return to;
	}

	/**
	 * @return {@link Health} details if any, never <code>null</code>
	 */
	public Map<String, Map<String, Object>> getDetails() {
		if (null == details)
			return Collections.emptyMap();
		return details;
	}

	public boolean hasDetails() {
		return !getDetails().isEmpty();
	}

	/**
	 * Helper for easy consumption by notifier in a SPEL context<br>
	 * More @see
	 * http://docs.spring.io/spring-framework/docs/current/spring-framework-reference/htmlsingle/#expressions
	 * 
	 * @return formated string, never <code>null</code>
	 */
	public String getDetailsFormatted() {
		StringBuffer buf = new StringBuffer("\n");
		Iterator<Entry<String, Map<String, Object>>> iter = getDetails().entrySet().iterator();
		if (iter.hasNext()) {
			while (iter.hasNext()) {
				Entry<String, Map<String, Object>> entry = iter.next();
				buf.append(entry.getKey()).append(":\n");
				for (Entry<String, Object> healthDetails : entry.getValue().entrySet()) {
					buf.append("\t").append(healthDetails.getKey()).append(":\n");
					buf.append("\t\t").append(healthDetails.getValue()).append("\n");
				}
			}
		}
		return buf.toString();
	}
}
