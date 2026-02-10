---
sidebar_custom_props:
  icon: 'package'
---

# SNAPSHOT-Versions

If you want to use a snapshot version of Spring Boot Admin Server you most likely need to include the spring and
sonatype snapshot repositories:

```xml title="pom.xml"
<repositories>
    <!-- Repo at GitHub to retrive snapshots of spring-boot-admin -->
    <repository>
        <id>spring-boot-admin-snapshot</id>
        <name>Spring Boot Admin Snapshots</name>
        <url>https://maven.pkg.github.com/codecentric/spring-boot-admin</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
        <releases>
            <enabled>false</enabled>
        </releases>
    </repository>
    
    <!-- Repo for spring milestones, RCs, snapshots -->
    <repository>
        <id>spring-milestone</id>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
        <url>https://repo.spring.io/milestone</url>
    </repository>
    <repository>
        <id>spring-snapshot</id>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
        <url>http:s//repo.spring.io/snapshot</url>
    </repository>
</repositories>
```
