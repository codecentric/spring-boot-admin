# Mattermost Notifications

To enable [Mattermost](https://mattermost.com/) notifications you need to add a bot account under integrations on your Mattermost server and configure it appropriately.

| Property                                                                                                                                                                                                                         |
| -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `spring.boot.admin.notify.mattermost.api-url`API url for Mattermost (i.e. https\://example.mattermost.com/api/v4/posts)- Type: 

  `java.net.URI`                                                                                  |
| `spring.boot.admin.notify.mattermost.enabled`Enables the notification.- Type: 

  `java.lang.Boolean`

- Default: 

  `true`                                                                                                           |
| `spring.boot.admin.notify.mattermost.message`- Type: 

  `java.lang.String`                                                                                                                                                        |
| `spring.boot.admin.notify.mattermost.channel-id`Optional channel name without # sign (i.e. h616jh436pysjpopp3259mhwxc)- Type: 

  `java.lang.String`                                                                               |
| `spring.boot.admin.notify.mattermost.ignore-changes`List of changes to ignore. Must be in Format OLD:NEW, for any status use \* as wildcard, e.g. \*:UP or OFFLINE:\*- Type: 

  `java.lang.String[]`

- Default: 

  `["UNKNOWN:UP"]` |
| `spring.boot.admin.notify.mattermost.bot-access-token`Bot access token (i.e. dufc8q78hjgeccwsfhe37pcq1w)- Type: 

  `java.lang.String`                                                                                             |
