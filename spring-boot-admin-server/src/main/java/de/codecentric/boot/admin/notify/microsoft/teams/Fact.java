package de.codecentric.boot.admin.notify.microsoft.teams;

public class Fact {

    private String name;
    private String value;

    public Fact(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof  Fact)) {
            return false;
        }

        Fact fact = (Fact)obj;

        return (this.getName() == fact.getName() || (
                this.getName() != null && this.getName().equals(fact.getName())
                )) &&
               (this.getValue() == fact.getValue() || (
                        this.getValue() != null && this.getValue().equals(fact.getValue())
                ));
    }

    @Override
    public int hashCode() {
        return getValue().hashCode() + getName().hashCode();
    }
}
