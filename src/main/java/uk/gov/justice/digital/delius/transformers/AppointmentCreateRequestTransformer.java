package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.WellKnownAppointmentCreateRequest;

public class AppointmentCreateRequestTransformer {

    public static AppointmentCreateRequest appointmentOf(WellKnownAppointmentCreateRequest wkcRequest,
                                                         Long requirementId,
                                                         IntegrationContext context) {
        return AppointmentCreateRequest.builder()
                .requirementId(requirementId)
                .contactType(context.getContactMapping().getAppointmentContactType())
                .appointmentStart(wkcRequest.getAppointmentStart())
                .appointmentEnd(wkcRequest.getAppointmentEnd())
                .officeLocationCode(wkcRequest.getOfficeLocationCode())
                .notes(wkcRequest.getNotes())
                .providerCode(context.getProviderCode())
                .staffCode(context.getStaffCode())
                .teamCode(context.getTeamCode())
                .build();
    }
}
