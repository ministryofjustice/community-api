package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.Requirement;

import java.util.Optional;

public class AppointmentCreateRequestTransformer {

    public static AppointmentCreateRequest appointmentOf(ContextlessAppointmentCreateRequest request,
                                                         Optional<Requirement> requirement,
                                                         IntegrationContext context) {
        return AppointmentCreateRequest.builder()
                .requirementId(requirement.map(Requirement::getRequirementId).orElse(null))
                .contactType(context.getContactMapping().getAppointmentContactType())
                .appointmentStart(request.getAppointmentStart())
                .appointmentEnd(request.getAppointmentEnd())
                .officeLocationCode(request.getOfficeLocationCode())
                .notes(request.getNotes())
                .providerCode(context.getProviderCode())
                .staffCode(context.getStaffCode())
                .teamCode(context.getTeamCode())
                .build();
    }
}
