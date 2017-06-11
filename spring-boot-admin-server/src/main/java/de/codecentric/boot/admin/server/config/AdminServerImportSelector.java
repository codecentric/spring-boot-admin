/*
 * Copyright 2013-2014 the original author or authors.
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
package de.codecentric.boot.admin.server.config;

import java.util.Arrays;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

/**
 * Defers the imports for our {@code @Configuration}-classes, because the need to be processed after
 * normal @Configuration-classes.
 *
 * @author Johannes Edmeier
 */
public class AdminServerImportSelector implements DeferredImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        String[] configs = {NotifierConfiguration.class.getCanonicalName(), HazelcastStoreConfiguration.class.getCanonicalName(), AdminServerCoreConfiguration.class.getCanonicalName(), AdminServerWebConfiguration.class.getCanonicalName()};

        if (ClassUtils.isPresent("de.codecentric.boot.admin.ui.config.AdminServerWebUiConfiguration",
                this.getClass().getClassLoader())) {
            configs = Arrays.copyOf(configs, configs.length + 1);
            configs[configs.length - 1] = "de.codecentric.boot.admin.ui.config.AdminServerWebUiConfiguration";
        }
        return configs;
    }

}
