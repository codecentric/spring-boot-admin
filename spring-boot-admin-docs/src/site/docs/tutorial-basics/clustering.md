Spring Boot Admin Server supports cluster replication via Hazelcast. It is automatically enabled when a `HazelcastConfig`- or `HazelcastInstance`-Bean is present. You can also configure the Hazelcast instance to be persistent, to keep the status over restarts. Also have a look at the [Spring Boot support for Hazelcast](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#boot-features-hazelcast/).

1.  Add Hazelcast to your dependencies:

    <div class="formalpara-title">

    **pom.xml**

    </div>

    ``` xml
    ```

2.  Instantiate a HazelcastConfig:

    ``` java
    ```

|                                                                  |                                          |
|------------------------------------------------------------------|------------------------------------------|
| Property name                                                    | Default value                            |
| `spring.boot.admin.hazelcast.enabled`                            | `true`                                   |
| Enables the Hazelcast support                                    |                                          |
| `spring.boot.admin.hazelcast.event-store`                        | `"spring-boot-admin-event-store"`        |
| Name of the Hazelcast-map to store the events                    |                                          |
| `spring.boot.admin.hazelcast.sent-notifications`                 | `"spring-boot-admin-sent-notifications"` |
| Name of the Hazelcast-map used to deduplicate the notifications. |                                          |

Hazelcast configuration options
