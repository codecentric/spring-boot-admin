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

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.StringUtils.hasText;

/**
 * This controller exposes REST resources under '/echo'.
 *
 * @author Cosimo Damiano Prete
 * @since 17/06/2026
 **/
@RestController
@RequestMapping("/echo")
public class EchoController {

	private final EchoRepository repository;

	public EchoController(EchoRepository repository) {
		this.repository = repository;
	}

	/**
	 * Allows to get the latest recorded echo value or to set a new one and return it if a
	 * value for {@code status} or {@code details} is provided.
	 * <p>
	 * While this endpoint breaks quite some principles (SRP, invalid REST resource and so
	 * on), it has been designed in this way so that it can be called by just typing it in
	 * the browser address bar without the need of any other additional or external tools
	 * (e.g.: Postman, cURL and so on).
	 * @param status the new status to set. For example: UP, DOWN, OUT_OF_SERVICE,
	 * UNKNOWN.
	 * @param details the new details to set. For example: "Database is down", "Disk space
	 * is low" and so on.
	 * @return the latest recorded echo value or the new one if a value for {@code status}
	 * or {@code details} is provided.
	 */
	@GetMapping(produces = APPLICATION_JSON_VALUE)
	public EchoEntity echo(@RequestParam(required = false) String status,
			@RequestParam(required = false) String details) {
		return (hasText(status) || hasText(details)) ? repository.save(status, details) : repository.get();
	}

}
