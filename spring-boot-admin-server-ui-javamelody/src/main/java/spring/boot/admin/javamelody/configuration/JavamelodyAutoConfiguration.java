package spring.boot.admin.javamelody.configuration;

import net.bull.javamelody.CollectorServlet;
import net.bull.javamelody.JavaMelodyService;
import net.bull.javamelody.MonitoringFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

// Always run, if you depend on this module, you will get this autoconfiguration
@Configuration
@ConditionalOnProperty(value = "javamelody.collectserver.enabled", matchIfMissing = true)
public class JavamelodyAutoConfiguration {

    private static final String CONTEXT_ROOT = "/javamelody";

    @Bean
    public JavaMelodyService javaMelodyService() {
        return new JavaMelodyService();
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        final Filter javaMelodyFilter = new MonitoringFilter();

        FilterRegistrationBean javaMelodyFilterBean = new FilterRegistrationBean(javaMelodyFilter);
        javaMelodyFilterBean.addServletNames("monitoring");
        javaMelodyFilterBean.addUrlPatterns("/*");
        return javaMelodyFilterBean;
    }

    @Bean
    public ServletRegistrationBean collectorServletBean() {
        final CollectorServlet servlet = new CollectorServlet();
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(servlet);
        servletRegistrationBean.addUrlMappings(CONTEXT_ROOT);

        return servletRegistrationBean;
    }
}
