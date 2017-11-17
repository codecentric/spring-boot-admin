package de.codecentric.boot.admin.notify.microsoft.teams;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class MessageTest {
    @Test
    public void test_equalsWithTwoLikeMessagesReturns_true() throws Exception {
        Message messageOne = new Message();
        messageOne.setTitle("TITLE1");
        messageOne.setSummary("SUMMARY1");
        messageOne.setThemeColor("THEME1");

        Message messageTwo = new Message();
        messageTwo.setTitle("TITLE1");
        messageTwo.setSummary("SUMMARY1");
        messageTwo.setThemeColor("THEME1");

        assertEquals("The equals method doesn't work", messageOne, messageTwo);
    }

    @Test
    public void test_equalsOfTwoNonEqualMessagesReturns_false() throws Exception {
        Message messageOne = new Message();
        messageOne.setTitle("TITLE1");
        messageOne.setSummary("SUMMARY1");
        messageOne.setThemeColor("THEME1");

        Message messageTwo = new Message();
        messageTwo.setTitle("TITLE2");
        messageTwo.setSummary("SUMMARY1");
        messageTwo.setThemeColor("THEME1");

        assertNotEquals("The equals method doesn't work", messageOne, messageTwo);
    }

    @Test
    public void test_equalsOfMessageToNonMessageObjectReturns_false() throws Exception {
        Message messageOne = new Message();
        messageOne.setTitle("TITLE1");
        messageOne.setSummary("SUMMARY1");
        messageOne.setThemeColor("THEME1");

        Object messageTwo = new Object();

        assertNotEquals("The equals method matched when it shouldn't", messageOne, messageTwo);
    }

    @Test
    public void test_hashCodeOnEqualObjectsReturns_sameHash() throws Exception {
        Message messageOne = new Message();
        messageOne.setTitle("TITLE1");
        messageOne.setSummary("SUMMARY1");
        messageOne.setThemeColor("THEME1");

        Message messageTwo = new Message();
        messageTwo.setTitle("TITLE1");
        messageTwo.setSummary("SUMMARY1");
        messageTwo.setThemeColor("THEME1");

        assertEquals("The equals method doesn't work", messageOne, messageTwo);

        assertEquals("The hash method doesn't match for equals ", messageOne.hashCode(), messageTwo.hashCode());
    }

    @Test
    public void test_hashCodeOnDifferentObjectsReturns_differentHash() throws Exception {
        Message messageOne = new Message();
        messageOne.setTitle("TITLE1");
        messageOne.setSummary("SUMMARY1");
        messageOne.setThemeColor("THEME1");

        Message messageTwo = new Message();
        messageTwo.setTitle("TITLE2");
        messageTwo.setSummary("SUMMARY1");
        messageTwo.setThemeColor("THEME1");

        assertNotEquals("The equals method doesn't work", messageOne, messageTwo);

        assertNotEquals("The hash method matches for non-equal", messageOne.hashCode(), messageTwo.hashCode());
    }

}