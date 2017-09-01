package spring.boot.admin.javamelody.configuration;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class JavamelodyAutoConfigurationTest {

    private JavamelodyAutoConfiguration javaMelodyConfiguration;

    @Before
    public void setUp() throws Exception {
        javaMelodyConfiguration = new JavamelodyAutoConfiguration();
    }

    @Test
    public void shouldRunAsServer() {
        final ServletRegistrationBean servletRegistrationBean = javaMelodyConfiguration.collectorServletBean();
        assertThat(servletRegistrationBean.getUrlMappings().size(), is(1));
        assertThat(servletRegistrationBean.getUrlMappings().iterator().next(), is("/javamelody"));
    }

    @Test
    public void shouldRunAsClientAsWell() {
        final FilterRegistrationBean filterRegistrationBean = javaMelodyConfiguration.filterRegistrationBean();

        assertTrue(filterRegistrationBean.getInitParameters().isEmpty());

        assertThat(filterRegistrationBean.getUrlPatterns().size(), is(1));
        assertThat(filterRegistrationBean.getUrlPatterns().iterator().next(), is("/*"));

        assertThat(filterRegistrationBean.getServletNames().size(), is(1));
        assertThat(filterRegistrationBean.getServletNames().iterator().next(), is("monitoring"));
    }

}
