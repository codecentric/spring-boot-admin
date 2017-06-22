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

import de.codecentric.boot.admin.server.domain.entities.Application;
import de.codecentric.boot.admin.server.domain.entities.ApplicationRepository;
import de.codecentric.boot.admin.server.domain.events.ClientApplicationEvent;
import de.codecentric.boot.admin.server.domain.events.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.ApplicationId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class MailNotifierTest {
    private final Application application = Application.create(ApplicationId.of("-id-"))
                                                       .register(Registration.create("App", "http://health").build());
    private MailSender sender;
    private MailNotifier notifier;
    private ApplicationRepository repository;

    @Before
    public void setup() {
        repository = mock(ApplicationRepository.class);
        when(repository.find(application.getId())).thenReturn(Mono.just(application));

        sender = mock(MailSender.class);

        notifier = new MailNotifier(sender, repository);
        notifier.setTo(new String[]{"foo@bar.com"});
        notifier.setCc(new String[]{"bar@foo.com"});
        notifier.setFrom("SBA <no-reply@example.com>");
        notifier.setSubject("#{application.id} is #{event.statusInfo.status}");
    }

    @Test
    public void test_onApplicationEvent() {
        StepVerifier.create(notifier.notify(
                new ClientApplicationStatusChangedEvent(application.getId(), application.getVersion(),
                        StatusInfo.ofDown()))).verifyComplete();
        StepVerifier.create(notifier.notify(
                new ClientApplicationStatusChangedEvent(application.getId(), application.getVersion(),
                        StatusInfo.ofUp()))).verifyComplete();

        SimpleMailMessage expected = new SimpleMailMessage();
        expected.setTo(new String[]{"foo@bar.com"});
        expected.setCc(new String[]{"bar@foo.com"});
        expected.setFrom("SBA <no-reply@example.com>");
        expected.setText("App (-id-)\nstatus changed from DOWN to UP\n\nhttp://health");
        expected.setSubject("-id- is UP");

        verify(sender).send(eq(expected));
    }

    // The following tests are rather for AbstractNotifier

    @Test
    public void test_onApplicationEvent_disbaled() {
        notifier.setEnabled(false);
        notifier.notify(new ClientApplicationStatusChangedEvent(application.getId(), application.getVersion(),
                StatusInfo.ofUp()));

        verifyNoMoreInteractions(sender);
    }

    @Test
    public void test_onApplicationEvent_noSend() {
        notifier.notify(new ClientApplicationStatusChangedEvent(application.getId(), application.getVersion(),
                StatusInfo.ofUp()));

        verifyNoMoreInteractions(sender);
    }

    @Test
    public void test_onApplicationEvent_noSend_wildcard() {
        notifier.setIgnoreChanges(new String[]{"*:UP"});
        notifier.notify(new ClientApplicationStatusChangedEvent(application.getId(), application.getVersion(),
                StatusInfo.ofUp()));

        verifyNoMoreInteractions(sender);
    }

    @Test
    public void test_onApplicationEvent_throw_doesnt_propagate() {
        Notifier notifier = new AbstractStatusChangeNotifier(repository) {
            @Override
            protected Mono<Void> doNotify(ClientApplicationEvent event, Application application) {
                return Mono.error(new IllegalStateException("test"));
            }
        };
        notifier.notify(new ClientApplicationStatusChangedEvent(application.getId(), application.getVersion(),
                StatusInfo.ofUp()));
    }
}
