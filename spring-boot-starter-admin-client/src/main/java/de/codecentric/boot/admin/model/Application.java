/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.util.Assert;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.net.URL;

/**
 * The domain model for all registered application at the spring boot admin application.
 */
@Immutable
@JsonDeserialize(builder = Application.ApplicationBuilder.class)
public class Application implements Serializable {

	private static final long serialVersionUID = 1L;

    public Application(ApplicationBuilder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.version = builder.version;
        this.hostname = builder.hostname;
        this.ipAddress = builder.ipAddress;
        this.url = builder.url;
    }

    // mandatory fields
    @NotNull
    private final String name;

    @NotNull
    private final URL url;

    // optional fields
    private final Long id;
    private final String hostname;
    private final String ipAddress;
    private final String version;

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getHostname() {
        return hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Long getId() {
		return id;
	}

	public URL getUrl() {
		return url;
	}

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Application{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", version='").append(version).append('\'');
        sb.append(", hostname='").append(hostname).append('\'');
        sb.append(", ipAddress='").append(ipAddress).append('\'');
        sb.append(", url='").append(url).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Application that = (Application) o;

        if (hostname != null ? !hostname.equals(that.hostname) : that.hostname != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (ipAddress != null ? !ipAddress.equals(that.ipAddress) : that.ipAddress != null) return false;
        if (!name.equals(that.name)) return false;
        if (!url.equals(that.url)) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + url.hashCode();
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (hostname != null ? hostname.hashCode() : 0);
        result = 31 * result + (ipAddress != null ? ipAddress.hashCode() : 0);
        return result;
    }

    /**
     * Builder for {@link Application} objects.
     */
    public static class ApplicationBuilder {
        // mandatory fields
        private final String name;
        private final URL url;

        // optional fields
        private Long id;
        private String version;
        private String hostname;
        private String ipAddress;

        /**
         * Copies an existing {@link Application}.
         *
         * @param application Existing application
         */
        public ApplicationBuilder(Application application) {
            this.version = application.getVersion();
            this.name = application.getName();
            this.url = application.getUrl();
            this.hostname = application.getHostname();
            this.ipAddress = application.getIpAddress();
        }

        /**
         * Constructs a new {@code ApplicationBuilder}.
         *
         * @param name The name of this application.
         * @param url The url of the Spring Boot Actuator endpoints
         */
        @JsonCreator
        public ApplicationBuilder(@JsonProperty("name") String name, @JsonProperty("url") URL url) {
            Assert.notNull(name, "name must not be null");
            Assert.notNull(url, "url must not be null");
            this.name = name;
            this.url = url;
        }

        public ApplicationBuilder withId(Long id) {
            Assert.notNull(name, "id must not be null");
            this.id = id;
            return this;
        }
        public ApplicationBuilder withHostname(String hostname) {
            Assert.notNull(name, "hostname must not be null");
            this.hostname = hostname;
            return this;
        }
        public ApplicationBuilder withIpAddress(String ipAddress) {
            Assert.notNull(name, "ipAddress must not be null");
            this.ipAddress = ipAddress;
            return this;
        }
        public ApplicationBuilder withVersion(String version) {
            //Assert.notNull(version, "version must not be null");
            this.version = version;
            return this;
        }
        /**
         * Returns a newly build Application.
         *
         * @return A newly build Application.
         */
        public Application build() {
            return new Application(this);
        }

    }

}
