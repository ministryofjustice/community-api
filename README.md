# Delius API
New Tech Delius: Offender API.

The service provides REST access to the  Delius Oracle DB offender information.

## Continuous Integration
https://circleci.com/gh/noms-digital-studio/delius-offender-api

## Gradle commands

### Build and run tests
```
./gradlew build
```

### Assessmble the app
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

### Start the application Oracle profile
```
SPRING_DATASOURCE_URL=jdbc:oracle:thin:@<VM Oracle IP address>:1521:DNDA SPRING_PROFILES_ACTIVE=oracle java -jar build/libs/delius-offender-api.jar
```

### Additional configuration
To override the default data source and ldap server, use the Spring conventional parameters.

An example for connecting to a remote Ldap service is described in application.yml. 

The Spring documentation can be found here:

https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html


Starts the application on port '8080'.

## Documentation
http://localhost:8080/api/swagger-ui.html

## Endpoints curl examples

### Logon
```
curl -X POST http://localhost:8080/api/logon -H 'Content-Type: text/plain' -d 'uid=jihn,ou=people,dc=memorynotfound,dc=com'
```

### Get offender details
```
curl -X GET http://localhost:8080/api/offenders/12344568 -H 'Authorization: <token>'
```

### Application info
```
curl -X GET http://localhost:8080/api/info
```

### Application health
```
curl -X GET http://localhost:8080/api/health
```


