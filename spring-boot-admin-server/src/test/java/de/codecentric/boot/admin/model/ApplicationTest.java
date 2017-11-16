package de.codecentric.boot.admin.model;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class ApplicationTest {

    @Test
    public void test_equals_hashCode() {
        Application a1 = Application.create("foo")
                                    .withHealthUrl("healthUrl")
                                    .withManagementUrl("mgmt")
                                    .withServiceUrl("svc")
                                    .withId("id")
                                    .build();
        Application a2 = Application.create("foo")
                                    .withHealthUrl("healthUrl")
                                    .withManagementUrl("mgmt")
                                    .withServiceUrl("svc")
                                    .withId("id")
                                    .build();

        assertThat(a1, is(a2));
        assertThat(a1.hashCode(), is(a2.hashCode()));

        Application a3 = Application.create("foo")
                                    .withHealthUrl("healthUrl2")
                                    .withManagementUrl("mgmt")
                                    .withServiceUrl("svc")
                                    .withId("other")
                                    .build();

        assertThat(a1, not(is(a3)));
        assertThat(a2, not(is(a3)));
    }

    @Test
    public void test_builder_copy() {
        Application app = Application.create("App")
                                     .withId("-id-")
                                     .withHealthUrl("http://health")
                                     .withManagementUrl("http://mgmgt")
                                     .withServiceUrl("http://svc")
                                     .withStatusInfo(StatusInfo.ofUp())
                                     .build();
        Application copy = Application.copyOf(app).build();
        assertThat(app, is(copy));
    }
}