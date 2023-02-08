#!/bin/bash
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

. "${SCRIPT_DIR}"/common-functions.bash

./gradlew wrapper --gradle-version=7.6 --distribution-type=bin

./gradlew useLatestVersions


$SED -i -e '/spring.io\/milestone/d' \
        -e '/mavenCentral/d' \
  build.gradle

$SED -i -z -e 's/repositories {\n}\n//' build.gradle

$SED -i -e 's#dependencies {#repositories {\n  maven { url = uri("https://repo.spring.io/milestone") }\n  mavenCentral()\n}\ndependencies {#' \
        -e 's/spring-boot") version "[0-9].[0-9].[0-9]\(-beta\)\?\(-beta-[0-9]\)\?/spring-boot") version "5.0.1/' \
        -e 's/hmpps-sqs-spring-boot-starter:[0-9].[0-9].[0-9]\(-beta\)\?\(-beta-[0-9]\)\?"/hmpps-sqs-spring-boot-starter:2.0.0-beta-10"/' \
  build.gradle

find . -name '*.java' -exec $SED -i \
  -e 's/import javax.servlet/import jakarta.servlet/' \
  -e 's/import javax.validation/import jakarta.validation/' \
  -e 's/import javax.transaction/import jakarta.transaction/' \
  -e 's/import javax.persistence/import jakarta.persistence/' \
  -e 's/EnableGlobalMethodSecurity/EnableMethodSecurity/' \
  -e 's/authorizeRequests/authorizeHttpRequests/' \
  -e 's/antMatchers/requestMatchers/' \
  -e 's/import org.hibernate.annotations.Type/import org.hibernate.type.YesNoConverter\nimport jakarta.persistence.Convert/' \
  -e 's/@Type(type = "yes_no")/@Convert(converter = YesNoConverter.class)/' \
  -e 's/antMatchers/requestMatchers/' \
  -e 's/import org.springframework.boot.web.server.LocalServerPort/import org.springframework.boot.test.web.server.LocalServerPort/' \
  -e 's/@JmsListener(destination = "\([^"]*\)", containerFactory/@SqsListener("\1", factory/' \
  -e 's/import org.springframework.jms.annotation.JmsListener/import io.awspring.cloud.sqs.annotation.SqsListener/' \
  -e 's/class \([a-zA-Z]*\) : WebSecurityConfigurerAdapter() {/class \1 {/' \
  -e 's/override fun configure(http: HttpSecurity) {/@Bean\n  fun filterChain(http: HttpSecurity): SecurityFilterChain {/' \
  -e '/import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter/d' \
  -e 's/EnableGlobalMethodSecurity/EnableMethodSecurity/' \
  -e '/@BatchSize/d' \
  -e '/import org.hibernate.annotations.BatchSize/d' \
  -e 's/OpenApiCustomiser/OpenApiCustomizer/g' \
  -e 's/signWith(SignatureAlgorithm.RS256, keyPair.private)/signWith(keyPair.private, SignatureAlgorithm.RS256)/' \
  -e 's/org.springframework.boot.autoconfigure.condition.ConditionalOnExpression/io.opentelemetry.api.trace.Span/' \
  -e '/import com.microsoft.applicationinsights.web.internal.ThreadContext/d' \
  -e '/ConditionalOnExpression.*applicationinsights/d' \
  -e '/ThreadContext.getRequestTelemetryContext().httpRequestTelemetry/d' \
  -e 's# properties\["username"\] = it }#\n          Span.current().setAttribute("username", it) // username in customDimensions\n          Span.current().setAttribute("enduser.id", it) // user_Id at the top level of the request\n        }#' \
  -e 's/properties\["clientId"\] = jwtBody.getClaim("client_id").toString()/Span.current().setAttribute("clientId", jwtBody.getClaim("client_id").toString())/' \
  {} \;

find . -name '*.kt' -exec $SED -i -z \
  -e 's/referencedColumnName = "\([a-zA-Z]*\)",\([[:space:]]*\)nullable = false/referencedColumnName = "\1",\2nullable = true/g' \
  {} \;

find . -name '*.yml' -exec $SED -i \
  -e 's/PostgreSQL10Dialect/PostgreSQLDialect/' \
  {} \;


$SED -i -e 's/com.vladmihalcea:hibernate-types-52/com.vladmihalcea:hibernate-types-60/' \
        -e 's/testImplementation("com.github.tomakehurst:wiremock-standalone:2[^"]*")/testImplementation("com.github.tomakehurst:wiremock-jre8-standalone:2.35.0")/' \
        -e 's/testImplementation("io.jsonwebtoken:jjwt:0.9.1")/testImplementation("io.jsonwebtoken:jjwt-impl:0.11.5")\n  testImplementation("io.jsonwebtoken:jjwt-jackson:0.11.5")/' \
        -e 's/implementation("org.springdoc:springdoc-openapi-ui:1[^"]*")/implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2")/' \
        -e 's/implementation("org.springdoc:springdoc-openapi-webflux-ui:1[^"]*")/implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.0.2")/' \
  build.gradle.kts

$SED -i -e '/implementation("org.springdoc:springdoc-openapi-kotlin/d' \
        -e '/implementation("org.springdoc:springdoc-openapi-data-rest/d' \
        -e '/implementation("org.springdoc:springdoc-openapi-security/d' \
  build.gradle.kts

find . -name 'App*InsightsConfig*.kt' -exec $SED -i -z \
  -e 's#import.*#import com.microsoft.applicationinsights.TelemetryClient\nimport org.springframework.context.annotation.Bean\nimport org.springframework.context.annotation.Configuration\n\n/**\n * TelemetryClient gets altered at runtime by the java agent and so is a no-op otherwise\n */\n@Configuration\nclass ApplicationInsightsConfiguration {\n  @Bean\n  fun telemetryClient(): TelemetryClient = TelemetryClient()\n}\n\nfun TelemetryClient.trackEvent(name: String, properties: Map<String, String>) = this.trackEvent(name, properties, null)\n#' \
  {} \;

grep -n ApplicationInsightsAppender src/main/resources/log*.xml && ls src/main/resources/log*.xml && echo -e "\nOld app insights appender detected - will need removing"