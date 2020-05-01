package uk.gov.justice.digital.delius.transformers;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.Nsi;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

class NsiTransformerTest {

    private static final NsiTransformer TRANSFORMER = new NsiTransformer(new RequirementTransformer());

    @Test
    void testTransform() {
        final LocalDate expectedStartDate = LocalDate.of(2020, Month.APRIL, 1);
        final LocalDate actualStartDate = LocalDate.of(2020, Month.APRIL, 1);
        final LocalDate referralDate = LocalDate.of(2020, Month.FEBRUARY, 1);
        final uk.gov.justice.digital.delius.jpa.standard.entity.Nsi nsiEntity = uk.gov.justice.digital.delius.jpa.standard.entity.Nsi.builder()
            .nsiId(100L)
            .nsiStatus(uk.gov.justice.digital.delius.jpa.standard.entity.NsiStatus.builder().code("STX").description("").build())
            .nsiSubType(StandardReference.builder().codeDescription("Sub Type Desc").codeValue("STC").build())
            .nsiType(uk.gov.justice.digital.delius.jpa.standard.entity.NsiType.builder().code("TYPE").description("Type Desc").build())
            .actualStartDate(actualStartDate)
            .expectedStartDate(expectedStartDate)
            .referralDate(referralDate)
            .rqmnt(uk.gov.justice.digital.delius.jpa.standard.entity.Requirement.builder().activeFlag(1L).build()).build();

        final Nsi nsi = TRANSFORMER.nsiOf(nsiEntity);
        assertThat(nsi.getActualStartDate()).isEqualTo(actualStartDate);
        assertThat(nsi.getExpectedStartDate()).isEqualTo(expectedStartDate);
        assertThat(nsi.getNsiStatus().getCode()).isEqualTo("STX");
        assertThat(nsi.getReferralDate()).isEqualTo(referralDate);
        assertThat(nsi.getNsiType()).isEqualTo(KeyValue.builder().code("TYPE").description("Type Desc").build());
        assertThat(nsi.getNsiSubType()).isEqualTo(KeyValue.builder().code("STC").description("Sub Type Desc").build());
        assertThat(nsi.getRequirement().getActive()).isEqualTo(true);
    }
}
