package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentCreateRequest;

public class AppointmentCreateRequestTransformer {

    public static AppointmentCreateRequest appointmentOf(ContextlessAppointmentCreateRequest request,
                                                         Long requirementId,
                                                         IntegrationContext context) {
        return AppointmentCreateRequest.builder()
                .requirementId(requirementId)
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
