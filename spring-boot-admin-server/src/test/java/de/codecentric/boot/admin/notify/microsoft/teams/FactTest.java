package de.codecentric.boot.admin.notify.microsoft.teams;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class FactTest {


    @Test
    public void test_equalsOfTwoEqualObjectsReturns_true() throws Exception {
        Fact factOne = new Fact("name1", "value1");
        Fact factTwo = new Fact("name1", "value1");

        assertEquals("The equals method doesn't work", factOne, factTwo);
    }

    @Test
    public void test_equalsOfTwoNonEqualObjectsReturns_false() throws Exception {
        Fact factOne = new Fact("name1", "");
        Fact factTwo = new Fact("name1", "value1");

        assertNotEquals("The equals method doesn't work", factOne, factTwo);
    }

    @Test
    public void test_equalsOfFactToNotFactObjectReturns_false() throws Exception {
        Fact factOne = new Fact("name1", "value1");
        Object thing = new Object();

        assertNotEquals("The equals method matched when it shouldn't", factOne, thing);
    }

    @Test
    public void test_hashCodeOfTwoEqualObjectsReturns_sameHash() throws Exception {
        Fact factOne = new Fact("name1", "value1");
        Fact factTwo = new Fact("name1", "value1");

        assertEquals("The equals method doesn't work", factOne, factTwo);

        assertEquals("The hash method doesn't match for equals", factOne.hashCode(), factTwo.hashCode());
    }

    @Test
    public void test_hashCodeOfTwoDifferentObjectsReturns_differentHash() throws Exception {
        Fact factOne = new Fact("name1", "value1");
        Fact factTwo = new Fact("name2", "value1");

        assertNotEquals("The equals method doesn't work", factOne, factTwo);

        assertNotEquals("The hash method matches for non-equal", factOne.hashCode(), factTwo.hashCode());
    }

}