package uk.gov.justice.digital.delius.transformers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.config.DeliusIntegrationContextConfig.IntegrationContext;
import uk.gov.justice.digital.delius.data.api.ContextlessReferralEndRequest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class NsiPatchRequestTransformerTest {

    private static final OffsetDateTime END_DATE = OffsetDateTime.of(2021,6,1,0,0,0,1, ZoneOffset.UTC);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private IntegrationContext integrationContext;

    private NsiPatchRequestTransformer transformer = new NsiPatchRequestTransformer();

    @BeforeEach
    public void before() {

        integrationContext = new IntegrationContext();
        integrationContext.getContactMapping().setEndTypeToOutcomeType(
            new HashMap<>() {{
                this.put("CANCELLED", "CRS01");
                this.put("PREMATURELY_ENDED", "CRS02");
                this.put("COMPLETED", "CRS03");
            }}
        );
    }

    @Test
    public void transformsToJsonPatchMappingCorrectOutcome() throws JsonProcessingException {

        assertThat(
            objectMapper.writeValueAsString(
                transformer.mapEndTypeToOutcomeOf(buildRequest("CANCELLED"), integrationContext)
            )
        ).isEqualTo("[{\"op\":\"replace\",\"path\":\"/outcome\",\"value\":\"CRS01\"}," +
            "{\"op\":\"replace\",\"path\":\"/endDate\",\"value\":\"2021-06-01T01:00:00.000000001\"}," +
            "{\"op\":\"replace\",\"path\":\"/notes\",\"value\":\"some notes\"}]");

        assertThat(
            objectMapper.writeValueAsString(
                transformer.mapEndTypeToOutcomeOf(buildRequest("PREMATURELY_ENDED"), integrationContext)
            )
        ).isEqualTo("[{\"op\":\"replace\",\"path\":\"/outcome\",\"value\":\"CRS02\"}," +
            "{\"op\":\"replace\",\"path\":\"/endDate\",\"value\":\"2021-06-01T01:00:00.000000001\"}," +
            "{\"op\":\"replace\",\"path\":\"/notes\",\"value\":\"some notes\"}]");

        assertThat(
            objectMapper.writeValueAsString(
                transformer.mapEndTypeToOutcomeOf(buildRequest("COMPLETED"), integrationContext)
            )
        ).isEqualTo("[{\"op\":\"replace\",\"path\":\"/outcome\",\"value\":\"CRS03\"}," +
            "{\"op\":\"replace\",\"path\":\"/endDate\",\"value\":\"2021-06-01T01:00:00.000000001\"}," +
            "{\"op\":\"replace\",\"path\":\"/notes\",\"value\":\"some notes\"}]");
    }

    @Test
    public void throwsExceptionWhenNoMapping() {
        final var request = buildRequest("UnknownValue");

        IllegalStateException illegalStateException = Assertions.assertThrows(IllegalStateException.class,
            () -> transformer.mapEndTypeToOutcomeOf(request, integrationContext) );
        assertThat(illegalStateException.getMessage())
            .isEqualTo("Mapping does not exist for referral end type: UnknownValue");
    }

    private ContextlessReferralEndRequest buildRequest(final String endType) {
        return ContextlessReferralEndRequest.builder()
            .endType(endType)
            .endedAt(END_DATE)
            .notes("some notes")
            .build();
    }
}