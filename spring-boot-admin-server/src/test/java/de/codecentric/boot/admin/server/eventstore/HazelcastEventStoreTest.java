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

package de.codecentric.boot.admin.server.eventstore;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.test.TestHazelcastInstanceFactory;

public class HazelcastEventStoreTest extends AbstractEventStoreTest {

    @Override
    protected InstanceEventStore createStore(int maxLogSizePerAggregate) {
        HazelcastInstance hazelcast = new TestHazelcastInstanceFactory(1).newHazelcastInstance();
        return new HazelcastEventStore(maxLogSizePerAggregate, hazelcast.getMap("testList"));
    }

}
