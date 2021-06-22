package uk.gov.justice.digital.delius.controller.secure;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.justice.digital.delius.controller.ConflictingRequestException;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.controller.advice.SecureControllerAdvice;
import uk.gov.justice.digital.delius.data.api.Custody;
import uk.gov.justice.digital.delius.data.api.OffenderReleasedNotification;
import uk.gov.justice.digital.delius.service.CustodyService;
import uk.gov.justice.digital.delius.service.OffenderIdentifierService;

import java.time.LocalDate;

import static io.restassured.config.EncoderConfig.encoderConfig;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig.newConfig;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class CustodyController_releaseOffenderTest {

    private final CustodyService custodyService = mock(CustodyService.class);
    private OffenderIdentifierService offenderIdentifierService = mock(OffenderIdentifierService.class);

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.config = newConfig().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"));
        RestAssuredMockMvc.standaloneSetup(
            new CustodyResource(custodyService, offenderIdentifierService),
            new SecureControllerAdvice()
        );
    }

    @Test
    public void missingOffender_returnsNotFound() {
        when(custodyService.offenderReleased("G3333AA", LocalDate.of(2020, 11, 22))).
            thenThrow(new NotFoundException(String.format("Offender with nomsNumber %s not found", "G3333AA")));

        given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(createOffenderReleased(LocalDate.of(2020, 11, 22)))
            .when()
            .put("/secure/offenders/nomsNumber/G3333AA/released")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void multipleActiveConvictions_returnsConflict() {
        when(custodyService.offenderReleased("G3333AA", LocalDate.of(2020, 11, 22))).
            thenThrow(new ConflictingRequestException(String.format("Multiple active convictions found")));//;

        given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(createOffenderReleased(LocalDate.of(2020, 11, 22)))
            .when()
            .put("/secure/offenders/nomsNumber/G3333AA/released")
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    public void noActiveConvictions_returnsConflict() {
        when(custodyService.offenderReleased("G3333AA", LocalDate.of(2020, 11, 22)))
            .thenThrow(new ConflictingRequestException(String.format("No active convictions found")));

        given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(createOffenderReleased(LocalDate.of(2020, 11, 22)))
            .when()
            .put("/secure/offenders/nomsNumber/G3333AA/released")
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    public void singleActiveConviction_success() {
        when(custodyService.offenderReleased("G3333AA", LocalDate.of(2020, 11, 22)))
            .thenReturn(Custody.builder().build());

        given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(createOffenderReleased(LocalDate.of(2020, 11, 22)))
            .when()
            .put("/secure/offenders/nomsNumber/G3333AA/released")
            .then()
            .statusCode(HttpStatus.OK.value());
    }

    private OffenderReleasedNotification createOffenderReleased(LocalDate releaseDate) {
        return OffenderReleasedNotification
            .builder()
            .occurred(releaseDate)
            .build();
    }

}
