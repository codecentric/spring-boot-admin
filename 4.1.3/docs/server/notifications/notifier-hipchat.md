# Hipchat Notifications

To enable [Hipchat](https://www.hipchat.com/) notifications you need to create an API token on your Hipchat account and set the appropriate configuration properties.

| Property                                                                                                                                                                                                                      |
| ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `spring.boot.admin.notify.hipchat.url`Base URL for HipChat API (i.e. https\://ACCOUNT\_NAME.hipchat.com/v2- Type: 

  `java.net.URI`                                                                                            |
| `spring.boot.admin.notify.hipchat.notify`TRUE will cause OS notification, FALSE will only notify to room- Type: 

  `java.lang.Boolean`                                                                                         |
| `spring.boot.admin.notify.hipchat.enabled`Enables the notification.- Type: 

  `java.lang.Boolean`

- Default: 

  `true`                                                                                                           |
| `spring.boot.admin.notify.hipchat.message`- Type: 

  `java.lang.String`                                                                                                                                                        |
| `spring.boot.admin.notify.hipchat.room-id`Id of the room to notify- Type: 

  `java.lang.String`                                                                                                                                |
| `spring.boot.admin.notify.hipchat.auth-token`API token that has access to notify in the room- Type: 

  `java.lang.String`                                                                                                      |
| `spring.boot.admin.notify.hipchat.ignore-changes`List of changes to ignore. Must be in Format OLD:NEW, for any status use \* as wildcard, e.g. \*:UP or OFFLINE:\*- Type: 

  `java.lang.String[]`

- Default: 

  `["UNKNOWN:UP"]` |
