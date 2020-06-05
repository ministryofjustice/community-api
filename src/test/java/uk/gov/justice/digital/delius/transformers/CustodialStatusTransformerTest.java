package uk.gov.justice.digital.delius.transformers;


import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.CustodialStatus;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class CustodialStatusTransformerTest {

    public static final long SENTENCE_ID = 12345678L;
    public static final String CUSTODIAL_TYPE_CODE = "P";
    public static final String CUSTODIAL_TYPE_DESCRIPTION = "Post Sentence Supervision";
    public static final String OFFENCE_DESCRIPTION = "Common assault and battery - 10501";
    public static final String SENTENCE_DESCRIPTION = "ORA Adult Custody (inc PSS)";
    public static final LocalDate SENTENCE_DATE = LocalDate.of(2018, 12, 3);
    public static final LocalDate ACTUAL_RELEASE_DATE = LocalDate.of(2019, 7, 3);
    public static final LocalDate LICENCE_EXPIRY_DATE = LocalDate.of(2019, 11, 3);
    public static final LocalDate PSS_END_DATE = LocalDate.of(2020, 6, 3);
    public static final Long LENGTH = 11L;
    public static final String LENGTH_UNIT = "Months";

    @Test
    public void mapToCustodialStatus() {
        Disposal disposal = Disposal.builder()
                .disposalId(SENTENCE_ID)
                .disposalType(DisposalType.builder()
                        .description(SENTENCE_DESCRIPTION)
                        .build())
                .custody(Custody.builder()
                        .releases(Collections.singletonList(Release.builder()
                                .actualReleaseDate(ACTUAL_RELEASE_DATE.atTime(13, 0))
                                .build()))
                        .custodialStatus(StandardReference.builder()
                                .codeValue(CUSTODIAL_TYPE_CODE)
                                .codeDescription(CUSTODIAL_TYPE_DESCRIPTION)
                                .build())
                        .pssStartDate(LICENCE_EXPIRY_DATE)
                        .pssEndDate(PSS_END_DATE)
                        .build())
                .event(Event.builder()
                        .mainOffence(MainOffence.builder()
                                .offence(Offence.builder()
                                        .description(OFFENCE_DESCRIPTION)
                                        .build()).build())
                        .build())
                .length(LENGTH)
                .startDate(SENTENCE_DATE)
                .build();

        CustodialStatus custodialStatus = new CustodialStatusTransformer().custodialStatusOf(disposal);

        assertThat(custodialStatus.getSentenceId()).isEqualTo(SENTENCE_ID);
        assertThat(custodialStatus.getCustodialType().getCode()).isEqualTo(CUSTODIAL_TYPE_CODE);
        assertThat(custodialStatus.getCustodialType().getDescription()).isEqualTo(CUSTODIAL_TYPE_DESCRIPTION);
        assertThat(custodialStatus.getSentence().getDescription()).isEqualTo(SENTENCE_DESCRIPTION);
        assertThat(custodialStatus.getMainOffence().getDescription()).isEqualTo(OFFENCE_DESCRIPTION);
        assertThat(custodialStatus.getSentenceDate()).isEqualTo(SENTENCE_DATE);
        assertThat(custodialStatus.getActualReleaseDate()).isEqualTo(ACTUAL_RELEASE_DATE);
        assertThat(custodialStatus.getLicenceExpiryDate()).isEqualTo(LICENCE_EXPIRY_DATE);
        assertThat(custodialStatus.getPssEndDate()).isEqualTo(PSS_END_DATE);
        assertThat(custodialStatus.getLength()).isEqualTo(LENGTH);
        assertThat(custodialStatus.getLengthUnit()).isEqualTo(LENGTH_UNIT);
    }
}