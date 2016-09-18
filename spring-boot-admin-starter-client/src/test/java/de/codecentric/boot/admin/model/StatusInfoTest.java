package de.codecentric.boot.admin.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class StatusInfoTest {

	@Test
	public void test_equals_hashcode() {
		StatusInfo up = StatusInfo.ofUp();
		StatusInfo up2 = StatusInfo.valueOf("UP", 0L);
		StatusInfo down = StatusInfo.valueOf("DOWN");

		assertThat(up, is(up2));
		assertThat(up, not(is(down)));
		assertThat(up.hashCode(), is(up2.hashCode()));
	}

}
