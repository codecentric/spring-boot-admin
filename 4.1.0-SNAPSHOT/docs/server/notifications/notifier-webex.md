# Webex Notifications

To enable [Webex](https://www.webex.com/) notifications, you need to set the appropriate configuration properties for `auth-token` and `room-id`.

| Property                                                                                                                                                                                                                    |
| --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `spring.boot.admin.notify.webex.url`base url for Webex API (i.e. https\://webexapis.com/v1/messages)- Type: 

  `java.net.URI`

- Default: 

  `"https://webexapis.com/v1/messages"`                                              |
| `spring.boot.admin.notify.webex.enabled`Enables the notification.- Type: 

  `java.lang.Boolean`

- Default: 

  `true`                                                                                                           |
| `spring.boot.admin.notify.webex.message`- Type: 

  `java.lang.String`                                                                                                                                                        |
| `spring.boot.admin.notify.webex.room-id`Room identifier in Webex where the message will be sent- Type: 

  `java.lang.String`                                                                                                 |
| `spring.boot.admin.notify.webex.auth-token`Bearer authentication token for Webex API- Type: 

  `java.lang.String`                                                                                                            |
| `spring.boot.admin.notify.webex.ignore-changes`List of changes to ignore. Must be in Format OLD:NEW, for any status use \* as wildcard, e.g. \*:UP or OFFLINE:\*- Type: 

  `java.lang.String[]`

- Default: 

  `["UNKNOWN:UP"]` |
