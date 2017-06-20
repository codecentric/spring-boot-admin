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
import de.codecentric.boot.admin.server.model.ApplicationId;
import de.codecentric.boot.admin.server.model.StatusInfo;

import java.util.Arrays;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CompositeNotifierTest {
    private static final ClientApplicationEvent APP_DOWN = new ClientApplicationStatusChangedEvent(
            ApplicationId.of("-"), StatusInfo.ofDown());

    @Test(expected = IllegalArgumentException.class)
    public void test_ctor_assert() {
        new CompositeNotifier(null);
    }

    @Test
    public void test_all_notifiers_get_notified() throws Exception {
        TestNotifier notifier1 = new TestNotifier();
        TestNotifier notifier2 = new TestNotifier();
        CompositeNotifier compositeNotifier = new CompositeNotifier(Arrays.asList(notifier1, notifier2));

        compositeNotifier.notify(APP_DOWN);

        assertThat(notifier1.getEvents()).containsOnly(APP_DOWN);
        assertThat(notifier2.getEvents()).containsOnly(APP_DOWN);
    }
}
