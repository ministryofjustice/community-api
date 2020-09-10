package uk.gov.justice.digital.delius.controller.secure;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.controller.advice.SecureControllerAdvice;
import uk.gov.justice.digital.delius.service.OffenderUpdatesService;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OffenderUpdatesResourceTest {

    private final OffenderUpdatesService offenderUpdatesService = mock(OffenderUpdatesService.class);


    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.standaloneSetup(
                new OffenderUpdatesResource(offenderUpdatesService),
                new SecureControllerAdvice()
        );
        when(offenderUpdatesService.getNextUpdate()).thenReturn(Optional.empty());
    }


    @Nested
    @DisplayName("offenders/nextEvent")
    class GetNextEvent {

        @Test
        @DisplayName("Will get the next event")
        void willGetNextEvent() {
        }

        @Test
        @DisplayName("Will return 404 when no new events present")
        void willReturnNotFoundWhenNoNewEvents() {
        }
    }


}