# DingTalk Notifications

To enable [DingTalk](https://www.dingtalk.com/) notifications you need to create and authorize a dingtalk bot and set the appropriate configuration properties for webhookUrl and secret.

| Property                                                                                                                                                                                                                       |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `spring.boot.admin.notify.dingtalk.secret`Secret for DingTalk.- Type: 

  `java.lang.String`                                                                                                                                     |
| `spring.boot.admin.notify.dingtalk.enabled`Enables the notification.- Type: 

  `java.lang.Boolean`

- Default: 

  `true`                                                                                                           |
| `spring.boot.admin.notify.dingtalk.message`- Type: 

  `java.lang.String`                                                                                                                                                        |
| `spring.boot.admin.notify.dingtalk.webhook-url`Webhook URI for the DingTalk API.- Type: 

  `java.lang.String`                                                                                                                   |
| `spring.boot.admin.notify.dingtalk.ignore-changes`List of changes to ignore. Must be in Format OLD:NEW, for any status use \* as wildcard, e.g. \*:UP or OFFLINE:\*- Type: 

  `java.lang.String[]`

- Default: 

  `["UNKNOWN:UP"]` |
