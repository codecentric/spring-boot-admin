# Microsoft Teams Notifications

To enable Microsoft Teams notifications, you need to set up a connector webhook url and set the appropriate configuration property.

![msteams-notification.png](/4.1.2/assets/images/msteams-notification-a8011263e85860fc57532bbd837863d1.png)

Sample Microsoft Teams notification for a `STATUS_CHANGED` event

## Message format[​](#message-format "Direct link to Message format")

Notifications are sent as [Adaptive Cards](https://adaptivecards.microsoft.com/). The card body is composed of the title, the instance name, the text and a `FactSet` containing the instance's status and URLs.

## SpEL expression context[​](#spel-expression-context "Direct link to SpEL expression context")

The expression-based properties are parsed as SpEL template expressions (`#{ ... }`). The following root variables are available:

| Variable     | Type                                                                                                                                                                                     | Description                                                                                                                                                                                                                                                                    |
| ------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `event`      | [`InstanceEvent`](https://github.com/codecentric/spring-boot-admin/blob/master/spring-boot-admin-server/src/main/java/de/codecentric/boot/admin/server/domain/events/InstanceEvent.java) | The event that triggered the notification. Frequently used property: `type` (one of `REGISTERED`, `DEREGISTERED`, `STATUS_CHANGED`, `REGISTRATION_UPDATED`, `INFO_CHANGED`, `ENDPOINTS_DETECTED`). For `STATUS_CHANGED` events `event.statusInfo.status` holds the new status. |
| `instance`   | [`Instance`](https://github.com/codecentric/spring-boot-admin/blob/master/spring-boot-admin-server/src/main/java/de/codecentric/boot/admin/server/domain/entities/Instance.java)         | The full instance aggregate. Frequently used paths: `instance.id` (e.g. "TestAppId"), `instance.registration.name` (e.g. "Test App"), `instance.statusInfo.status` (one of `UP`, `DOWN`, `OFFLINE`, `RESTRICTED`, `OUT_OF_SERVICE`, `UNKNOWN`).                                |
| `lastStatus` | `String`                                                                                                                                                                                 | The previous status code of the instance (e.g. `UP`, `DOWN`, `UNKNOWN`), useful for building messages like `from #{lastStatus} to #{event.statusInfo.status}`.                                                                                                                 |

| Property                                                                                                                                                                                                                       |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `spring.boot.admin.notify.ms-teams.enabled`Enables the notification.- Type: 

  `java.lang.Boolean`

- Default: 

  `true`                                                                                                           |
| `spring.boot.admin.notify.ms-teams.webhook-url`Webhook url for Microsoft Teams Channel Webhook connector (i.e. [...](https://outlook.office.com/webhook/){webhook-id})- Type: 

  `java.lang.String`                             |
| `spring.boot.admin.notify.ms-teams.ignore-changes`List of changes to ignore. Must be in Format OLD:NEW, for any status use \* as wildcard, e.g. \*:UP or OFFLINE:\*- Type: 

  `java.lang.String[]`

- Default: 

  `["UNKNOWN:UP"]` |
| `spring.boot.admin.notify.ms-teams.registered-title`Title of the Teams message when an app registers- Type: 

  `java.lang.String`

- Default: 

  `"Registered"`                                                                    |
| `spring.boot.admin.notify.ms-teams.deregistered-title`Title of the Teams message when an app deregisters- Type: 

  `java.lang.String`

- Default: 

  `"Deregistered"`                                                              |
| `spring.boot.admin.notify.ms-teams.status-changed-title`Title of the Teams message when an app changes status- Type: 

  `java.lang.String`

- Default: 

  `"Status Changed"`                                                       |
| `spring.boot.admin.notify.ms-teams.title-color-expression`Expression for the color of the message title, see [supported colors](https://adaptivecards.microsoft.com/?topic=TextBlock#color)- Type: 

  `java.lang.String`        |
| `spring.boot.admin.notify.ms-teams.registered-text-expression`Expression for the text that will be displayed when an app registers- Type: 

  `java.lang.String`                                                                 |
| `spring.boot.admin.notify.ms-teams.deregistered-text-expression`Expression for the text that will be displayed when an app deregisters.- Type: 

  `java.lang.String`                                                            |
| `spring.boot.admin.notify.ms-teams.status-changed-text-expression`Expression for the text that will be displayed when an app changes status- Type: 

  `java.lang.String`                                                        |
