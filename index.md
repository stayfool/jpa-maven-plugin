# JPA-MAVEN-PLUGIN
stayfool <jych1224@163.com>
v1.0.0 2017-8-19

this plugin is use to generate jpa entity and spring-data-jpa repository from database.

## USAGE
```xml
<plugin>
    <groupId>org.stayfool</groupId>
    <artifactId>jpa-maven-plugin</artifactId>
    <version>1.0.0</version>
    <configuration>
        <database>
            <driverClass>com.mysql.jdbc.Driver</driverClass>
            <url>jdbc:mysql://localhost:3306/medicine?useSSL=false</url>
            <username>root</username>
            <password>root</password>
        </database>
        <generate>
            <!--base output directory-->
            <baseDir>src/test/java</baseDir>
            <!--base package-->
            <basePkg>org.stayfool</basePkg>
            <!--entity configuration-->
            <entity>
                <!--default entity-->
                <pkg>entity</pkg>
                <!--default false-->
                <!--if this is set up to true, the accessType will be set up to field automatically-->
                <useLombok>true</useLombok>
                <!--default property-->
                <accessType>field</accessType>
                <!--default entity.vm-->
                <!-- if you want to use your own template, set here-->
                <template>entity.vm</template>
            </entity>

            <!--spring repository configuration-->
            <!--this is design for spring-data-jpa-->
            <!--if you don't want generate repository, remove this label-->
            <!--if you want use default settings, use <springRepo></springRepo>-->
            <springRepo>
                <!--default repo-->
                <pkg>repo</pkg>
                repository.vm
                <!-- if you want to use your own template, set here-->
                <template>repo.vm</template>
                <!--repository super interface, must be full name-->
                <!--these can be used :-->
                <!--org.springframework.data.jpa.repository.JpaRepository-->
                <!--org.springframework.data.repository.CrudRepository-->
                <!--org.springframework.data.repository.PagingAndSortingRepository-->
                <!--org.springframework.data.repository.Repository-->
                <superInterface>org.springframework.data.jpa.repository.JpaRepository</superInterface>
            </springRepo>
        </generate>
    </configuration>
</plugin>
```

## LICENSE
**MIT**
