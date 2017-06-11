package de.codecentric.boot.admin.server.model;

import java.io.Serializable;
import org.springframework.util.Assert;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public final class ApplicationId implements Serializable {
    private final String value;

    private ApplicationId(String value) {
        this.value = value;
    }

    @JsonCreator
    public static ApplicationId of(String id) {
        Assert.hasText(id, "'id' must have text");
        return new ApplicationId(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ApplicationId that = (ApplicationId) o;

        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}
