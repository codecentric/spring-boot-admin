package de.codecentric.boot.admin.notify.microsoft.teams;

import java.util.HashMap;
import java.util.Map;


public class Section {

    private String activityTitle;
    private String activitySubtitle;
    private String activityImage;
    private Map<String, String> facts = new HashMap<>();
    private String text;

    public Section(String activityTitle, String activitySubtitle, String activityImage, Map<String, String> facts, String text) {
        this.activityTitle = activityTitle;
        this.activitySubtitle = activitySubtitle;
        this.activityImage = activityImage;
        this.facts = facts;
        this.text = text;
    }

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

    public Map<String, String> getFacts() {
        return facts;
    }

    public void setFacts(Map<String, String> facts) {
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