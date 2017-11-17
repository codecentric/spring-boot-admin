package de.codecentric.boot.admin.notify.microsoft.teams;

import java.util.ArrayList;
import java.util.List;


public class Section {

    private String activityTitle;
    private String activitySubtitle;
    private String activityImage;
    private List<Fact> facts = new ArrayList<>();
    private String text;

    private String getActivityTitle() {

        return activityTitle;
    }

    void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }

    private String getActivitySubtitle() {
        return activitySubtitle;
    }

    void setActivitySubtitle(String activitySubtitle) {
        this.activitySubtitle = activitySubtitle;
    }

    private String getActivityImage() {
        return activityImage;
    }

    void setActivityImage(String activityImage) {
        this.activityImage = activityImage;
    }

    List<Fact> getFacts() {
        return facts;
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

        return ((this.getActivitySubtitle() == null && section.getActivitySubtitle() == null) || (
                        this.getActivitySubtitle() != null && this.getActivitySubtitle().equals(section.getActivitySubtitle())
                )) &&
                ((this.getActivityImage() == null && section.getActivityImage() == null) || (
                        this.getActivityImage() != null
                                && this.getActivityImage().equals(section.getActivityImage())
                )) &&
                ((this.getActivityTitle() == null &&  section.getActivityTitle() == null) || (
                        this.getActivityTitle() != null
                                && this.getActivityTitle().equals(section.getActivityTitle())
                ))
                && this.getFacts().equals(section.getFacts());
    }

    @Override
    public int hashCode() {
        return (getActivitySubtitle() + getActivityImage() + getActivityTitle()).hashCode() + getFacts().hashCode();
    }
}