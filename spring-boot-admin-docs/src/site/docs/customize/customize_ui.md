# UI

## Linking / Embedding External Pages in Navbar

Links will be opened in a new window/tab (i.e. `target="_blank"`) having no access to opener and referrer (`rel="noopener noreferrer"`).

### Simple link

To add a simple link to an external page, use the following snippet.

```yaml
spring:
  boot:
    admin:
      ui:
        external-views:
          - label: "🚀" #(1)
            url: "https://codecentric.de" #(2)
            order: 2000 #(3)
```

1. The label will be shown in the navbar
2. URL to the page you want to link to
3. Order that allows to specify position of item in navbar

### Dropdown with links

To aggregate links below a single element, dropdowns can be configured as follows.

```yaml
spring:
  boot:
    admin:
      ui:
        external-views:
          - label: Link w/o children
            children:
              - label: "📖 Docs"
                url: https://codecentric.github.io/spring-boot-admin/current/
              - label: "📦 Maven"
                url: https://search.maven.org/search?q=g:de.codecentric%20AND%20a:spring-boot-admin-starter-server
              - label: "🐙 GitHub"
                url: https://github.com/codecentric/spring-boot-admin
```

### Dropdown as link having links as children

```yaml
spring:
  boot:
    admin:
      ui:
        external-views:
          - label: Link w children
            url: https://codecentric.de  #(1)
            children:
              - label: "📖 Docs"
                url: https://codecentric.github.io/spring-boot-admin/current/
              - label: "📦 Maven"
                url: https://search.maven.org/search?q=g:de.codecentric%20AND%20a:spring-boot-admin-starter-server
              - label: "🐙 GitHub"
                url: https://github.com/codecentric/spring-boot-admin
          - label: "🎅 Is it christmas"
            url: https://isitchristmas.com
            iframe: true
```

## Custom Views

