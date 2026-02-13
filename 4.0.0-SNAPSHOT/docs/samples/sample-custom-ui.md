# Custom UI Sample

Demonstrates how to create custom UI extensions for Spring Boot Admin using Vue.js components. This sample shows how to add custom views, menu items, and instance-specific endpoints to the Admin UI.

## Overview[​](#overview "Direct link to Overview")

**Location**: `spring-boot-admin-samples/spring-boot-admin-sample-custom-ui/`

**Features**:

* Custom top-level navigation views
* Custom instance endpoint views
* Submenu integration
* Internationalization (i18n)
* Custom icons and handles
* Vue 3 components
* Access to SBA global components
* ApplicationStore integration

## Prerequisites[​](#prerequisites "Direct link to Prerequisites")

* Java 17+, Maven 3.6+
* Node.js and npm (for building UI)

## Project Structure[​](#project-structure "Direct link to Project Structure")

This is a **library module** that gets included by other samples (like servlet sample):

```
<!-- In servlet sample -->
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-sample-custom-ui</artifactId>
</dependency>
```

## Building[​](#building "Direct link to Building")

```
cd spring-boot-admin-samples/spring-boot-admin-sample-custom-ui
mvn clean package
```

**Build Process**:

1. Frontend Maven Plugin installs Node.js
2. Runs `npm ci` to install dependencies
3. Runs `npm run build` to compile Vue components
4. Copies dist files to `META-INF/spring-boot-admin-server-ui/extensions/custom/`
5. Admin Server auto-loads extensions from this location

## Custom View Examples[​](#custom-view-examples "Direct link to Custom View Examples")

### 1. Top-Level View[​](#1-top-level-view "Direct link to 1. Top-Level View")

src/index.js

```
SBA.use({
  install({ viewRegistry, i18n }) {
    viewRegistry.addView({
      name: "custom",              // Unique view name
      path: "/custom",             // URL path
      component: custom,           // Vue component
      group: "custom",             // Group for styling
      handle,                      // Custom navigation handle
      order: 1000,                 // Menu order
    });

    // Add translations
    i18n.mergeLocaleMessage("en", {
      custom: {
        label: "My Extensions",
      },
    });
    i18n.mergeLocaleMessage("de", {
      custom: {
        label: "Meine Erweiterung",
      },
    });
  },
});
```

### 2. Submenu Item[​](#2-submenu-item "Direct link to 2. Submenu Item")

```
SBA.viewRegistry.addView({
  name: "customSub",
  parent: "custom",          // Parent view name
  path: "/customSub",
  component: customSubitem,
  label: "Custom Sub",
  order: 1000,
});
```

### 3. Instance Endpoint View[​](#3-instance-endpoint-view "Direct link to 3. Instance Endpoint View")

```
SBA.viewRegistry.addView({
  name: "instances/custom",
  parent: "instances",       // Under instance views
  path: "custom",
  component: customEndpoint,
  label: "Custom",
  group: "custom",
  order: 1000,
  isEnabled: ({ instance }) => {
    return instance.hasEndpoint("custom");  // Conditional rendering
  },
});
```

### 4. Custom Group Icon[​](#4-custom-group-icon "Direct link to 4. Custom Group Icon")

```
SBA.viewRegistry.setGroupIcon(
  "custom",
  `<svg xmlns='http://www.w3.org/2000/svg' class='h-5 mr-3' viewBox='0 0 576 512'>
    <path d='M512 80c8.8 0 16 7.2 16 16V416c0...'/>
  </svg>`
);
```

## Vue Component Example[​](#vue-component-example "Direct link to Vue Component Example")

src/custom.vue

```
<template>
  <div class="m-4">
    <template v-for="application in applications" :key="application.name">
      <sba-panel :title="application.name">
        This application has the following instances:

        <ul>
          <template v-for="instance in application.instances">
            <li>
              <span class="mx-1" v-text="instance.registration.name"></span>

              <!-- SBA components are registered globally -->
              <sba-status :status="instance.statusInfo.status" class="mx-1" />
              <sba-tag :value="instance.id" class="mx-1" label="id" />
            </li>
          </template>
        </ul>
      </sba-panel>
    </template>
  </div>
</template>

<script>
export default {
  setup() {
    const { applications } = SBA.useApplicationStore();  // Access store
    return {
      applications,
    };
  },
};
</script>
```

**Key Features**:

* Access to `SBA.useApplicationStore()` for application data
* Global SBA components (`sba-panel`, `sba-status`, `sba-tag`)
* Reactive data from Vuex store
* Tailwind CSS classes for styling

## Available SBA Components[​](#available-sba-components "Direct link to Available SBA Components")

Global components you can use without importing:

* `<sba-panel>` - Card/panel container
* `<sba-status>` - Status indicator (UP/DOWN/etc.)
* `<sba-tag>` - Tag display
* `<sba-icon>` - Icon component
* `<sba-button>` - Button component
* `<sba-input>` - Input field
* `<sba-toggle>` - Toggle switch
* `<sba-instance-selector>` - Instance dropdown
* Many more in `spring-boot-admin-server-ui` module

## Available Stores and APIs[​](#available-stores-and-apis "Direct link to Available Stores and APIs")

### ApplicationStore[​](#applicationstore "Direct link to ApplicationStore")

