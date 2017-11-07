FROM java
MAINTAINER Mike Jackson <michael.jackson@digital.justice.gov.uk>

COPY build/libs/delius-offender-api.jar /root/delius-offender-api.jar

ENTRYPOINT ["/usr/bin/java", "-jar", "/root/delius-offender-api.jar"]
