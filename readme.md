<h1 align="center">SearchEngine</h1>
<img src="./readme_assets/main_page.png" width="1918">

## Description

SearchEngine implemented by scratch on Spring Boot, PostgreSQL, Liquibase, Docker and Docker-compose. The main features of the search engine are indexing sites, individual pages and searching through them.

## Project setup

In the `settings.xml` file located in `/home/<your username>/.m2` inside tag `settings` add
```xml
<servers>
    <server>
        <id>github</id>
        <configuration>
            <httpHeaders>
                <property>
                    <name>Authorization</name>
                    <value>Token</value>
                </property>
            </httpHeaders>
        </configuration>
    </server>
</servers>
```
>Actual token, the string to be inserted into the tag `<value>...</value>`
[located in the document at the link](https://docs.google.com/document/d/1QVejAfHndY_6oSVGb3KwpU6C28C4RpStrZLHFxAlH2c/edit?usp=sharing).


If you have no `settings.xml` then create it and add 
```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
 https://maven.apache.org/xsd/settings-1.0.0.xsd">

    <servers>
        <server>
            <id>github</id>
            <configuration>
                <httpHeaders>
                    <property>
                        <name>Authorization</name>
                        <value>Token</value>
                    </property>
                </httpHeaders>
            </configuration>
        </server>
    </servers>

</settings>
```
Clone project from GIT repository and build project using Maven.

```text
mvn clean install
```
Give execute permissions to file `start.sh` and execute it. Then visit `localhost:8080` and start indexing to ensure that it works.

## Documentation
To view the documentation, run the application and go to 
```text
http://localhost:8080/swagger-ui/index.html
```

## Adding sites for indexing

If you want to add sites that should be indexed, you must add in field `sites` of `application.yml` file the address and name of the site in the format

```text
  - url: https://www.playback.ru
    name: Playback
```