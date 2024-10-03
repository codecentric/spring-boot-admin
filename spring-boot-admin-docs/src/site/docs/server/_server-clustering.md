## Clustering

Spring Boot Admin Server supports cluster replication via Hazelcast. It is automatically enabled when a `HazelcastConfig`\- or `HazelcastInstance`\-Bean is present. You can also configure the Hazelcast instance to be persistent, to keep the status over restarts. Also have a look at the [Spring Boot support for Hazelcast](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#boot-features-hazelcast/).

1. Add Hazelcast to your dependencies:  
```xml title="pom.xml"
<dependency>  
    <groupId>com.hazelcast</groupId>  
    <artifactId>hazelcast</artifactId>  
</dependency>  
```
2. Instantiate a HazelcastConfig:  
```java title="HazelcastConfig.java"
@Bean  
public Config hazelcastConfig() {  
    // This map is used to store the events.  
    // It should be configured to reliably hold all the data,  
    // Spring Boot Admin will compact the events, if there are too many  
    MapConfig eventStoreMap = new MapConfig(DEFAULT_NAME_EVENT_STORE_MAP).setInMemoryFormat(InMemoryFormat.OBJECT)  
        .setBackupCount(1)  
        .setMergePolicyConfig(new MergePolicyConfig(PutIfAbsentMergePolicy.class.getName(), 100));  
    // This map is used to deduplicate the notifications.  
    // If data in this map gets lost it should not be a big issue as it will atmost  
    // lead to  
    // the same notification to be sent by multiple instances  
    MapConfig sentNotificationsMap = new MapConfig(DEFAULT_NAME_SENT_NOTIFICATIONS_MAP)  
        .setInMemoryFormat(InMemoryFormat.OBJECT)  
        .setBackupCount(1)  
        .setEvictionConfig(  
                new EvictionConfig().setEvictionPolicy(EvictionPolicy.LRU).setMaxSizePolicy(MaxSizePolicy.PER_NODE))  
        .setMergePolicyConfig(new MergePolicyConfig(PutIfAbsentMergePolicy.class.getName(), 100));  
    Config config = new Config();  
    config.addMapConfig(eventStoreMap);  
    config.addMapConfig(sentNotificationsMap);  
    config.setProperty("hazelcast.jmx", "true");  
    // WARNING: This setups a local cluster, you change it to fit your needs.  
    config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);  
    TcpIpConfig tcpIpConfig = config.getNetworkConfig().getJoin().getTcpIpConfig();  
    tcpIpConfig.setEnabled(true);  
    tcpIpConfig.setMembers(singletonList("127.0.0.1"));  
    return config;  
}  
```

__Hazelcast configuration options__
| Property name                                  | Description                                                      | Default value                          |
| ---------------------------------------------- | ---------------------------------------------------------------- | -------------------------------------- |
| spring.boot.admin.hazelcast.enabled            | Enables the Hazelcast support                                    | true                                   |
| spring.boot.admin.hazelcast.event-store        | Name of the Hazelcast-map to store the events                    | "spring-boot-admin-event-store"        |
| spring.boot.admin.hazelcast.sent-notifications | Name of the Hazelcast-map used to deduplicate the notifications. | "spring-boot-admin-sent-notifications" |
