package de.codecentric.boot.admin.server.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StatusInfoTest {

    @Test
    public void test_equals_hashcode() {
        StatusInfo up = StatusInfo.ofUp();
        StatusInfo up2 = StatusInfo.ofUp();
        StatusInfo down = StatusInfo.ofDown();

        assertThat(up).isEqualTo(up2).isNotEqualTo(down);
        assertThat(up.hashCode()).isEqualTo(up2.hashCode());
    }

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