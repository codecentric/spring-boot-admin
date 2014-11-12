package de.codecentric.boot.admin.swagger.config;

import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;
import com.wordnik.swagger.model.ApiInfo;
import de.codecentric.boot.admin.swagger.controller.SwaggerController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 *
 * @author Robert Winkler
 *
 */
@Configuration
@EnableSwagger
public class SwaggerConfig {

    @Autowired
    private Environment environment;

    @Autowired
    private SpringSwaggerConfig springSwaggerConfig;

    @Bean
    public SwaggerController swaggerController(){
        return new SwaggerController();
    }

    @Bean
    public SwaggerSpringMvcPlugin adminServerRestApi(){
        String appVersion = environment.getProperty("info.version");
        String swaggerGoup = environment.getProperty("spring.application.name");
        String description = environment.getProperty("info.description");
        Assert.notNull(swaggerGoup, "The name of the application is mandatory. Please customize spring.application.name");
        Assert.notNull(appVersion, "The version of the application is mandatory. Please customize info.version");
        Assert.notNull(description, "The description of the application is mandatory. Please customize info.description");
        return new SwaggerSpringMvcPlugin(this.springSwaggerConfig).includePatterns(".*api.*").apiInfo(adminServerApiInfo(swaggerGoup, description)).apiVersion(appVersion).swaggerGroup(swaggerGoup);
    }

    private ApiInfo adminServerApiInfo(String swaggerGoup, String description) {

        ApiInfo apiInfo = new ApiInfo(
                String.format("%s API", swaggerGoup),
                String.format(description),
                "https://github.com/codecentric/spring-boot-admin",
                "robert.winkler@telekom.de",
                "Apache 2.0 License",
                "http://www.apache.org/licenses/LICENSE-2.0.html"
        );
        return apiInfo;
    }
}