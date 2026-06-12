# Telegram Notifications

To enable [Telegram](https://telegram.org/) notifications you need to create and authorize a telegram bot and set the appropriate configuration properties for auth-token and chat-id.

| Property                                                                                                                                                                                                                       |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `spring.boot.admin.notify.telegram.api-url`base url for telegram (i.e. https\://api.telegram.org)- Type: 

  `java.lang.String`

- Default: 

  `"https://api.telegram.org"`                                                         |
| `spring.boot.admin.notify.telegram.chat-id`Unique identifier for the target chat or username of the target channel- Type: 

  `java.lang.String`                                                                                 |
| `spring.boot.admin.notify.telegram.enabled`Enables the notification.- Type: 

  `java.lang.Boolean`

- Default: 

  `true`                                                                                                           |
| `spring.boot.admin.notify.telegram.message`- Type: 

  `java.lang.String`                                                                                                                                                        |
| `spring.boot.admin.notify.telegram.auth-token`The token identifying und authorizing your Telegram bot (e.g. \`123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11\`)- Type: 

  `java.lang.String`                                         |
| `spring.boot.admin.notify.telegram.parse-mode`Send Markdown or HTML, if you want Telegram apps to show bold, italic, fixed-width text or inline URLs in your bot's message.- Type: 

  `java.lang.String`

- Default: 

  `"HTML"`   |
| `spring.boot.admin.notify.telegram.disable-notify`If true users will receive a notification with no sound.- Type: 

  `java.lang.Boolean`                                                                                        |
| `spring.boot.admin.notify.telegram.ignore-changes`List of changes to ignore. Must be in Format OLD:NEW, for any status use \* as wildcard, e.g. \*:UP or OFFLINE:\*- Type: 

  `java.lang.String[]`

- Default: 

  `["UNKNOWN:UP"]` |
