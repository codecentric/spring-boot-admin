# Slack Notifications

To enable [Slack](https://slack.com/) notifications you need to add an incoming Webhook under custom integrations on your Slack account and configure it appropriately.

| Property                                                                                                                                                                                                                    |
| --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `spring.boot.admin.notify.slack.icon`Optional emoji icon without colons (i.e. my-emoji)- Type: 

  `java.lang.String`                                                                                                         |
| `spring.boot.admin.notify.slack.channel`Optional channel name without # sign (i.e. somechannel)- Type: 

  `java.lang.String`                                                                                                 |
| `spring.boot.admin.notify.slack.enabled`Enables the notification.- Type: 

  `java.lang.Boolean`

- Default: 

  `true`                                                                                                           |
| `spring.boot.admin.notify.slack.message`- Type: 

  `java.lang.String`                                                                                                                                                        |
| `spring.boot.admin.notify.slack.username`Optional username which sends notification- Type: 

  `java.lang.String`

- Default: 

  `"Spring Boot Admin"`                                                                           |
| `spring.boot.admin.notify.slack.webhook-url`Webhook url for Slack API (i.e. https\://hooks.slack.com/services/xxx)- Type: 

  `java.net.URI`                                                                                  |
| `spring.boot.admin.notify.slack.ignore-changes`List of changes to ignore. Must be in Format OLD:NEW, for any status use \* as wildcard, e.g. \*:UP or OFFLINE:\*- Type: 

  `java.lang.String[]`

- Default: 

  `["UNKNOWN:UP"]` |
