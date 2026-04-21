# Microsoft Teams Notifications

To enable Microsoft Teams notifications you need to set up a connector webhook url and set the appropriate configuration property.

| Property                                                                                                                                                                                                                       |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `spring.boot.admin.notify.ms-teams.enabled`Enables the notification.- Type: 

  `java.lang.Boolean`

- Default: 

  `true`                                                                                                           |
| `spring.boot.admin.notify.ms-teams.theme-color`Theme Color is the color of the accent on the message that appears in Microsoft Teams. Default is Spring Green- Type: 

  `java.lang.String`                                      |
| `spring.boot.admin.notify.ms-teams.webhook-url`Webhook url for Microsoft Teams Channel Webhook connector (i.e. [...](https://outlook.office.com/webhook/){webhook-id})- Type: 

  `java.net.URI`                                 |
| `spring.boot.admin.notify.ms-teams.ignore-changes`List of changes to ignore. Must be in Format OLD:NEW, for any status use \* as wildcard, e.g. \*:UP or OFFLINE:\*- Type: 

  `java.lang.String[]`

- Default: 

  `["UNKNOWN:UP"]` |
| `spring.boot.admin.notify.ms-teams.message-summary`Summary section of every Teams message originating from Spring Boot Admin- Type: 

  `java.lang.String`

- Default: 

  `"Spring Boot Admin Notification"`                        |
| `spring.boot.admin.notify.ms-teams.registered-title`Title of the Teams message when an app registers- Type: 

  `java.lang.String`

- Default: 

  `"Registered"`                                                                    |
| `spring.boot.admin.notify.ms-teams.de-registered-title`Title of the Teams message when an app de-registers- Type: 

  `java.lang.String`

- Default: 

  `"De-Registered"`                                                           |
| `spring.boot.admin.notify.ms-teams.status-changed-title`Title of the Teams message when an app changes status- Type: 

  `java.lang.String`

- Default: 

  `"Status Changed"`                                                       |
| `spring.boot.admin.notify.ms-teams.status-activity-subtitle`Message will be used as title of the Activity section of the Teams message when an app changes status- Type: 

  `java.lang.String`                                  |
| `spring.boot.admin.notify.ms-teams.register-activity-subtitle`Message will be used as title of the Activity section of the Teams message when an app registers- Type: 

  `java.lang.String`                                     |
| `spring.boot.admin.notify.ms-teams.deregister-activity-subtitle`Message will be used as title of the Activity section of the Teams message when an app de-registers.- Type: 

  `java.lang.String`                               |
