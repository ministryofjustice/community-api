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

Starts the application on port '8080'.

## Documentation
http://localhost:8080/swagger-ui.html

## Endpoints curl examples

### Logon
```
curl -X POST http://localhost:8080/logon -H 'Content-Type: text/plain' -d 'uid=jihn,ou=people,dc=memorynotfound,dc=com'
```

### Get offender details
```
curl -X GET http://localhost:8080/offenders/12344568 -H 'Authorization: <token>'
```

