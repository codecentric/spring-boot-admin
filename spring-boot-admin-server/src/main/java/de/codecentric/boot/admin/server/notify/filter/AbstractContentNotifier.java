/*
 * Copyright 2014-2025 the original author or authors.
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

package de.codecentric.boot.admin.server.notify.filter;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.DataBindingPropertyAccessor;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.notify.AbstractStatusChangeNotifier;

/**
 * Base class for notifiers that generate message content from templates using Spring
 * Expression Language (SpEL).
 * <p>
 * This class provides a framework for creating custom notifiers that format notification
 * messages using SpEL templates with access to instance event data. Subclasses must
 * implement two methods:
 * <ul>
 * <li>{@link #buildContentModel(InstanceEvent, Instance)} - Provide the data model for
 * template evaluation</li>
 * <li>{@link #getDefaultMessage()} - Define the default SpEL template string</li>
 * </ul>
 * <p>
 * <b>Usage Example:</b> <pre>{@code
 * public class EmailNotifier extends AbstractContentNotifier {
 *     public EmailNotifier(InstanceRepository repository) {
 *         super(repository);
 *     }
 *
 *     &#64;Override
 *     protected Map<String, Object> getContent(InstanceEvent event, Instance instance) {
 *         var content = super.getContent(event, instance);
 *         content.put("customContent", "Hello, World!");
 *         return content;
 *     }
 *
 *

&#64;Override
 *     protected String getDefaultMessage() {
 *         return "#{name} is #{status} at #{url}";
 *     }
 * }
 * }</pre>
 * <p>
 * The message template can be customized at runtime using {@link #setMessage(String)}.
 */
public abstract class AbstractContentNotifier extends AbstractStatusChangeNotifier {

	private Expression message;

	private final SpelExpressionParser parser = new SpelExpressionParser();

	public AbstractContentNotifier(InstanceRepository repository) {
		super(repository);

		this.message = this.parser.parseExpression(getDefaultMessage(), ParserContext.TEMPLATE_EXPRESSION);
	}

	public String getMessage() {
		return this.message.getExpressionString();
	}

	public void setMessage(String message) {
		this.message = this.parser.parseExpression(message, ParserContext.TEMPLATE_EXPRESSION);
	}

	/**
	 * Generates the notification message content by evaluating the SpEL template with
	 * event and instance data.
	 * <p>
	 * This method combines the configured message template with the data provided by
	 * {@link #buildContentModel(InstanceEvent, Instance)} to produce the final
	 * notification text.
	 * @param event the instance event that triggered the notification
	 * @param instance the instance associated with the event
	 * @return the evaluated message content as a string
	 */
	protected String createContent(InstanceEvent event, Instance instance) {
		SimpleEvaluationContext context = SimpleEvaluationContext
			.forPropertyAccessors(DataBindingPropertyAccessor.forReadOnlyAccess(), new MapAccessor())
			.withRootObject(buildContentModel(event, instance))
			.build();
		return this.message.getValue(context, String.class);
	}

	/**
	 * Provides the data model used for evaluating the message template.
	 * <p>
	 * The returned map contains key-value pairs that can be referenced in the SpEL
	 * template using #{key} syntax. For example, if the map contains {"name": "MyApp",
	 * "status": "UP"}, the template "#{name} is #{status}" would evaluate to "MyApp is
	 * UP".
	 * @param event the instance event containing event-specific data
	 * @param instance the instance containing registration and status information
	 * @return a map of template variables and their values
	 */
	protected Map<String, Object> buildContentModel(InstanceEvent event, Instance instance) {
		Map<String, Object> content = new HashMap<>();
		content.put("name", instance.getRegistration().getName());
		content.put("id", instance.getId().getValue());
		content.put("status", (event instanceof InstanceStatusChangedEvent statusChangedEvent)
				? statusChangedEvent.getStatusInfo().getStatus() : "UNKNOWN");
		content.put("lastStatus", getLastStatus(event.getInstance()));

		return content;
	}

	/**
	 * Defines the default SpEL template string used for message generation.
	 * <p>
	 * The template should use #{key} syntax to reference variables provided by
	 * {@link #buildContentModel(InstanceEvent, Instance)}. This default can be overridden
	 * at runtime using {@link #setMessage(String)}.
	 * @return the default SpEL template string (e.g., "#{name} is #{status}")
	 */
	protected abstract String getDefaultMessage();

}