It is possible to add custom views to the ui. The views must be implemented as [Vue.js](https://vuejs.org/) components.

The JavaScript-Bundle and CSS-Stylesheet must be placed on the classpath at `/META-INF/spring-boot-admin-server-ui/extensions/{name}/` so the server can pick them up. The [spring-boot-admin-sample-custom-ui](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-samples/spring-boot-admin-sample-custom-ui/) module contains a sample which has the necessary maven setup to build such a module.

The custom extension registers itself by calling:

```javascript
SBA.use({
  install({ viewRegistry, i18n }) {
    viewRegistry.addView({
      name: "custom", //(1)
      path: "/custom", //(2)
      component: custom, //(3)
      group: "custom", //(4)
      handle, //(5)
      order: 1000, //(6)
    });
    i18n.mergeLocaleMessage("en", {
      custom: {
        label: "My Extensions", //(7)
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

1. Name of the view and the route.
2. Path in Vue router.
3. The imported custom component, which will be rendered on the route.
4. An optional group name that allows to bind views to a logical group (defaults to "none")
5. The handle for the custom view to be shown in the top navigation bar.
6. Order for the view.
7. Using `i18n.mergeLocaleMessage` allows to add custom translations.

Views in the top navigation bar are sorted by ascending order.

If new top level routes are added to the frontend, they also must be known to the backend. Add a `/META-INF/spring-boot-admin-server-ui/extensions/{name}/routes.txt` with all your new toplevel routes (one route per line).

Groups are used in instance sidebar to aggregate multiple views into a collapsible menu entry showing the group’s name. When a group contains just a single element, the label of the view is shown instead of the group’s name.

#### Override/Set custom group icons

In order to override or set icons for (custom) groups you can use the `SBA.viewRegistry.setGroupIcon` function as follows:

```javascript
SBA.viewRegistry.setGroupIcon(
  "custom", //(1)
  `<svg xmlns='http://www.w3.org/2000/svg'
        class='h-5  mr-3'
        viewBox='0 0 576 512'><path d='M512 80c8.8 0 16 7.2 16 16V416c0 8.8-7.2 16-16 16H64c-8.8 0-16-7.2-16-16V96c0-8.8 7.2-16 16-16H512zM64 32C28.7 32 0 60.7 0 96V416c0 35.3 28.7 64 64 64H512c35.3 0 64-28.7 64-64V96c0-35.3-28.7-64-64-64H64zM200 208c14.2 0 27 6.1 35.8 16c8.8 9.9 24 10.7 33.9 1.9s10.7-24 1.9-33.9c-17.5-19.6-43.1-32-71.5-32c-53 0-96 43-96 96s43 96 96 96c28.4 0 54-12.4 71.5-32c8.8-9.9 8-25-1.9-33.9s-25-8-33.9 1.9c-8.8 9.9-21.6 16-35.8 16c-26.5 0-48-21.5-48-48s21.5-48 48-48zm144 48c0-26.5 21.5-48 48-48c14.2 0 27 6.1 35.8 16c8.8 9.9 24 10.7 33.9 1.9s10.7-24 1.9-33.9c-17.5-19.6-43.1-32-71.5-32c-53 0-96 43-96 96s43 96 96 96c28.4 0 54-12.4 71.5-32c8.8-9.9 8-25-1.9-33.9s-25-8-33.9 1.9c-8.8 9.9-21.6 16-35.8 16c-26.5 0-48-21.5-48-48z'/>
  </svg>` //(2)
);
```

1. Name of the group to set icon for
2. Arbitrary HTML code (e.g. SVG image) that is inserted and parsed as icon.

#### Adding a Top-Level View

Here is a simple top level view just listing all registered applications:

```html
<template>
  <div class="m-4">
    <template v-for="application in applications" :key="application.name">
      <sba-panel :title="application.name">
        This application has the following instances:

        <ul>
          <template v-for="instance in application.instances">
            <li>
              <span class="mx-1" v-text="instance.registration.name"></span>

              <!-- SBA components are registered globally and can be used without importing them! -->
              <!-- They are defined in spring-boot-admin-server-ui -->
              <sba-status :status="instance.statusInfo.status" class="mx-1" />
              <sba-tag :value="instance.id" class="mx-1" label="id" />
            </li>
          </template>
        </ul>
      </sba-panel>
    </template>
    <pre v-text="applications"></pre>
  </div>
</template>

<script>
/* global SBA */
import "./custom.css";

export default {
  setup() {
    const { applications } = SBA.useApplicationStore(); //(1)
    return {
      applications,
    };
  },
  methods: {
    stringify: JSON.stringify,
  },
};
</script>
```

1. By destructuring `applications` of `SBA.useApplicationStore()`, you have reactive access to registered applications.

:::tip
There are some helpful methods on the application and instances object available. Have a look at [application.ts](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-server-ui/src/main/frontend/services/application.ts) and [instance.ts](https://github.com/codecentric/spring-boot-admin/tree/master/spring-boot-admin-server-ui/src/main/frontend/services/instance.ts)
:::

And this is how you register the top-level view.

##### Example

```javascript
SBA.viewRegistry.addView({
  name: "customSub",
  parent: "custom", // (1)
  path: "/customSub", // (2)
  component: customSubitem,
  label: "Custom Sub",
  order: 1000,
});
```

1. References the name of the parent view.
2. Router path used to navigate to.
3. Define whether the path should be registered as child route in parent’s route. When set to `true`, the parent component has to implement `<router-view>`

The `routes.txt` config with the added route:

```text
/custom/**
/customSub/**
```

### Visualizing a Custom Endpoint

Here is a view to show a custom endpoint:

```html
<template>
  <div class="custom">
    <p>Instance: <span v-text="instance.id" /></p>
    <p>Output: <span v-html="text" /></p>
  </div>
</template>

<script>
export default {
  props: {
    instance: {
      //(1)
      type: Object,
      required: true,
    },
  },
  data: () => ({
    text: "",
  }),
  async created() {
    console.log(this.instance);
    const response = await this.instance.axios.get("actuator/custom"); //(2)
    this.text = response.data;
  },
};
</script>

<style lang="css" scoped>
.custom {
  font-size: 20px;
  width: 80%;
}
</style>
```

1. If you define a `instance` prop the component will receive the instance the view should be rendered for.
2. Each instance has a preconfigured [axios](https://github.com/axios/axios) instance to access the endpoints with the correct path and headers.

Registering the instance view works like for the top-level view with some additional properties:

```javascript
SBA.viewRegistry.addView({
  name: "instances/custom",
  parent: "instances", // (1)
  path: "custom",
  component: customEndpoint,
  label: "Custom",
  group: "custom", // (2)
  order: 1000,
  isEnabled: ({ instance }) => {
    return instance.hasEndpoint("custom");
  }, // (3)
});
```

1. The parent must be 'instances' in order to render the new custom view for a single instance.
2. You can group views by assigning them to a group.
3. If you add a `isEnabled` callback you can figure out dynamically if the view should be show for the particular instance.

:::note
You can override default views by putting the same group and name as the one you want to override.
:::

### Customizing Header Logo and Title

You can set custom information in the header (i.e. displaying staging information or company name) by using following configuration properties:

* **spring.boot.admin.ui.brand**: This HTML snippet is rendered in navigation header and defaults to `<img src="assets/img/icon-spring-boot-admin.svg"><span>Spring Boot Admin</span>`. By default it shows the SBA logo followed by it’s name. If you want to show a custom logo you can set: `spring.boot.admin.ui.brand=<img src="custom/custom-icon.png">`. Either you just add the image to your jar-file in `/META-INF/spring-boot-admin-server-ui/` (SBA registers a `ResourceHandler` for this location by default), or you must ensure yourself that the image gets served correctly (e.g. by registering your own `ResourceHandler`)
* **spring.boot.admin.ui.title**: Use this option to customize the browsers window title.

## Customizing Colors

You can provide a custom color theme to the application by overwriting the following properties:

```yaml
spring:
  boot:
    admin:
      ui:
        theme:
          color: "#4A1420"
          palette:
            50: "#F8EBE4"
            100: "#F2D7CC"
            200: "#E5AC9C"
            300: "#D87B6C"
            400: "#CB463B"
            500: "#9F2A2A"
            600: "#83232A"
            700: "#661B26"
            800: "#4A1420"
            900: "#2E0C16"
```

| Property name                                                               | Default                                                                                                                          | Usage                                                                                                                                             |
| --------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------- |
| spring.boot.admin.ui.theme.color                                            | #42d3a5                                                                                                                          | Used in meta tag for setting theme color for user agents. Used to customize the display of the page or of the surrounding user interface.         |
| spring.boot.admin.ui.theme.background-enabled                               | true                                                                                                                             | Disable background image in UI                                                                                                                    |
| spring.boot.admin.ui.theme.palette.[50,100,200,300,400,500,600,700,800,900] | 50: #E8FBEF 100: #D0F7DF 200: #A1EFBD 300: #71E69C 400: #41DE7B 500: #22C55E 600: #1A9547 700: #116530 800: #09351A 900: #010603 | Define a color palette that affects the colors in sidebar view (e.g shade 600 of palette is used as text color and shade 50 as background color.) |

## Customizing Login Logo

You can set a custom image to be displayed on the login page.

1. Put the image in a resource location which is served via http (e.g. `/META-INF/spring-boot-admin-server-ui/assets/img/`).
2. Configure the icons to use using the following property:  
   * **spring.boot.admin.ui.login-icon**: Used as icon on login page. (e.g `assets/img/custom-login-icon.svg`)

## Customizing Favicon

It is possible to use a custom favicon, which is also used for desktop notifications. Spring Boot Admin uses a different icon when one or more application is down.

1. Put the favicon (`.png` with at least 192x192 pixels) in a resource location which is served via http (e.g. `/META-INF/spring-boot-admin-server-ui/assets/img/`).
2. Configure the icons to use using the following properties:  
   * `spring.boot.admin.ui.favicon`: Used as default icon. (e.g `assets/img/custom-favicon.png`  
   * `spring.boot.admin.ui.favicon-danger`: Used when one or more service is down. (e.g `assets/img/custom-favicon-danger.png`)

## Customizing Available Languages

To filter languages to a subset of all supported languages:

* **spring.boot.admin.ui.available-languages**: Used as a filter of existing languages. (e.g `en,de` out of existing `de,en,fr,ko,pt-BR,ru,zh`)

## Show / Hide views

You can very simply hide views in the navbar:

```yaml
spring:
  boot:
    admin:
      ui:
        view-settings:
          - name: "journal"
            enabled: false
```
