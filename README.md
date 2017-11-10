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

### Start the application
```
./gradlew run
```

Start the application on port '8080'.

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

