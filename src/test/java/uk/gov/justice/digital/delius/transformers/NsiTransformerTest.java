package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.Nsi;
import uk.gov.justice.digital.delius.jpa.standard.entity.NsiManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class NsiTransformerTest {

    private static final NsiTransformer TRANSFORMER = new NsiTransformer(new RequirementTransformer(), new ProbationAreaTransformer(new InstitutionTransformer()));

    @Test
    void testTransform() {
        final LocalDate expectedStartDate = LocalDate.of(2020, Month.APRIL, 1);
        final LocalDate actualStartDate = LocalDate.of(2020, Month.APRIL, 1);
        final LocalDate referralDate = LocalDate.of(2020, Month.FEBRUARY, 1);
        NsiManager nsiManager1 = NsiManager.builder()
                .startDate(LocalDate.of(2020,5,4))
                .endDate(LocalDate.of(2021,5,4))
                .probationArea(ProbationArea.builder()
                        .description("NPS London")
                        .code("N07")
                        .teams(Collections.emptyList())
                        .providerTeams(Collections.emptyList())
                        .build())
                .build();
        NsiManager nsiManager2 = NsiManager.builder()
                .startDate(LocalDate.of(2019,5,4))
                .endDate(LocalDate.of(2020,5,3))
                .probationArea(ProbationArea.builder()
                        .description("NPS Sheffield")
                        .code("N04")
                        .teams(Collections.emptyList())
                        .providerTeams(Collections.emptyList())
                        .build())
                .build();
        final uk.gov.justice.digital.delius.jpa.standard.entity.Nsi nsiEntity = uk.gov.justice.digital.delius.jpa.standard.entity.Nsi.builder()
            .nsiId(100L)
            .nsiStatus(uk.gov.justice.digital.delius.jpa.standard.entity.NsiStatus.builder().code("STX").description("").build())
            .nsiSubType(StandardReference.builder().codeDescription("Sub Type Desc").codeValue("STC").build())
            .nsiType(uk.gov.justice.digital.delius.jpa.standard.entity.NsiType.builder().code("TYPE").description("Type Desc").build())
            .actualStartDate(actualStartDate)
            .expectedStartDate(expectedStartDate)
            .referralDate(referralDate)
            .nsiManagers(Arrays.asList(nsiManager1, nsiManager2))
            .length(12L)
            .rqmnt(uk.gov.justice.digital.delius.jpa.standard.entity.Requirement.builder().activeFlag(1L).build()).build();

        final Nsi nsi = TRANSFORMER.nsiOf(nsiEntity);

        assertThat(nsi.getNsiId()).isEqualTo(100L);
        assertThat(nsi.getActualStartDate()).isEqualTo(actualStartDate);
        assertThat(nsi.getExpectedStartDate()).isEqualTo(expectedStartDate);
        assertThat(nsi.getNsiStatus().getCode()).isEqualTo("STX");
        assertThat(nsi.getReferralDate()).isEqualTo(referralDate);
        assertThat(nsi.getNsiType()).isEqualTo(KeyValue.builder().code("TYPE").description("Type Desc").build());
        assertThat(nsi.getNsiSubType()).isEqualTo(KeyValue.builder().code("STC").description("Sub Type Desc").build());
        assertThat(nsi.getRequirement().getActive()).isEqualTo(true);

        assertThat(nsi.getLength()).isEqualTo(12L);
        assertThat(nsi.getLengthUnit()).isEqualTo("Months");
        assertThat(nsi.getNsiManagers()).isNotNull();
        assertThat(nsi.getNsiManagers()).hasSize(2);
        assertThat(nsi.getNsiManagers().get(0).getStartDate()).isEqualTo(LocalDate.of(2020, 5, 4));
        assertThat(nsi.getNsiManagers().get(0).getEndDate()).isEqualTo(LocalDate.of(2021, 5, 4));
        assertThat(nsi.getNsiManagers().get(0).getProbationArea().getCode()).isEqualTo("N07");
        assertThat(nsi.getNsiManagers().get(0).getProbationArea().getDescription()).isEqualTo("NPS London");
        assertThat(nsi.getNsiManagers().get(1).getStartDate()).isEqualTo(LocalDate.of(2019, 5, 4));
        assertThat(nsi.getNsiManagers().get(1).getEndDate()).isEqualTo(LocalDate.of(2020, 5, 3));
        assertThat(nsi.getNsiManagers().get(1).getProbationArea().getCode()).isEqualTo("N04");
        assertThat(nsi.getNsiManagers().get(1).getProbationArea().getDescription()).isEqualTo("NPS Sheffield");
//        assertThat(nsi.getCourt()).isNotNull();
//        assertThat(nsi.getCourt().getCourtName()).isEqualTo(("Somethign"));
//        assertThat(nsi.getCourt?)
//
//        court | Harrogate Magistrates' Court
//
//        provider| NPS North East
//
//        team | Enforcement hub - Sheffield and Rotherham
//
//        officer | Unallocated
    }
}
