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

	public ClientApplicationStatusChangedEvent(Application application, StatusInfo from,
			StatusInfo to) {
		super(application);
		this.from = from;
		this.to = to;
	}

	public StatusInfo getFrom() {
		return from;
	}

	public StatusInfo getTo() {
		return to;
	}

	@Override
	public String getType() {
		return "STATUS_CHANGE";
	}
}