```
const { applications } = SBA.useApplicationStore();

// applications is reactive
// Contains: { name, instances[], buildVersion, status, ... }
```

### Instance API[​](#instance-api "Direct link to Instance API")

```
const instance = await SBA.getInstanceById(instanceId);
const health = await instance.fetchHealth();
const metrics = await instance.fetchMetrics();
const info = await instance.fetchInfo();
```

### Event Bus[​](#event-bus "Direct link to Event Bus")

```
SBA.eventBus.on('event-name', (data) => {
  // Handle event
});

SBA.eventBus.emit('custom-event', { foo: 'bar' });
```

## File Structure[​](#file-structure "Direct link to File Structure")

```
spring-boot-admin-sample-custom-ui/
├── src/
│   ├── index.js              # Main entry point
│   ├── custom.vue            # Top-level view component
│   ├── custom-subitem.vue    # Submenu component
│   ├── custom-endpoint.vue   # Instance endpoint component
│   ├── handle.vue            # Navigation handle component
│   └── custom.css            # Custom styles
├── package.json
├── vite.config.js
└── pom.xml
```

## Build Configuration[​](#build-configuration "Direct link to Build Configuration")

### package.json[​](#packagejson "Direct link to package.json")

```
{
  "scripts": {
    "build": "vite build"
  },
  "dependencies": {
    "vue": "^3.x"
  }
}
```

### vite.config.js[​](#viteconfigjs "Direct link to vite.config.js")

```
export default {
  build: {
    lib: {
      entry: 'src/index.js',
      formats: ['es'],
      fileName: 'index'
    },
    rollupOptions: {
      external: ['vue'],  // Vue provided by SBA
      output: {
        globals: {
          vue: 'Vue'
        }
      }
    }
  }
}
```

### pom.xml (Maven Integration)[​](#pomxml-maven-integration "Direct link to pom.xml (Maven Integration)")

```
<plugin>
    <groupId>com.github.eirslett</groupId>
    <artifactId>frontend-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>install-node-and-npm</id>
            <goals>
                <goal>install-node-and-npm</goal>
            </goals>
        </execution>
        <execution>
            <id>npm-build</id>
            <goals>
                <goal>npm</goal>
            </goals>
            <configuration>
                <arguments>run build</arguments>
            </configuration>
        </execution>
    </executions>
</plugin>

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-resources-plugin</artifactId>
    <executions>
        <execution>
            <id>copy-resources</id>
            <phase>process-resources</phase>
            <goals>
                <goal>copy-resources</goal>
            </goals>
            <configuration>
                <outputDirectory>
                    ${project.build.outputDirectory}/META-INF/spring-boot-admin-server-ui/extensions/custom
                </outputDirectory>
                <resources>
                    <resource>
                        <directory>${project.build.directory}/dist</directory>
                    </resource>
                </resources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Development Workflow[​](#development-workflow "Direct link to Development Workflow")

### 1. Make Changes[​](#1-make-changes "Direct link to 1. Make Changes")

Edit Vue components in `src/`:

```
<template>
  <div>My custom view</div>
</template>
```

### 2. Build[​](#2-build "Direct link to 2. Build")

```
mvn clean package
```

### 3. Use in Application[​](#3-use-in-application "Direct link to 3. Use in Application")

```
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-sample-custom-ui</artifactId>
</dependency>
```

### 4. Run & Test[​](#4-run--test "Direct link to 4. Run & Test")

```
mvn spring-boot:run
```

Navigate to your custom view in the UI.

## Common Use Cases[​](#common-use-cases "Direct link to Common Use Cases")

### Custom Dashboard[​](#custom-dashboard "Direct link to Custom Dashboard")

```
viewRegistry.addView({
  name: "dashboard",
  path: "/dashboard",
  component: CustomDashboard,
  label: "Dashboard",
  order: 1,  // First in menu
});
```

### Instance Action Button[​](#instance-action-button "Direct link to Instance Action Button")

```
<template>
  <sba-button @click="restartInstance">
    Restart
  </sba-button>
</template>

<script>
export default {
  props: ['instance'],
  methods: {
    async restartInstance() {
      await this.instance.restart();
    }
  }
}
</script>
```

### Custom Metrics View[​](#custom-metrics-view "Direct link to Custom Metrics View")

```
<template>
  <div>
    <h2>CPU Usage: {{ cpuUsage }}%</h2>
  </div>
</template>

<script>
export default {
  props: ['instance'],
  data() {
    return { cpuUsage: 0 };
  },
  async mounted() {
    const metrics = await this.instance.fetchMetrics();
    this.cpuUsage = metrics['process.cpu.usage'] * 100;
  }
}
</script>
```

## Key Takeaways[​](#key-takeaways "Direct link to Key Takeaways")

✅ **Full Customization**: Add any Vue component to UI ✅ **SBA Integration**: Access stores, components, APIs ✅ **Maven Integration**: Build with Maven ✅ **Reusable**: Package as library for multiple projects

## Next Steps[​](#next-steps "Direct link to Next Steps")

* [UI Customization Guide](/4.0.0-SNAPSHOT/docs/06-customization/ui/)
* [Servlet Sample](/4.0.0-SNAPSHOT/docs/samples/sample-servlet.md) (uses this extension)

## See Also[​](#see-also "Direct link to See Also")

* [Vue.js Documentation](https://vuejs.org/)
