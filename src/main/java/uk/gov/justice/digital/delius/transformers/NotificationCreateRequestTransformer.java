package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.data.api.ContextlessNotificationCreateRequest;
import uk.gov.justice.digital.delius.data.api.NotificationCreateRequest;
import uk.gov.justice.digital.delius.data.api.Nsi;

public class NotificationCreateRequestTransformer {

    public static NotificationCreateRequest notificationOf(ContextlessNotificationCreateRequest request,
                                                           Nsi nsi,
                                                           IntegrationContext context) {

        final var contactMapping = context.getContactMapping();

        return NotificationCreateRequest.builder()
            .nsiId(nsi.getNsiId())
            .contactType(contactMapping.getNotificationContactType())
            .contactDateTime(request.getContactDateTime())
            .notes(request.getNotes())
            .providerCode(context.getProviderCode())
            .staffCode(context.getStaffCode())
            .teamCode(context.getTeamCode())
            .build();
    }
}
