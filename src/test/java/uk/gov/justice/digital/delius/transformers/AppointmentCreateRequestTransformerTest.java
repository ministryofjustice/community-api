package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.Nsi;
import uk.gov.justice.digital.delius.data.api.Requirement;

import java.time.OffsetDateTime;

import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;

class AppointmentCreateRequestTransformerTest {

    private static final String RAR_REQ_TYPE = "F";
    private static final String NON_RAR_CONTACT_TYPE = "CRS01";
    private static final String RAR_CONTACT_TYPE = "CRSAPT";

    @Test
    public void appointmentCreateRequestFromContextlessClientRequest_usingNsiNoRequirement() {
        OffsetDateTime start = now();
        OffsetDateTime end = start.plusHours(1);

        assertThat(AppointmentCreateRequestTransformer.appointmentOf(
            aContextlessAppointmentCreateRequest(start, end, false),
            Nsi.builder().nsiId(654321L).build(),
            anIntegrationContext())
        ).isEqualTo(
            anAppointmentCreateRequest(start, end, NON_RAR_CONTACT_TYPE, null)
        );
    }

    @Test
    public void appointmentCreateRequestFromContextlessClientRequest_usingNsiNonRarRequirement() {
        OffsetDateTime start = now();
        OffsetDateTime end = start.plusHours(1);

        assertThat(AppointmentCreateRequestTransformer.appointmentOf(
            aContextlessAppointmentCreateRequest(start, end, false),
            Nsi.builder().nsiId(654321L).requirement(
                Requirement.builder().requirementTypeMainCategory(KeyValue.builder().code("X").build()).build()
            ).build(),
            anIntegrationContext())
        ).isEqualTo(
            anAppointmentCreateRequest(start, end, NON_RAR_CONTACT_TYPE, null)
        );
    }

    @Test
    public void appointmentCreateRequestFromContextlessClientRequest_usingNsiRarRequirement_andNonRarAppt() {
        OffsetDateTime start = now();
        OffsetDateTime end = start.plusHours(1);

        assertThat(AppointmentCreateRequestTransformer.appointmentOf(
            aContextlessAppointmentCreateRequest(start, end, false),
            Nsi.builder().nsiId(654321L).requirement(
                Requirement.builder().requirementTypeMainCategory(KeyValue.builder().code(RAR_REQ_TYPE).build()).build()
            ).build(),
            anIntegrationContext())
        ).isEqualTo(
            anAppointmentCreateRequest(start, end, RAR_CONTACT_TYPE, false)
        );
    }

    @Test
    public void appointmentCreateRequestFromContextlessClientRequest_usingNsiRarRequirement_andRarAppt() {
        OffsetDateTime start = now();
        OffsetDateTime end = start.plusHours(1);

        assertThat(AppointmentCreateRequestTransformer.appointmentOf(
            aContextlessAppointmentCreateRequest(start, end, true),
            Nsi.builder().nsiId(654321L).requirement(
                Requirement.builder().requirementTypeMainCategory(KeyValue.builder().code(RAR_REQ_TYPE).build()).build()
            ).build(),
            anIntegrationContext())
        ).isEqualTo(
            anAppointmentCreateRequest(start, end, RAR_CONTACT_TYPE, true)
        );
    }

    private AppointmentCreateRequest anAppointmentCreateRequest(OffsetDateTime start, OffsetDateTime end, String contactType, Boolean rarActivity) {
        return AppointmentCreateRequest.builder()
            .nsiId(654321L)
            .requirementId(null)
            .contactType(contactType)
            .appointmentStart(start)
            .appointmentEnd(end)
            .notes("some notes")
            .providerCode("CRS")
            .staffCode("CRSUATU")
            .teamCode("CRSUAT")
            .rarActivity(rarActivity)
            .build();
    }

    private ContextlessAppointmentCreateRequest aContextlessAppointmentCreateRequest(OffsetDateTime start, OffsetDateTime end, boolean countsTowardsRarDays) {
        return ContextlessAppointmentCreateRequest.builder()
            .appointmentStart(start)
            .appointmentEnd(end)
            .notes("some notes")
            .countsTowardsRarDays(countsTowardsRarDays)
            .build();
    }

    private IntegrationContext anIntegrationContext() {
        IntegrationContext integrationContext = new IntegrationContext();
        integrationContext.setProviderCode("CRS");
        integrationContext.setStaffCode("CRSUATU");
        integrationContext.setTeamCode("CRSUAT");
        integrationContext.getContactMapping().setAppointmentRarContactType(RAR_CONTACT_TYPE);
        integrationContext.getContactMapping().setAppointmentNonRarContactType(NON_RAR_CONTACT_TYPE);
        integrationContext.setRequirementRehabilitationActivityType(RAR_REQ_TYPE);
        return integrationContext;
    }
}