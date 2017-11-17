package de.codecentric.boot.admin.notify.microsoft.teams;

import java.util.ArrayList;
import java.util.List;


public class Message {

    private String summary;
    private String themeColor;
    private String title;
    private List<Section> sections = new ArrayList<>();

    String getSummary() {
        return summary;
    }

    void setSummary(String summary) {
        this.summary = summary;
    }

    private String getThemeColor() {
        return themeColor;
    }

    void setThemeColor(String themeColor) {
        this.themeColor = themeColor;
    }

    String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    List<Section> getSections() {
        return sections;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Message)){
            return false;
        }

        Message message = (Message)obj;

        return  ((this.getSummary() == null &&  message.getSummary() == null) || (
                    this.getSummary() != null && this.getSummary().equals(message.getSummary())
                )) &&
                ((this.getThemeColor() == null &&  message.getThemeColor() == null) || (
                    this.getThemeColor() != null && this.getThemeColor().equals(message.getThemeColor())
                )) &&
                ((this.getTitle() == null &&  message.getTitle() == null) || (
                    this.getTitle() != null && this.getTitle().equals(message.getTitle())
                )) &&
                this.getSections().equals(message.getSections());
    }

    @Override
    public int hashCode() {
        return (getSummary() + getThemeColor() + getTitle()).hashCode() + getSections().hashCode();
    }
}
