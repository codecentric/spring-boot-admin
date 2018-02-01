/*
 * Copyright 2014-2017 the original author or authors.
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
package de.codecentric.boot.admin.server.notify;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

/**
 * Notifier sending emails.
 *
 * @author Johannes Edmeier
 */
public class MailNotifier extends AbstractStatusChangeNotifier {
    private static final String DEFAULT_SUBJECT = "#{instance.registration.name} (#{instance.id}) is #{event.statusInfo.status}";
    private static final String DEFAULT_TEXT = "#{instance.registration.name} (#{instance.id})\nstatus changed from #{lastStatus} to #{event.statusInfo.status}\n\n#{instance.registration.healthUrl}";

    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final MailSender sender;

    /**
     * recipients of the mail
     */
    private String to[] = {"root@localhost"};

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

    public MailNotifier(MailSender sender, InstanceRepository repository) {
        super(repository);
        this.sender = sender;
        this.subject = parser.parseExpression(DEFAULT_SUBJECT, ParserContext.TEMPLATE_EXPRESSION);
        this.text = parser.parseExpression(DEFAULT_TEXT, ParserContext.TEMPLATE_EXPRESSION);
    }

    @Override
    protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
        Map<String, Object> root = new HashMap<>();
        root.put("event", event);
        root.put("instance", instance);
        root.put("lastStatus", getLastStatus(event.getInstance()));
        StandardEvaluationContext context = new StandardEvaluationContext(root);
        context.addPropertyAccessor(new MapAccessor());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(from);
        message.setSubject(subject.getValue(context, String.class));
        message.setText(text.getValue(context, String.class));
        message.setCc(cc);

        return Mono.fromRunnable(() -> sender.send(message));
    }

    public void setTo(String[] to) {
        this.to = Arrays.copyOf(to, to.length);
    }

    public String[] getTo() {
        return Arrays.copyOf(to, to.length);
    }

    public void setCc(String[] cc) {
        this.cc = Arrays.copyOf(cc, cc.length);
    }

    public String[] getCc() {
        return Arrays.copyOf(cc, cc.length);
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getFrom() {
        return from;
    }

    public void setSubject(String subject) {
        this.subject = parser.parseExpression(subject, ParserContext.TEMPLATE_EXPRESSION);
    }

    public String getSubject() {
        return subject.getExpressionString();
    }

    public void setText(String text) {
        this.text = parser.parseExpression(text, ParserContext.TEMPLATE_EXPRESSION);
    }

    public String getText() {
        return text.getExpressionString();
    }

}
