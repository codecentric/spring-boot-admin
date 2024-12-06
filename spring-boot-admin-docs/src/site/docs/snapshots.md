# Use SNAPSHOT-Versions

If you want to use a snapshot version of Spring Boot Admin Server you most likely need to include the spring and sonatype snapshot repositories:

```xml title="pom.xml"
<repositories>
    <repository>
        <id>spring-milestone</id>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
        <url>http://repo.spring.io/milestone</url>
    </repository>
    <repository>
        <id>spring-snapshot</id>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
        <url>http://repo.spring.io/snapshot</url>
    </repository>
    <repository>
        <id>sonatype-nexus-snapshots</id>
        <name>Sonatype Nexus Snapshots</name>
        <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
        <releases>
            <enabled>false</enabled>
        </releases>
    </repository>
</repositories>
```
