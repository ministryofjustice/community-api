package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.Requirement;

import java.time.OffsetDateTime;

import static java.time.OffsetDateTime.now;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;

class AppointmentCreateRequestTransformerTest {

    private static final Long REQUIREMENT_ID = 1234567L;
    private static final String RAR_CONTACT_TYPE = "CRSAPT";
    private static final String NON_RAR_CONTACT_TYPE = "CRSSAA";

    @Test
    public void appointmentCreateRequestFromContextlessClientRequest() {
        OffsetDateTime start = now();
        OffsetDateTime end = start.plusHours(1);

        assertThat(
            AppointmentCreateRequestTransformer.appointmentOf(
                buildContextlessRequest(start, end, null),
                of(Requirement.builder().requirementId(REQUIREMENT_ID).build()),
                anIntegrationContext()
            )
        ).isEqualTo(
            buildExpectedTransformedRequest(start, end, REQUIREMENT_ID, RAR_CONTACT_TYPE)
        );
    }

    @Test
    public void appointmentCreateRequestFromContextlessClientRequest_withNoRequirement() {
        OffsetDateTime start = now();
        OffsetDateTime end = start.plusHours(1);

        assertThat(
            AppointmentCreateRequestTransformer.appointmentOf(
                buildContextlessRequest(start, end, null),
                empty(),
                anIntegrationContext()
            )
        ).isEqualTo(
            buildExpectedTransformedRequest(start, end, null, RAR_CONTACT_TYPE)
        );
    }

    @Test
    public void appointmentCreateRequestFromContextlessClientRequest_withNonRarFalse() {
        OffsetDateTime start = now();
        OffsetDateTime end = start.plusHours(1);

        assertThat(
            AppointmentCreateRequestTransformer.appointmentOf(
                buildContextlessRequest(start, end, false),
                of(Requirement.builder().requirementId(REQUIREMENT_ID).build()),
                anIntegrationContext())
        ).isEqualTo(
            buildExpectedTransformedRequest(start, end, REQUIREMENT_ID, RAR_CONTACT_TYPE)
        );
    }

    @Test
    public void appointmentCreateRequestFromContextlessClientRequest_withNonRarTrue() {
        OffsetDateTime start = now();
        OffsetDateTime end = start.plusHours(1);

        assertThat(
            AppointmentCreateRequestTransformer.appointmentOf(
                buildContextlessRequest(start, end, true),
                of(Requirement.builder().requirementId(REQUIREMENT_ID).build()),
                anIntegrationContext())
        ).isEqualTo(
            buildExpectedTransformedRequest(start, end, REQUIREMENT_ID, NON_RAR_CONTACT_TYPE)
        );
    }

    private ContextlessAppointmentCreateRequest buildContextlessRequest(OffsetDateTime start, OffsetDateTime end, Boolean nonRar) {
        return ContextlessAppointmentCreateRequest.builder()
            .appointmentStart(start)
            .appointmentEnd(end)
            .nonRar(nonRar)
            .officeLocationCode("CRSHEFF")
            .notes("some notes")
            .build();
    }

    private AppointmentCreateRequest buildExpectedTransformedRequest(OffsetDateTime start, OffsetDateTime end, Long requirementId, String contactType) {
        return AppointmentCreateRequest.builder()
            .requirementId(requirementId)
            .contactType(contactType)
            .appointmentStart(start)
            .appointmentEnd(end)
            .officeLocationCode("CRSHEFF")
            .notes("some notes")
            .providerCode("CRS")
            .staffCode("CRSUATU")
            .teamCode("CRSUAT")
            .build();
    }

    private IntegrationContext anIntegrationContext() {
        IntegrationContext integrationContext = new IntegrationContext();
        integrationContext.setProviderCode("CRS");
        integrationContext.setStaffCode("CRSUATU");
        integrationContext.setTeamCode("CRSUAT");
        integrationContext.getContactMapping().setAppointmentRarContactType(RAR_CONTACT_TYPE);
        integrationContext.getContactMapping().setAppointmentNonRarContactType(NON_RAR_CONTACT_TYPE);
        return integrationContext;
    }
}