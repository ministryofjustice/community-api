package uk.gov.justice.digital.delius.transformers;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.OffenderLatestRecall;
import uk.gov.justice.digital.delius.data.api.OffenderRelease;
import uk.gov.justice.digital.delius.jpa.standard.entity.Release;

@Component
@AllArgsConstructor
public class ReleaseTransformer {

    private InstitutionTransformer institutionTransformer;
    private RecallTransformer recallTransformer;

    public OffenderRelease offenderReleaseOf(Release release) {
        final var releaseType = KeyValue.builder()
                .code(release.getReleaseType().getCodeValue())
                .description(release.getReleaseType().getCodeDescription())
                .build();
        return OffenderRelease.builder()
                .date(release.getActualReleaseDate().toLocalDate())
                .institution(institutionTransformer.institutionOf(release.getInstitution()))
                .notes(release.getNotes())
                .reason(releaseType)
                .build();
    }

    public OffenderLatestRecall offenderLatestRecallOf(Release release) {
        final var offenderRelease = offenderReleaseOf(release);
        final var offenderRecall =
                release.findLatestRecall()
                        .map(latestRecall -> recallTransformer.offenderRecallOf(latestRecall))
                        .orElse(null);
        return OffenderLatestRecall.builder()
                .lastRelease(offenderRelease)
                .lastRecall(offenderRecall)
                .build();
    }
}
