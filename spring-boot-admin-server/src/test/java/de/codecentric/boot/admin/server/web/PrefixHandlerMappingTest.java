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
package de.codecentric.boot.admin.server.web;

import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;

import static org.assertj.core.api.Assertions.assertThat;

public class PrefixHandlerMappingTest {

    private final StaticApplicationContext context = new StaticApplicationContext();
    private Method method;

    @Before
    public void init() throws Exception {
        this.method = ReflectionUtils.findMethod(TestController.class, "invoke");
    }

    @Test
    public void withoutPrefix() throws Exception {
        TestController controller = new TestController();
        PrefixHandlerMapping mapping = new PrefixHandlerMapping(controller);
        mapping.setApplicationContext(this.context);
        mapping.afterPropertiesSet();
        assertThat(mapping.getHandler(new MockHttpServletRequest("GET", "/test")).getHandler()).isEqualTo(
                new HandlerMethod(controller, this.method));
        assertThat(mapping.getHandler(new MockHttpServletRequest("GET", "/noop"))).isNull();
    }

    @Test
    public void withPrefix() throws Exception {
        TestController controller = new TestController();
        PrefixHandlerMapping mapping = new PrefixHandlerMapping(controller);
        mapping.setApplicationContext(this.context);
        mapping.setPrefix("/pre");
        mapping.afterPropertiesSet();
        assertThat(mapping.getHandler(new MockHttpServletRequest("GET", "/pre/test")).getHandler()).isEqualTo(
                new HandlerMethod(controller, this.method));
        assertThat(mapping.getHandler(new MockHttpServletRequest("GET", "/pre/noop"))).isNull();
    }

    private static class TestController {
        @RequestMapping("/test")
        public Object invoke() {
            return null;
        }
    }
}
