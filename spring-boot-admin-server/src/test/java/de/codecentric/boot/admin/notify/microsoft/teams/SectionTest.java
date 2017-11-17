package de.codecentric.boot.admin.notify.microsoft.teams;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class SectionTest {
    @Test
    public void test_equalsWithTwoEqualSectionsReturns_true() throws Exception {
        Section sectionOne = new Section();
        sectionOne.setActivityImage("IMAGE1");
        sectionOne.setActivitySubtitle("SUBTITLE1");
        sectionOne.setActivityTitle("TITLE1");
        sectionOne.setText("TEXT1");

        Section sectionTwo = new Section();
        sectionTwo.setActivityImage("IMAGE1");
        sectionTwo.setActivitySubtitle("SUBTITLE1");
        sectionTwo.setActivityTitle("TITLE1");
        sectionTwo.setText("TEXT1");

        assertEquals("The equals method doesn't work", sectionOne, sectionTwo);
    }

    @Test
    public void test_equalsWithTwoDifferentSectionsReturns_false() throws Exception {
        Section sectionOne = new Section();
        sectionOne.setActivityImage("IMAGE1");
        sectionOne.setActivitySubtitle("SUBTITLE1");
        sectionOne.setActivityTitle("TITLE1");
        sectionOne.setText("TEXT1");

        Section sectionTwo = new Section();
        sectionTwo.setActivityImage("IMAGE2");
        sectionTwo.setActivitySubtitle("SUBTITLE1");
        sectionTwo.setActivityTitle("TITLE1");
        sectionTwo.setText("TEXT1");

        assertNotEquals("The equals method matched when it shouldn't", sectionOne, sectionTwo);
    }

    @Test
    public void test_equalsSectionAndNonSectionReturns_false() throws Exception {
        Section sectionOne = new Section();
        sectionOne.setActivityImage("IMAGE1");
        sectionOne.setActivitySubtitle("SUBTITLE1");
        sectionOne.setActivityTitle("TITLE1");
        sectionOne.setText("TEXT1");

        Object sectionTwo = new Object();

        assertNotEquals("The equals method matched when it shouldn't", sectionOne, sectionTwo);


    }

    @Test
    public void test_hashCodeForTwoEqualSectionsReturns_sameHash() throws Exception {
        Section sectionOne = new Section();
        sectionOne.setActivityImage("IMAGE1");
        sectionOne.setActivitySubtitle("SUBTITLE1");
        sectionOne.setActivityTitle("TITLE1");
        sectionOne.setText("TEXT1");

        Section sectionTwo = new Section();
        sectionTwo.setActivityImage("IMAGE1");
        sectionTwo.setActivitySubtitle("SUBTITLE1");
        sectionTwo.setActivityTitle("TITLE1");
        sectionTwo.setText("TEXT1");

        assertEquals("The hashCode method doesn't match", sectionOne.hashCode(), sectionTwo.hashCode());
    }

    @Test
    public void test_hashCodeForTwoDifferentSectionsReturns_differentHash() throws Exception {
        Section sectionOne = new Section();
        sectionOne.setActivityImage("IMAGE1");
        sectionOne.setActivitySubtitle("SUBTITLE1");
        sectionOne.setActivityTitle("TITLE1");
        sectionOne.setText("TEXT1");

        Section sectionTwo = new Section();
        sectionTwo.setActivityImage("IMAGE2");
        sectionTwo.setActivitySubtitle("SUBTITLE1");
        sectionTwo.setActivityTitle("TITLE1");
        sectionTwo.setText("TEXT1");

        assertNotEquals("The hashCode method matched when it shouldn't",
                sectionOne.hashCode(), sectionTwo.hashCode());
    }

}