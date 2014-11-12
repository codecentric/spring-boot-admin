package de.codecentric.boot.admin.swagger.controller;

import com.mangofactory.swagger.annotations.ApiIgnore;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author Robert Winkler
 *
 */
@Controller
@ApiIgnore
public class SwaggerController {
    @RequestMapping(value = "/api-ui", method= RequestMethod.GET)
    public String index() {
        return "forward:api-ui/index.html";
    }
}
