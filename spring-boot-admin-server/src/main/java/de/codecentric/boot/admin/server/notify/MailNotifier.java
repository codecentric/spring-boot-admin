/*
 * Copyright 2014-2018 the original author or authors.
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
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * Notifier sending emails.
 *
 * @author Johannes Edmeier
 */
public class MailNotifier extends AbstractStatusChangeNotifier {
    private final MailSender sender;

    /**
     * recipients of the mail
     */
    private String[] to = {"root@localhost"};

    /**
     * cc-recipients of the mail
     */
    private String[] cc;

    /**
     * sender of the change
     */
    private String from = null;

    /**
     * Mail Subject. SpEL template using event as root;
     */
    private String subject;

    /**
     * Mail Text. Is prepared via thymeleaf template;
     */
    private TemplateEngine templateEngine;

    public MailNotifier(MailSender sender, InstanceRepository repository, TemplateEngine templateEngine) {
        super(repository);
        this.sender = sender;
        this.templateEngine = templateEngine;
    }

    @Override
    protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
        final Context ctx = new Context();
        ctx.setVariable("event", event);
        ctx.setVariable("instance", instance);
        ctx.setVariable("lastStatus", getLastStatus(event.getInstance()));

        final SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(from);
        message.setSubject(templateEngine.process("notification-template-subject.txt", ctx));
        message.setText(templateEngine.process("notification-template-body.html", ctx));
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
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }

}
