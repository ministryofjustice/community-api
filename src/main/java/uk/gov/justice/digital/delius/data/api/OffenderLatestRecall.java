package uk.gov.justice.digital.delius.data.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OffenderLatestRecall {
    @Schema(description = "Last recall")
    private OffenderRecall lastRecall;
    @Schema(description = "Last release")
    private OffenderRelease lastRelease;

    public final static OffenderLatestRecall NO_RELEASE = OffenderLatestRecall.builder().lastRelease(null).lastRecall(null).build();
}
