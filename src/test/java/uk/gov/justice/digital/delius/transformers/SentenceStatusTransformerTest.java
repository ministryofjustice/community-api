package uk.gov.justice.digital.delius.transformers;


import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.SentenceStatus;
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.DisposalType;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.MainOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Release;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SentenceStatusTransformerTest {

    public static final long SENTENCE_ID = 12345678L;
    public static final String CUSTODIAL_TYPE_CODE = "P";
    public static final String CUSTODIAL_TYPE_DESCRIPTION = "Post Sentence Supervision";
    public static final String OFFENCE_DESCRIPTION = "Common assault and battery - 10501";
    public static final String SENTENCE_DESCRIPTION = "ORA Adult Custody (inc PSS)";
    public static final LocalDate SENTENCE_DATE = LocalDate.of(2018, 12, 3);
    public static final LocalDate ACTUAL_RELEASE_DATE = LocalDate.of(2019, 7, 3);
    public static final LocalDate LICENCE_EXPIRY_DATE = LocalDate.of(2019, 11, 3);
    public static final Long LENGTH = 11L;
    public static final String LENGTH_UNIT = "Months";

    @Test
    public void mapToCustodialStatus() {
        Disposal disposal = buildDisposal(Collections.singletonList(Release.builder()
                .actualReleaseDate(ACTUAL_RELEASE_DATE.atTime(13, 0))
                .build()));

        SentenceStatus sentenceStatus = SentenceStatusTransformer.sentenceStatusOf(disposal);

        assertThat(sentenceStatus.getSentenceId()).isEqualTo(SENTENCE_ID);
        assertThat(sentenceStatus.getCustodialType().getCode()).isEqualTo(CUSTODIAL_TYPE_CODE);
        assertThat(sentenceStatus.getCustodialType().getDescription()).isEqualTo(CUSTODIAL_TYPE_DESCRIPTION);
        assertThat(sentenceStatus.getSentence().getDescription()).isEqualTo(SENTENCE_DESCRIPTION);
        assertThat(sentenceStatus.getMainOffence().getDescription()).isEqualTo(OFFENCE_DESCRIPTION);
        assertThat(sentenceStatus.getSentenceDate()).isEqualTo(SENTENCE_DATE);
        assertThat(sentenceStatus.getActualReleaseDate()).isEqualTo(ACTUAL_RELEASE_DATE);
        assertThat(sentenceStatus.getLicenceExpiryDate()).isEqualTo(LICENCE_EXPIRY_DATE);
        assertThat(sentenceStatus.getLength()).isEqualTo(LENGTH);
        assertThat(sentenceStatus.getLengthUnit()).isEqualTo(LENGTH_UNIT);
    }

    @Test
    public void givenNullCustodyOnSentence_whenMap_thenReturnNotInCustody() {
        Disposal disposal = buildDisposal(Collections.singletonList(Release.builder()
            .actualReleaseDate(ACTUAL_RELEASE_DATE.atTime(13, 0))
            .build()), false);

        SentenceStatus sentenceStatus = SentenceStatusTransformer.sentenceStatusOf(disposal);

        assertThat(sentenceStatus.getCustodialType().getCode()).isEqualTo(SentenceStatusTransformer.NO_CUSTODY_CODE);
        assertThat(sentenceStatus.getCustodialType().getDescription()).isEqualTo(SentenceStatusTransformer.NO_CUSTODY_DESCRIPTION);
        assertThat(sentenceStatus.getActualReleaseDate()).isNull();
        assertThat(sentenceStatus.getLicenceExpiryDate()).isNull();
    }

    @Test
    public void givenMultipleReleases_thenActualReleaseDateIsTheLatest() {
        Disposal disposal = buildDisposal(Arrays.asList(
                Release.builder()
                        .actualReleaseDate(LocalDateTime.of(2019, 6, 3, 13, 0))
                        .build(),
                Release.builder()
                        .actualReleaseDate(ACTUAL_RELEASE_DATE.atTime(13, 0))
                        .build(),
                Release.builder()
                        .actualReleaseDate(LocalDateTime.of(2019, 5, 3, 13, 0))
                        .build()
        ));

        SentenceStatus sentenceStatus = SentenceStatusTransformer.sentenceStatusOf(disposal);

        assertThat(sentenceStatus.getActualReleaseDate()).isEqualTo(ACTUAL_RELEASE_DATE);
    }

    @Test
    public void givenNoReleases_thenActualReleaseDateIsNull() {
        Disposal disposal = buildDisposal(Collections.emptyList());

        SentenceStatus sentenceStatus = SentenceStatusTransformer.sentenceStatusOf(disposal);

        assertThat(sentenceStatus.getActualReleaseDate()).isNull();
    }

    private Disposal buildDisposal(List<Release> releases) {
        return buildDisposal(releases, true);
    }

    private Disposal buildDisposal(List<Release> releases, boolean addCustody) {

        Custody custody = null;
        if (addCustody) {
            custody = Custody.builder()
                .releases(releases)
                .custodialStatus(StandardReference.builder()
                    .codeValue(CUSTODIAL_TYPE_CODE)
                    .codeDescription(CUSTODIAL_TYPE_DESCRIPTION)
                    .build())
                .pssStartDate(LICENCE_EXPIRY_DATE)
                .build();
        }

        return Disposal.builder()
                .disposalId(SENTENCE_ID)
                .disposalType(DisposalType.builder()
                        .description(SENTENCE_DESCRIPTION)
                        .build())
                .custody(custody)
                .event(Event.builder()
                        .mainOffence(MainOffence.builder()
                                .offence(Offence.builder()
                                        .description(OFFENCE_DESCRIPTION)
                                        .build()).build())
                        .build())
                .length(LENGTH)
                .startDate(SENTENCE_DATE)
                .build();
    }
}
