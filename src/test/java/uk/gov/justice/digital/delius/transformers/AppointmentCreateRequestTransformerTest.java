package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.Nsi;
import uk.gov.justice.digital.delius.data.api.Requirement;

import java.time.OffsetDateTime;
import java.util.Optional;

import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;

class AppointmentCreateRequestTransformerTest {

    @Test
    public void appointmentCreateRequestFromContextlessClientRequest() {
        OffsetDateTime start = now();
        OffsetDateTime end = start.plusHours(1);

        assertThat(AppointmentCreateRequestTransformer.appointmentOf(
            ContextlessAppointmentCreateRequest.builder()
                .appointmentStart(start)
                .appointmentEnd(end)
                .notes("some notes")
                .countsTowardsRarDays(true)
                .build(),
            Nsi.builder().nsiId(654321L).build(),
            anIntegrationContext())).isEqualTo(
                    AppointmentCreateRequest.builder()
                        .nsiId(654321L)
                        .requirementId(null)
                        .contactType("CRSAPT")
                        .appointmentStart(start)
                        .appointmentEnd(end)
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
        integrationContext.getContactMapping().setAppointmentRarContactType("CRSAPT");
        integrationContext.getContactMapping().setAppointmentNonRarContactType("CRS01");
        return integrationContext;
    }
}