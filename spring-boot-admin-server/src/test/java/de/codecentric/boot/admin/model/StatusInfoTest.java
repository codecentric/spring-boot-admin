package de.codecentric.boot.admin.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class StatusInfoTest {

	@Test
	public void test_equals_hashcode() {
		StatusInfo up = StatusInfo.ofUp();
		StatusInfo up2 = StatusInfo.ofUp();
		StatusInfo down = StatusInfo.ofDown();

		assertThat(up, is(up2));
		assertThat(up, not(is(down)));
		assertThat(up.hashCode(), is(up2.hashCode()));
	}

	@Test
	public void test_isMethods() {

		assertThat(StatusInfo.valueOf("FOO").isUp(), is(false));
		assertThat(StatusInfo.valueOf("FOO").isDown(), is(false));
		assertThat(StatusInfo.valueOf("FOO").isUnknown(), is(false));
		assertThat(StatusInfo.valueOf("FOO").isOffline(), is(false));

		assertThat(StatusInfo.ofUp().isUp(), is(true));
		assertThat(StatusInfo.ofUp().isDown(), is(false));
		assertThat(StatusInfo.ofUp().isUnknown(), is(false));
		assertThat(StatusInfo.ofUp().isOffline(), is(false));

		assertThat(StatusInfo.ofDown().isUp(), is(false));
		assertThat(StatusInfo.ofDown().isDown(), is(true));
		assertThat(StatusInfo.ofDown().isUnknown(), is(false));
		assertThat(StatusInfo.ofDown().isOffline(), is(false));

		assertThat(StatusInfo.ofUnknown().isUp(), is(false));
		assertThat(StatusInfo.ofUnknown().isDown(), is(false));
		assertThat(StatusInfo.ofUnknown().isUnknown(), is(true));
		assertThat(StatusInfo.ofUnknown().isOffline(), is(false));

		assertThat(StatusInfo.ofOffline().isUp(), is(false));
		assertThat(StatusInfo.ofOffline().isDown(), is(false));
		assertThat(StatusInfo.ofOffline().isUnknown(), is(false));
		assertThat(StatusInfo.ofOffline().isOffline(), is(true));
	}

}