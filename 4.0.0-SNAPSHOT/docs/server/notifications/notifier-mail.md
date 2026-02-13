# Mail Notifications

Mail notifications will be delivered as HTML emails rendered using [https://www.thymeleaf.org/\[Thymeleaf](https://www.thymeleaf.org/%5BThymeleaf)] templates. To enable Mail notifications, configure a `JavaMailSender` using `spring-boot-starter-mail` and set a recipient.

![mail-notification.png](/4.0.0-SNAPSHOT/assets/images/mail-notification-a2acdea4c13fd4e1f2b2e336c827f6a8.png)

Sample Mail Notification with default template

info

To prevent disclosure of sensitive information, the default mail template doesn’t show any metadata of the instance. If you want to you show some of the metadata you can use a custom template.

Add spring-boot-starter-mail to your dependencies

```

<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

application.properties

```
spring.mail.host=smtp.example.com
spring.boot.admin.notify.mail.to=admin@example.com
```

| Property                                                                                                                                                                                                                   |
| -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `spring.boot.admin.notify.mail.cc`cc-recipients of the mail- Type: 

  `java.lang.String[]`

- Default: 

  `[]`                                                                                                                 |
| `spring.boot.admin.notify.mail.to`recipients of the mail- Type: 

  `java.lang.String[]`

- Default: 

  `["root@localhost"]`                                                                                                    |
| `spring.boot.admin.notify.mail.from`sender of the change- Type: 

  `java.lang.String`

- Default: 

  `"Spring Boot Admin <noreply@localhost>"`                                                                                 |
| `spring.boot.admin.notify.mail.enabled`Enables the notification.- Type: 

  `java.lang.Boolean`

- Default: 

  `true`                                                                                                           |
| `spring.boot.admin.notify.mail.base-url`Base-URL used for hyperlinks in mail- Type: 

  `java.lang.String`                                                                                                                   |
| `spring.boot.admin.notify.mail.template`Thymeleaf template for mail- Type: 

  `java.lang.String`

- Default: 

  `"META-INF/spring-boot-admin-server/mail/status-changed.html"`                                                 |
| `spring.boot.admin.notify.mail.ignore-changes`List of changes to ignore. Must be in Format OLD:NEW, for any status use \* as wildcard, e.g. \*:UP or OFFLINE:\*- Type: 

  `java.lang.String[]`

- Default: 

  `["UNKNOWN:UP"]` |
| `spring.boot.admin.notify.mail.additional-properties`Additional properties to be set for the template- Type: 

  `java.util.Map<java.lang.String,java.lang.Object>`                                                          |
