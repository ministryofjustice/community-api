package uk.gov.justice.digital.delius.transformers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.data.api.OffenderLatestRecall;
import uk.gov.justice.digital.delius.data.api.OffenderRelease;
import uk.gov.justice.digital.delius.jpa.standard.entity.RInstitution;
import uk.gov.justice.digital.delius.jpa.standard.entity.Recall;
import uk.gov.justice.digital.delius.jpa.standard.entity.Release;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ReleaseTransformerTest {

    private static final Long SOME_RELEASE_ID = 123L;
    private final LocalDateTime SOME_DATE_TIME = LocalDateTime.now();
    private final LocalDate SOME_DATE = SOME_DATE_TIME.toLocalDate();
    private final RInstitution SOME_R_INSTITUTION = RInstitution.builder().code("MDI").description("HMP Moorland").build();
    private final String SOME_NOTES = "These are notes";
    private final StandardReference SOME_REASON = StandardReference.builder().codeValue("CODE_VALUE").codeDescription("Code description").build();
    private final String SOME_RELEASE_TYPE_CODE = "The release type code";
    private final String SOME_RELEASE_TYPE = "The release type";

    @Test
    public void offenderReleaseOf_valuesMappedCorrectly() {
        Release release = getDefaultRelease();

        OffenderRelease offenderRelease = ReleaseTransformer.offenderReleaseOf(release);

        assertThat(offenderRelease.getDate()).isEqualTo(SOME_DATE);
        assertThat(offenderRelease.getNotes()).isEqualTo(SOME_NOTES);
        assertThat(offenderRelease.getReason().getCode()).isEqualTo(SOME_RELEASE_TYPE_CODE);
        assertThat(offenderRelease.getReason().getDescription()).isEqualTo(SOME_RELEASE_TYPE);
    }

    @Test
    public void offenderReleaseOf_institutionTakenFromTransformer() {
        Release release = getDefaultRelease();

        OffenderRelease offenderRelease = ReleaseTransformer.offenderReleaseOf(release);

        assertThat(offenderRelease.getInstitution().getCode()).isEqualTo("MDI");
        assertThat(offenderRelease.getInstitution().getDescription()).isEqualTo("HMP Moorland");
    }

    @Test
    public void offenderLatestRecallOf_noRecalls_recallNull() {
        Release release = getDefaultRelease();

        OffenderLatestRecall offenderLatestRecall = ReleaseTransformer
                .offenderLatestRecallOf(release);

        assertThat(offenderLatestRecall.getLastRecall()).isNull();
    }

    @Test
    public void offenderLatestRecallOf_recallExists_recallIsReturned() {
        Release release = getDefaultReleaseBuilder().recalls(List.of(getDefaultRecall(SOME_DATE_TIME))).build();

        OffenderLatestRecall offenderLatestRecall = ReleaseTransformer
                .offenderLatestRecallOf(release);

        assertThat(offenderLatestRecall.getLastRecall().getDate()).isEqualTo(SOME_DATE);
    }

    private Release getDefaultRelease() {
        return getDefaultReleaseBuilder().build();
    }

    private Release.ReleaseBuilder getDefaultReleaseBuilder() {
        return Release.builder()
                .releaseId(SOME_RELEASE_ID)
                .actualReleaseDate(SOME_DATE_TIME)
                .institution(SOME_R_INSTITUTION)
                .notes(SOME_NOTES)
                .releaseType(StandardReference.builder().codeValue(SOME_RELEASE_TYPE_CODE).codeDescription(SOME_RELEASE_TYPE).build());
    }

    private Recall getDefaultRecall(LocalDateTime dateTime) {
        return getDefaultRecallBuilder(dateTime).build();
    }

    private Recall.RecallBuilder getDefaultRecallBuilder(LocalDateTime dateTime) {
        return Recall.builder()
                .releaseId(SOME_RELEASE_ID)
                .recallDate(dateTime)
                .notes(SOME_NOTES)
                .reason(SOME_REASON)
                .softDeleted(0L);
    }
}
