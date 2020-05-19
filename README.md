# Community API
Probation Community API.

The service provides REST access to the  Delius Oracle DB offender information.

## Continuous Integration
https://circleci.com/gh/noms-digital-studio/community-api

## Docker repository
quay.io/hmpps/community-api

Admin via https://quay.io/repository/hmpps/community-api?tab=tags

## Gradle commands

### Build and run tests
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
java -jar build/libs/community-api.jar
```

### Start the application for secure endpoints
 
When running locally and accessing the secure endpoints it is recommended to run the HMPPS Authentication server.

```
docker-compose up oauth
```

or to run the latest version of this API from the docker repository

```
docker-compose up
```

### Start the application with Delius Oracle db

set SPRING_PROFILES_ACTIVE=oracle
```
SPRING_DATASOURCE_URL=jdbc:oracle:thin:@<VM Oracle IP address>:1521:DNDA SPRING_PROFILES_ACTIVE=oracle java -jar build/libs/community-api.jar
```

### Start the application with real LDAP
```
SPRING_LDAP_URLS=ldap://<ldap_addr>:<ldap_port> SPRING_LDAP_USERNAME=cn=orcladmin SPRING_LDAP_PASSWORD=<secret> java -jar build/libs/community-api.jar
```

### Start the app with the DEV profile. In this mode the H2 database contains a small data set
```
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

H2 Web console - <http://localhost:8080/api/h2-console>

JDBC URL: ```jdbc:h2:mem:testdb;Mode=Oracle``` 
USER: sa
PASSWORD: <blank>


### Additional configuration
The application is configured with conventional Spring parameters.
The Spring documentation can be found here:

https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html

### Default port
Starts the application on port '8080'.
To override, set server.port (eg SERVER_PORT=8099 java -jar etc etc)

## Unit / Integration Tests

### Unit Tests
The unit tests can be found in the normal source set `test`.  This contains tests do not require running the Spring Boot application or priming the database.  They should be very quick to run.

The unit tests can be run with the command `./gradlew test`.

### Integration Tests
The integration tests can be found in the additional source set `testIntegration`.  This contains long running tests that generally start up the full application with local database.

The integration tests can be run with the command `./gradlew testIntegration`.

### Test sets plugin
Where did the new source set `testIntegration` come from?

The plugin `org.unbroken-dome.test-sets` is used to introduce a new source set called `testIntegration` which complements the existing source set `test`.  Note that the plugin handles everything a source set needs, including new configurations.  For example, Wiremock is now a dependency of the `testIntegrationImplementation` configuration as it is only needed by the integration tests.

### Running tests in CI
In the CircleCI config we run the gradle command `./gradlew check` which is intended to perform all validation of the project.

The `check` task always dependsOn the `test` - it now also depends on the `testIntegration` task.
 
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

### Application Ping
```
curl -X GET http://localhost:8080/api/ping
```
## Health

- `api/ping`: will respond `pong` to all requests.  This should be used by dependent systems to check connectivity to whereabouts,
rather than calling the `api/health` endpoint.
- `api/health`: provides information about the application health and its dependencies.  This should only be used
by whereabouts health monitoring (e.g. pager duty) and not other systems who wish to find out the state of whereabouts.
- `api/info`: provides information about the version of deployed application.
