package spring.boot.admin.javamelody.configuration;

import net.bull.javamelody.CollectorServlet;
import spring.boot.admin.javamelody.listener.JavaMelodyListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Always run, if you depend on this module, you will get this autoconfiguration
@Configuration
@ConditionalOnProperty(prefix = "javamelody", name = "collectserver.enabled", matchIfMissing = true)
public class JavamelodyAutoConfiguration {

    private static final String CONTEXT_ROOT = "/javamelody";

    @Bean
    public JavaMelodyListener javaMelodyListener() {
        return new JavaMelodyListener();
    }

    @Bean
    public ServletRegistrationBean collectorServletBean() {
        final CollectorServlet servlet = new CollectorServlet();
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(servlet);
        servletRegistrationBean.addUrlMappings(CONTEXT_ROOT);

        return servletRegistrationBean;
    }
}
