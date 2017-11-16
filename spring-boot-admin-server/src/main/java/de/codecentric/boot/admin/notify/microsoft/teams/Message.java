package de.codecentric.boot.admin.notify.microsoft.teams;

import java.util.ArrayList;
import java.util.List;


public class Message {

    private String summary;
    private String themeColor;
    private String title;
    private List<Section> sections = new ArrayList<>();

    public Message(String summary, String themeColor, String title, List<Section> sections) {

        this.summary = summary;
        this.themeColor = themeColor;
        this.title = title;
        this.sections = sections;
    }

    public Message() {

    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getThemeColor() {
        return themeColor;
    }

    public void setThemeColor(String themeColor) {
        this.themeColor = themeColor;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Message)){
            return false;
        }

        Message message = (Message)obj;

        return  (this.getSummary() == message.getSummary() || (
                    this.getSummary() != null && this.getSummary().equals(message.getSummary())
                )) &&
                (this.getThemeColor() == message.getThemeColor() || (
                    this.getThemeColor() != null && this.getThemeColor().equals(message.getThemeColor())
                )) &&
                (this.getTitle() == message.getTitle() || (
                    this.getTitle() != null && this.getTitle().equals(message.getTitle())
                )) &&
                this.getSections().equals(message.getSections());
    }
}
