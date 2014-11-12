package de.codecentric.boot.admin;

import de.codecentric.boot.admin.model.Application;

import java.net.MalformedURLException;
import java.net.URL;

import static de.codecentric.boot.admin.model.Application.ApplicationBuilder;
import static org.junit.Assert.fail;

/**
 * @author Robert Winkler
 */
public class TestData {

    public static Application createApplication(String name) {
        try {
            return new ApplicationBuilder(name, new URL("http://localhost:8080")).build();
        } catch (MalformedURLException e) {
            fail("URL 'http://localhost:8080' is malformed");
        }
        return null;
    }
}
