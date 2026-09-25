# Let’s Chat Notifications

To enable [Let’s Chat](https://sdelements.github.io/lets-chat/) notifications you need to add the host url and add the API token and username from Let’s Chat

| Property                                                                                                                                                                                                                       |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `spring.boot.admin.notify.letschat.url`Host URL for Let´s Chat- Type: 

  `java.net.URI`                                                                                                                                         |
| `spring.boot.admin.notify.letschat.room`Name of the room- Type: 

  `java.lang.String`                                                                                                                                           |
| `spring.boot.admin.notify.letschat.token`Token for the Let´s chat API- Type: 

  `java.lang.String`                                                                                                                              |
| `spring.boot.admin.notify.letschat.enabled`Enables the notification.- Type: 

  `java.lang.Boolean`

- Default: 

  `true`                                                                                                           |
| `spring.boot.admin.notify.letschat.message`- Type: 

  `java.lang.String`                                                                                                                                                        |
| `spring.boot.admin.notify.letschat.username`username which sends notification- Type: 

  `java.lang.String`

- Default: 

  `"Spring Boot Admin"`                                                                                    |
| `spring.boot.admin.notify.letschat.ignore-changes`List of changes to ignore. Must be in Format OLD:NEW, for any status use \* as wildcard, e.g. \*:UP or OFFLINE:\*- Type: 

  `java.lang.String[]`

- Default: 

  `["UNKNOWN:UP"]` |
