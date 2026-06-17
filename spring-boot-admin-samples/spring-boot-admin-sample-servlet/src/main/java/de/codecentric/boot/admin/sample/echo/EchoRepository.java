/*
 * Copyright 2014-2023 the original author or authors.
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

package de.codecentric.boot.admin.sample.echo;

import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.util.StringUtils.hasText;

/**
 * @author Cosimo Damiano Prete
 * @since 17/06/2026
 **/
public class EchoRepository {

	private static final EchoEntity DEFAULT = new EchoEntity("UP");

	private final AtomicReference<EchoEntity> value = new AtomicReference<>(DEFAULT);

	public EchoEntity save(String status, String details) {
		return value.updateAndGet((e) -> hasText(status) ? new EchoEntity(status.strip().toUpperCase(), details)
				: e.withDetails(details));
	}

	public EchoEntity get() {
		return value.get();
	}

}
