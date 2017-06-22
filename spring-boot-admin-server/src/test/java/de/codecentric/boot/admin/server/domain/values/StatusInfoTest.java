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

package de.codecentric.boot.admin.server.domain.values;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StatusInfoTest {

    @Test
    public void test_isMethods() {
        assertThat(StatusInfo.valueOf("FOO").isUp()).isFalse();
        assertThat(StatusInfo.valueOf("FOO").isDown()).isFalse();
        assertThat(StatusInfo.valueOf("FOO").isUnknown()).isFalse();
        assertThat(StatusInfo.valueOf("FOO").isOffline()).isFalse();

        assertThat(StatusInfo.ofUp().isUp()).isTrue();
        assertThat(StatusInfo.ofUp().isDown()).isFalse();
        assertThat(StatusInfo.ofUp().isUnknown()).isFalse();
        assertThat(StatusInfo.ofUp().isOffline()).isFalse();

        assertThat(StatusInfo.ofDown().isUp()).isFalse();
        assertThat(StatusInfo.ofDown().isDown()).isTrue();
        assertThat(StatusInfo.ofDown().isUnknown()).isFalse();
        assertThat(StatusInfo.ofDown().isOffline()).isFalse();

        assertThat(StatusInfo.ofUnknown().isUp()).isFalse();
        assertThat(StatusInfo.ofUnknown().isDown()).isFalse();
        assertThat(StatusInfo.ofUnknown().isUnknown()).isTrue();
        assertThat(StatusInfo.ofUnknown().isOffline()).isFalse();

        assertThat(StatusInfo.ofOffline().isUp()).isFalse();
        assertThat(StatusInfo.ofOffline().isDown()).isFalse();
        assertThat(StatusInfo.ofOffline().isUnknown()).isFalse();
        assertThat(StatusInfo.ofOffline().isOffline()).isTrue();
    }

}