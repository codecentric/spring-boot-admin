---
sidebar_custom_props:
  icon: 'ui'
---
# Look and Feel

You can set custom information in the header (i.e. displaying staging information or company name) by using following configuration properties:

* **spring.boot.admin.ui.brand**: This HTML snippet is rendered in navigation header and defaults to `<img src="assets/img/icon-spring-boot-admin.svg"><span>Spring Boot Admin</span>`. By default it shows the SBA logo followed by it’s name. If you want to show a custom logo you can set: `spring.boot.admin.ui.brand=<img src="custom/custom-icon.png">`. Either you just add the image to your jar-file in `/META-INF/spring-boot-admin-server-ui/` (SBA registers a `ResourceHandler` for this location by default), or you must ensure yourself that the image gets served correctly (e.g. by registering your own `ResourceHandler`)
* **spring.boot.admin.ui.title**: Use this option to customize the browsers window title.

## Customizing Colors

You can provide a custom color theme to the application by overwriting the following properties:

```yaml title="application.yml"
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

## Show or hide views

You can very simply hide views in the navbar:

```yaml title="application.yml"
spring:
  boot:
    admin:
      ui:
        view-settings:
          - name: "journal"
            enabled: false
```

## Hide Service URL

To hide service URLs in Spring Boot Admin UI entirely, set the following property in your Server's configuration:

| Property name                            | Default | Usage                                                                                                              
|------------------------------------------|---------|--------------------------------------------------------------------------------------------------------------------
| `spring.boot.admin.ui.hide-instance-url` | `false` | Set to `true` to hide service URLs as well as actions that require them in UI (e.g. jump to /health or /actuator). 

If you want to hide the URL for specific instances only, you can set the `hide-url` property in the instance metadata
while registering a service.
When using Spring Boot Admin Client you can set the property `spring.boot.admin.client.metadata.hide-url=true` in the
corresponding config file. The value set in `metadata` does not have any effect, when the URLs are disabled in Server.
