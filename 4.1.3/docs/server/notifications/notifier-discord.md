# Discord Notifications

To enable Discord notifications you need to create a webhook and set the appropriate configuration property.

| Property                                                                                                                                                                                                                      |
| ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `spring.boot.admin.notify.discord.tts`If the message is a text to speech message. False by default.- Type: 

  `java.lang.Boolean`                                                                                              |
| `spring.boot.admin.notify.discord.enabled`Enables the notification.- Type: 

  `java.lang.Boolean`

- Default: 

  `true`                                                                                                           |
| `spring.boot.admin.notify.discord.message`- Type: 

  `java.lang.String`                                                                                                                                                        |
| `spring.boot.admin.notify.discord.username`Optional username. Default is set in Discord.- Type: 

  `java.lang.String`                                                                                                          |
| `spring.boot.admin.notify.discord.avatar-url`Optional URL to avatar.- Type: 

  `java.lang.String`                                                                                                                              |
| `spring.boot.admin.notify.discord.webhook-url`Webhook URI for the Discord API (i.e. https\://discordapp.com/api/webhooks/{webhook.id}/{webhook.token})- Type: 

  `java.net.URI`                                                |
| `spring.boot.admin.notify.discord.ignore-changes`List of changes to ignore. Must be in Format OLD:NEW, for any status use \* as wildcard, e.g. \*:UP or OFFLINE:\*- Type: 

  `java.lang.String[]`

- Default: 

  `["UNKNOWN:UP"]` |
