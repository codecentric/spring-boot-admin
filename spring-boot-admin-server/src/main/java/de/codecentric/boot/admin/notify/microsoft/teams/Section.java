package de.codecentric.boot.admin.notify.microsoft.teams;

import java.util.ArrayList;
import java.util.List;


public class Section {

    private String activityTitle;
    private String activitySubtitle;
    private String activityImage;
    private List<Fact> facts = new ArrayList<Fact>();
    private String text;


    public Section() {

    }

    public String getActivityTitle() {

        return activityTitle;
    }

    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }

    public String getActivitySubtitle() {
        return activitySubtitle;
    }

    public void setActivitySubtitle(String activitySubtitle) {
        this.activitySubtitle = activitySubtitle;
    }

    public String getActivityImage() {
        return activityImage;
    }

    public void setActivityImage(String activityImage) {
        this.activityImage = activityImage;
    }

    public List<Fact> getFacts() {
        return facts;
    }

    public void setFacts(List<Fact> facts) {
        this.facts = facts;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Section)) {
            return false;
        }
        Section section = (Section) obj;

        return (this.getActivitySubtitle() == section.getActivitySubtitle() || (
                        this.getActivitySubtitle() != null && this.getActivitySubtitle().equals(section.getActivitySubtitle())
                )) &&
                (this.getActivityImage() == section.getActivityImage() || (
                        this.getActivityImage() != null
                                && this.getActivityImage().equals(section.getActivityImage())
                )) &&
                (this.getActivityTitle() == section.getActivityTitle() || (
                        this.getActivityTitle() != null
                                && this.getActivityTitle().equals(section.getActivityTitle())
                ))
                && this.getFacts().equals(section.getFacts());
    }

}