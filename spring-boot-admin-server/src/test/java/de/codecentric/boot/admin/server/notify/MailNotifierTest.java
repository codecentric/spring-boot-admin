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

import de.codecentric.boot.admin.server.event.ClientApplicationEvent;
import de.codecentric.boot.admin.server.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.ApplicationId;
import de.codecentric.boot.admin.server.model.Registration;
import de.codecentric.boot.admin.server.model.StatusInfo;
import de.codecentric.boot.admin.server.registry.store.ApplicationStore;

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
    private final Application application = Application.create(ApplicationId.of("-id-"),
            Registration.create("App", "http://health").build()).build();
    private MailSender sender;
    private MailNotifier notifier;
    private ApplicationStore store;

    @Before
    public void setup() {
        store = mock(ApplicationStore.class);
        when(store.find(application.getId())).thenReturn(application);

        sender = mock(MailSender.class);

        notifier = new MailNotifier(sender, store);
        notifier.setTo(new String[]{"foo@bar.com"});
        notifier.setCc(new String[]{"bar@foo.com"});
        notifier.setFrom("SBA <no-reply@example.com>");
        notifier.setSubject("#{application.id} is #{event.statusInfo.status}");
    }

    @Test
    public void test_onApplicationEvent() {
        notifier.notify(new ClientApplicationStatusChangedEvent(application.getId(), StatusInfo.ofDown()));
        notifier.notify(new ClientApplicationStatusChangedEvent(application.getId(), StatusInfo.ofUp()));

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
        notifier.notify(new ClientApplicationStatusChangedEvent(application.getId(), StatusInfo.ofUp()));

        verifyNoMoreInteractions(sender);
    }

    @Test
    public void test_onApplicationEvent_noSend() {
        notifier.notify(new ClientApplicationStatusChangedEvent(application.getId(), StatusInfo.ofUp()));

        verifyNoMoreInteractions(sender);
    }

    @Test
    public void test_onApplicationEvent_noSend_wildcard() {
        notifier.setIgnoreChanges(new String[]{"*:UP"});
        notifier.notify(new ClientApplicationStatusChangedEvent(application.getId(), StatusInfo.ofUp()));

        verifyNoMoreInteractions(sender);
    }

    @Test
    public void test_onApplicationEvent_throw_doesnt_propagate() {
        Notifier notifier = new AbstractStatusChangeNotifier(store) {
            @Override
            protected void doNotify(ClientApplicationEvent event, Application application) throws Exception {
                throw new IllegalStateException("test");
            }
        };
        notifier.notify(new ClientApplicationStatusChangedEvent(application.getId(), StatusInfo.ofUp()));
    }
}
