# RocketChat Notifications

To enable [Rocket.Chat](https://www.rocket.chat/) notifications you need a personal token access and create a room to send message with this token

| Property                                                                                                                                                                                                                         |
| -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `spring.boot.admin.notify.rocketchat.url`Host URL for RocketChat server- Type: 

  `java.lang.String`                                                                                                                              |
| `spring.boot.admin.notify.rocketchat.token`Token for RocketChat API- Type: 

  `java.lang.String`                                                                                                                                  |
| `spring.boot.admin.notify.rocketchat.enabled`Enables the notification.- Type: 

  `java.lang.Boolean`

- Default: 

  `true`                                                                                                           |
| `spring.boot.admin.notify.rocketchat.message`- Type: 

  `java.lang.String`                                                                                                                                                        |
| `spring.boot.admin.notify.rocketchat.room-id`Room Id to send message- Type: 

  `java.lang.String`                                                                                                                                 |
| `spring.boot.admin.notify.rocketchat.user-id`User Id for RocketChat API- Type: 

  `java.lang.String`                                                                                                                              |
| `spring.boot.admin.notify.rocketchat.ignore-changes`List of changes to ignore. Must be in Format OLD:NEW, for any status use \* as wildcard, e.g. \*:UP or OFFLINE:\*- Type: 

  `java.lang.String[]`

- Default: 

  `["UNKNOWN:UP"]` |
