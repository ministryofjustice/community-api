[![CircleCI](https://circleci.com/gh/ministryofjustice/community-api/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/community-api)
[![Docker Repository on Quay](https://quay.io/repository/hmpps/community-api/status "Docker Repository on Quay")](https://quay.io/repository/hmpps/community-api)
[![Runbook](https://img.shields.io/badge/runbook-view-172B4D.svg?logo=confluence)](https://dsdmoj.atlassian.net/wiki/spaces/NOM/pages/1739325587/DPS+Runbook#Deploying-Community-API)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://community-api.test.probation.service.justice.gov.uk/swagger-ui/index.html)

# Community API
Spring Boot 2, Java 11 API for accessing Probation offenders information

The service provides REST access to the Delius Oracle DB.

Documentation including URLs for the various endpoints can be found on the test instance at
https://community-api.test.probation.service.justice.gov.uk/swagger-ui/index.html (VPN / trusted network required).
We are in the process of switching to using HMPPS Auth for all the endpoints in the service, these are identified by
(secure) after the name and are under `/secure` rather than `/api`.  The test instance is connected to the test instance
of Delius and the dev (t3) instance of Auth.

For credentials to access the test service or any questions on the service please ask at [#ask-hmpps-api in slack](https://mojdt.slack.com/archives/CR5ESQ8T1).

## Running locally

The easiest way to run the application locally is using `docker-compose`:
```
docker-compose pull && docker-compose up
```

This will grab the latest versions of auth and community api and start both - auth on 9090 and community api on 8080.

If running the application in intellij then you will need
```
--add-opens java.naming/com.sun.jndi.ldap=ALL-UNNAMED
```
in the jvm configuration, otherwise the LDAP configuration will not work.

## Testing locally

We use [Postman](https://www.postman.com/) to test the API calls.  The [swagger docs](http://localhost:8080/v2/api-docs?group=Community%20API)
can be imported as a collection to make it easier to test out a single call. If using browser based postman, you may need to copy the response from that URL and import the collection as raw text.

Calling the API is a two step process - obtaining a token from auth and then using the token in community API.  

### Obtaining a token

Within Postman in the Authorization tab select OAuth 2.0 type and add the authorization data to request headers.
The grant type should be Client Credentials, access token URL http://localhost:9090/auth/oauth/token, client ID
and secret both set to `community-api-client` and client authentication set to Send as Basic Auth header. Then click "Get new access token" and "Use access token". 

Alternatively using `curl`:
```
curl --location --request POST "http://localhost:9090/auth/oauth/token?grant_type=client_credentials" --header "Authorization: Basic $(echo -n community-api-client:community-api-client | base64)"  | jq .access_token
```
### Using the token

In the local environment some sample data is seeded automatically.  src/main/resources/db/data/V1_3__offender_X320741_data.sql
contains a single offender data so in Postman making a `GET` request to http://localhost:8080/secure/offenders/crn/X320741/identifiers
should then bring back the offender identifiers.

Alternatively using `curl`:
```
curl 'http://localhost:8080/secure/offenders/crn/X320741/identifiers' --header 'Authorization: Bearer XXX'
```
where `XXX` should be replaced by the token.

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

H2 Web console - <http://localhost:8080/h2-console>

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
curl -X GET http://localhost:8080/api/health/ping
```
## Health

- `api/health/ping`: provides a simple status `UP` return.  This should be used by dependent systems to check connectivity to whereabouts,
rather than calling the `api/health` endpoint.
- `api/health`: provides information about the application health and its dependencies.  This should only be used
by whereabouts health monitoring (e.g. pager duty) and not other systems who wish to find out the state of whereabouts.
- `api/info`: provides information about the version of deployed application.

## Alerts

### Inactivity alert

There is an alert in Application Insights called `Community API - Inactivity alert`. It fires if community-api hasn't received any successful requests in the last 10 minutes.

If the alert fires then look for any recent releases of community-api that may have introduced a problem. If not then ask in the MOJ Slack channel `hmpps-community-pr` for assistance. Note that the alert occasionally fires overnight during quiet periods - these can be ignored.
