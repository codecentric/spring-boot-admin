package de.codecentric.boot.admin.security.config;

/**
 * @author Robert Winkler
 */

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for the security aspects of an application.
 */
@ConfigurationProperties(prefix = "spring.boot.admin.security")
public class AdminServerSecurityProperties {

    /**
     * Order applied to the WebSecurityConfigurerAdapter that is used to configure
     * authentication for REST application endpoints.
     */
    public static final int REST_API_AUTH_ORDER = 100;

    /**
     * Order applied to the WebSecurityConfigurerAdapter that is used to configure
     * authentication for Spring Boot Actuator endpoints.
     */
    public static final int MANAGEMENT_API_AUTH_ORDER = 101;


    /**
     * Order applied to the WebSecurityConfigurerAdapter that is used to configure
     * authentication for Admin Web UI.
     */
    public static final int WEB_UI_AUTH_ORDER = 102;

    private final Basic basic = new Basic();

    public static final String ROLE_CLIENT = "ROLE_CLIENT";

    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    private String[] restApiPath = new String[] { "/api/**"};

    private String[] restApiUipath = new String[] {"/api-ui", "/api-ui/**"};

    private String[] adminWebUipath = new String[] {"/admin-ui", "/admin-ui/**"};

    public Basic getBasic() {
        return basic;
    }

    public String[] getRestApiPath() {
        return restApiPath;
    }

    public void setRestApiPath(String[] restApiPath) {
        this.restApiPath = restApiPath;
    }

    public String[] getRestApiUipath() {
        return restApiUipath;
    }

    public void setRestApiUipath(String[] restApiUipath) {
        this.restApiUipath = restApiUipath;
    }

    public String[] getAdminWebUipath() {
        return adminWebUipath;
    }

    public void setAdminWebUipath(String[] adminWebUipath) {
        this.adminWebUipath = adminWebUipath;
    }

    public static class Basic {
        private String realm = "spring-boot-admin";

        public String getRealm() {
            return this.realm;
        }

        public void setRealm(String realm) {
            this.realm = realm;
        }
    }
}
