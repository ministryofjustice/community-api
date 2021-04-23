package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.WellKnownAppointmentCreateRequest;

import java.time.OffsetDateTime;

import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;

class AppointmentCreateRequestTransformerTest {

    @Test
    public void appointmentCreateRequestFromWellKnownClientRequest() {
        OffsetDateTime start = now();
        OffsetDateTime end = start.plusHours(1);

        assertThat(AppointmentCreateRequestTransformer.appointmentOf(
            WellKnownAppointmentCreateRequest.builder()
                .appointmentStart(start)
                .appointmentEnd(end)
                .officeLocationCode("CRSHEFF")
                .notes("some notes")
                .build(),
            123456L,
            anIntegrationContext())).isEqualTo(
                    AppointmentCreateRequest.builder()
                        .requirementId(123456L)
                        .contactType("CRSAPT")
                        .appointmentStart(start)
                        .appointmentEnd(end)
                        .officeLocationCode("CRSHEFF")
                        .notes("some notes")
                        .providerCode("CRS")
                        .staffCode("CRSUATU")
                        .teamCode("CRSUAT")
                        .build()
        );
    }

    private IntegrationContext anIntegrationContext() {
        IntegrationContext integrationContext = new IntegrationContext();
        integrationContext.setProviderCode("CRS");
        integrationContext.setStaffCode("CRSUATU");
        integrationContext.setTeamCode("CRSUAT");
        integrationContext.getContactMapping().setAppointmentContactType("CRSAPT");
        return integrationContext;
    }
}