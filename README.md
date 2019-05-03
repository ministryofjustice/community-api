# Delius API
New Tech Delius: Offender API.

The service provides REST access to the  Delius Oracle DB offender information.

## Continuous Integration
https://circleci.com/gh/noms-digital-studio/delius-offender-api

## Gradle commands

### Build and run tests

Due to its hosting in an external environment operated by Tolomy, the project needs to be built using
JDK version 8. Here are the instructions for setting this up on Ubuntu, but will be similar for other
development environments.

`$ sudo apt-install java-opendk-8-jdk`

`$ sudo update-alternatives --config java  (and select the jdk8 option)`

`$ exportJAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd54  (or set this in your ~/.bashrc file)`

* Restart IntelliJ 
* In File -> Project Structure -> select this JDK for the project
* In Settings -> Java Compiler check the bytecode version is set to 8
* In Settings -> Build Tools -> Gradle check the Gradle JVM also uses the JDK8 version

```
./gradlew build
```

### Assemble the app
```
./gradlew assemble
```

This makes the JAR executable by including a manifest. 

### Start the application default profile
Without additional configuration this mode uses an in memory H2 (empty) database and an in memory LDAP service which 
references a file resource in the JAR (schema.ldif).

```
java -jar build/libs/delius-offender-api.jar
```

### Start the application with Delius Oracle db

set SPRING_PROFILES_ACTIVE=oracle
```
SPRING_DATASOURCE_URL=jdbc:oracle:thin:@<VM Oracle IP address>:1521:DNDA SPRING_PROFILES_ACTIVE=oracle java -jar build/libs/delius-offender-api.jar
```

### Start the application with real LDAP
```
SPRING_LDAP_URLS=ldap://<ldap_addr>:<ldap_port> SPRING_LDAP_USERNAME=cn=orcladmin SPRING_LDAP_PASSWORD=<secret> java -jar build/libs/delius-offender-api.jar
```

### Start the app with the DEV profile. In this mode the H2 database contains a small data set
```
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

H2 Web console - <http://localhost:8080/api/h2-console>

JDBC URL: ```jdbc:h2:mem:testdb``` 

### Additional configuration
The application is configured with conventional Spring parameters.
The Spring documentation can be found here:

https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html

### Default port
Starts the application on port '8080'.
To override, set server.port (eg SERVER_PORT=8099 java -jar etc etc)

## Documentation
http://localhost:8080/api/swagger-ui.html

## Endpoints curl examples

### Logon
The logon body must be a fully qualified LDAP distinguished name:

cn=nick.redshaw,cn=Users,dc=moj,dc=com

```
curl -X POST http://localhost:8080/api/logon -H 'Content-Type: text/plain' -d 'uid=jihn,ou=people,dc=memorynotfound,dc=com'
```

### Get offender details
```
curl -X GET http://localhost:8080/api/offenders/12344568 -H 'Authorization: bearer <token>'
```

### Application info
```
curl -X GET http://localhost:8080/api/info
```

### Application health
```
curl -X GET http://localhost:8080/api/health
```
