package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.data.api.AppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.ContextlessAppointmentCreateRequest;
import uk.gov.justice.digital.delius.data.api.Nsi;

public class AppointmentCreateRequestTransformer {

    public static AppointmentCreateRequest appointmentOf(ContextlessAppointmentCreateRequest request,
                                                         Nsi nsi,
                                                         IntegrationContext context) {
        final var contactMapping = context.getContactMapping();

        return AppointmentCreateRequest.builder()
                .nsiId(nsi.getNsiId())
                .contactType(request.getCountsTowardsRarDays() ?
                    contactMapping.getAppointmentRarContactType() :
                    contactMapping.getAppointmentNonRarContactType())
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
