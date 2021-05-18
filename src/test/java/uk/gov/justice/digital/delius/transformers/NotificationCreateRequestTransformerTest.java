package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.data.api.ContextlessNotificationCreateRequest;
import uk.gov.justice.digital.delius.data.api.NotificationCreateRequest;
import uk.gov.justice.digital.delius.data.api.Nsi;

import java.time.OffsetDateTime;

import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;

class NotificationCreateRequestTransformerTest {

    @Test
    public void notificationCreateRequestFromContextlessClientRequest() {
        OffsetDateTime contactDateTime = now();

        assertThat(NotificationCreateRequestTransformer.notificationOf(
            ContextlessNotificationCreateRequest.builder()
                .contactDateTime(contactDateTime)
                .notes("some notes")
                .build(),
            Nsi.builder().nsiId(654321L).build(),
            anIntegrationContext())).isEqualTo(
            NotificationCreateRequest.builder()
                .nsiId(654321L)
                .requirementId(null)
                .contactType("CRS01")
                .contactDateTime(contactDateTime)
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
        integrationContext.getContactMapping().setNotificationContactType("CRS01");
        return integrationContext;
    }
}