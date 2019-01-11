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
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static java.util.Collections.singleton;

/**
 * Notifier sending emails using thymleaf templates.
 *
 * @author Johannes Edmeier
 */
public class MailNotifier extends AbstractStatusChangeNotifier {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    /**
     * recipients of the mail
     */
    private String[] to = {"root@localhost"};

    /**
     * cc-recipients of the mail
     */
    private String[] cc = {};

    /**
     * sender of the change
     */
    private String from = "Spring Boot Admin <noreply@localhost>";

    /**
     * Additional properties to be set for the template
     */
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * Base-URL used for hyperlinks in mail
     */
    @Nullable
    private String baseUrl;

    /**
     * Thymleaf template for mail
     */
    private String template = "classpath:/META-INF/spring-boot-admin-server/mail/status-changed.html";

    public MailNotifier(JavaMailSender mailSender, InstanceRepository repository, TemplateEngine templateEngine) {
        super(repository);
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
        return Mono.fromRunnable(() -> {
            Context ctx = new Context();
            ctx.setVariables(additionalProperties);
            ctx.setVariable("baseUrl", this.baseUrl);
            ctx.setVariable("event", event);
            ctx.setVariable("instance", instance);
            ctx.setVariable("lastStatus", getLastStatus(event.getInstance()));

            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
                message.setText(getBody(ctx).replaceAll("\\s+\\n", "\n"), true);
                message.setSubject(getSubject(ctx));
                message.setTo(this.to);
                message.setCc(this.cc);
                message.setFrom(this.from);
                mailSender.send(mimeMessage);
            } catch (MessagingException ex) {
                throw new RuntimeException("Error sending mail notification", ex);
            }
        });
    }

    protected String getBody(Context ctx) {
        return templateEngine.process(this.template, ctx);
    }

    protected String getSubject(Context ctx) {
        return templateEngine.process(this.template, singleton("subject"), ctx).trim();
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

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    @Nullable
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(@Nullable  String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
}
