/*
 * Copyright 2013-2014 the original author or authors.
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
package de.codecentric.boot.admin.notify;

import java.util.Arrays;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import de.codecentric.boot.admin.event.ClientApplicationEvent;

/**
 * Notifier sending emails.
 *
 * @author Johannes Edmeier
 */
public class MailNotifier extends AbstractStatusChangeNotifier {
	private final static String DEFAULT_SUBJECT = "#{application.name} (#{application.id}) is #{to.status}";
	private final static String DEFAULT_TEXT = "#{application.name} (#{application.id})\nstatus changed from #{from.status} to #{to.status}\n\n#{application.healthUrl}";

	private final SpelExpressionParser parser = new SpelExpressionParser();
	private final MailSender sender;

	/**
	 * recipients of the mail
	 */
	private String to[] = { "root@localhost" };

	/**
	 * cc-recipients of the mail
	 */
	private String cc[];

	/**
	 * sender of the change
	 */
	private String from = null;

	/**
	 * Mail Text. SpEL template using event as root;
	 */
	private Expression text;

	/**
	 * Mail Subject. SpEL template using event as root;
	 */
	private Expression subject;

	public MailNotifier(MailSender sender) {
		this.sender = sender;
		this.subject = parser.parseExpression(DEFAULT_SUBJECT, ParserContext.TEMPLATE_EXPRESSION);
		this.text = parser.parseExpression(DEFAULT_TEXT, ParserContext.TEMPLATE_EXPRESSION);
	}

	@Override
	protected void doNotify(ClientApplicationEvent event) {
		EvaluationContext context = new StandardEvaluationContext(event);

		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setFrom(from);
		message.setSubject(subject.getValue(context, String.class));
		message.setText(text.getValue(context, String.class));
		message.setCc(cc);

		sender.send(message);
	}

	public void setTo(String[] to) {
		this.to = Arrays.copyOf(to, to.length);
	}

	public void setCc(String[] cc) {
		this.cc = Arrays.copyOf(cc, cc.length);
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public void setSubject(String subject) {
		this.subject = parser.parseExpression(subject, ParserContext.TEMPLATE_EXPRESSION);
	}

	public void setText(String text) {
		this.text = parser.parseExpression(text, ParserContext.TEMPLATE_EXPRESSION);
	}

}
