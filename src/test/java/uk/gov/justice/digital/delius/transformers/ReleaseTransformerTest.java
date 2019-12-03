package uk.gov.justice.digital.delius.transformers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.data.api.Institution;
import uk.gov.justice.digital.delius.data.api.OffenderLatestRecall;
import uk.gov.justice.digital.delius.data.api.OffenderRecall;
import uk.gov.justice.digital.delius.data.api.OffenderRelease;
import uk.gov.justice.digital.delius.jpa.standard.entity.RInstitution;
import uk.gov.justice.digital.delius.jpa.standard.entity.Recall;
import uk.gov.justice.digital.delius.jpa.standard.entity.Release;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@RunWith(MockitoJUnitRunner.class)
public class ReleaseTransformerTest {

    private static final Long SOME_RELEASE_ID = 123L;
    private final LocalDateTime SOME_DATE_TIME = LocalDateTime.now();
    private final LocalDate SOME_DATE = SOME_DATE_TIME.toLocalDate();
    private final RInstitution SOME_R_INSTITUTION = RInstitution.builder().build();
    private final Institution SOME_INSTITUTION = Institution.builder().build();
    private final String SOME_NOTES = "These are notes";
    private final StandardReference SOME_REASON = StandardReference.builder().codeValue("CODE_VALUE").codeDescription("Code description").build();
    private final OffenderRecall SOME_OFFENDER_RECALL = OffenderRecall.builder().date(SOME_DATE).build();
    private final String SOME_RELEASE_TYPE_CODE = "The release type code";
    private final String SOME_RELEASE_TYPE = "The release type";

    @Mock
    private InstitutionTransformer mockInstitutionTransformer;
    @Mock
    private RecallTransformer mockRecallTransformer;

    @InjectMocks
    private ReleaseTransformer releaseTransformer;

    @Test
    public void offenderReleaseOf_valuesMappedCorrectly() {
        Release release = getDefaultRelease();

        OffenderRelease offenderRelease = releaseTransformer.offenderReleaseOf(release);

        assertThat(offenderRelease.getDate()).isEqualTo(SOME_DATE);
        assertThat(offenderRelease.getNotes()).isEqualTo(SOME_NOTES);
        assertThat(offenderRelease.getReason().getCode()).isEqualTo(SOME_RELEASE_TYPE_CODE);
        assertThat(offenderRelease.getReason().getDescription()).isEqualTo(SOME_RELEASE_TYPE);
    }

    @Test
    public void offenderReleaseOf_institutionTakenFromTransformer() {
        Release release = getDefaultRelease();
        given(mockInstitutionTransformer.institutionOf(SOME_R_INSTITUTION)).willReturn(SOME_INSTITUTION);

        OffenderRelease offenderRelease = releaseTransformer.offenderReleaseOf(release);

        then(mockInstitutionTransformer).should().institutionOf(SOME_R_INSTITUTION);
        assertThat(offenderRelease.getInstitution()).isEqualTo(SOME_INSTITUTION);
    }

    @Test
    public void offenderLatestRecallOf_noRecalls_recallNull() {
        Release release = getDefaultRelease();

        OffenderLatestRecall offenderLatestRecall = releaseTransformer.offenderLatestRecallOf(release);

        assertThat(offenderLatestRecall.getLastRecall()).isNull();
    }

    @Test
    public void offenderLatestRecallOf_recallExists_recallIsReturned() {
        Release release = getDefaultReleaseBuilder().recalls(List.of(getDefaultRecall(SOME_DATE_TIME))).build();
        given(mockRecallTransformer.offenderRecallOf(any(Recall.class))).willReturn(SOME_OFFENDER_RECALL);

        OffenderLatestRecall offenderLatestRecall = releaseTransformer.offenderLatestRecallOf(release);

        assertThat(offenderLatestRecall.getLastRecall()).isEqualTo(SOME_OFFENDER_RECALL);
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
