package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.Nsi;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NsiTransformerTest {


    @Mock
    private uk.gov.justice.digital.delius.data.api.ProbationArea probationArea1;
    @Mock
    private uk.gov.justice.digital.delius.data.api.ProbationArea probationArea2;
    @Mock
    private uk.gov.justice.digital.delius.data.api.Court mockCourt;
    @Mock
    private uk.gov.justice.digital.delius.data.api.Team mockTeam1;
    @Mock
    private uk.gov.justice.digital.delius.data.api.Team mockTeam2;

    @Mock
    private ProbationAreaTransformer probationAreaTransformer;
    @Mock
    private CourtTransformer courtTransformer;
    @Mock
    private TeamTransformer teamTransformer;

    @Test
    void testTransform() {
        NsiTransformer transformer = new NsiTransformer(new RequirementTransformer(), probationAreaTransformer, courtTransformer, teamTransformer);
        final LocalDate expectedStartDate = LocalDate.of(2020, Month.APRIL, 1);
        final LocalDate actualStartDate = LocalDate.of(2020, Month.APRIL, 1);
        final LocalDate referralDate = LocalDate.of(2020, Month.FEBRUARY, 1);
        ProbationArea expectedProbationArea1 = ProbationArea.builder()
                .probationAreaId(1L)
                .build();
        Team expectedTeam1 = Team.builder()
                .teamId(1L)
                .build();
        NsiManager nsiManager1 = NsiManager.builder()
                .startDate(LocalDate.of(2020,5,4))
                .endDate(LocalDate.of(2021,5,4))
                .probationArea(expectedProbationArea1)
                .team(expectedTeam1)
                .build();
        ProbationArea expectedProbationArea2 = ProbationArea.builder()
                .probationAreaId(2L)
                .build();
        Team expectedTeam2 = Team.builder()
                .teamId(2L)
                .build();
        NsiManager nsiManager2 = NsiManager.builder()
                .startDate(LocalDate.of(2019,5,4))
                .endDate(LocalDate.of(2020,5,3))
                .probationArea(expectedProbationArea2)
                .team(expectedTeam2)
                .build();

        Court court = Court.builder().build();
        when(probationAreaTransformer.probationAreaOf(expectedProbationArea1)).thenReturn(probationArea1);
        when(probationAreaTransformer.probationAreaOf(expectedProbationArea2)).thenReturn(probationArea2);
        when(courtTransformer.courtOf(court)).thenReturn(mockCourt);
        when(teamTransformer.teamOf(expectedTeam1)).thenReturn(mockTeam1);
        when(teamTransformer.teamOf(expectedTeam2)).thenReturn(mockTeam2);

        final var nsiEntity = uk.gov.justice.digital.delius.jpa.standard.entity.Nsi.builder()
            .nsiId(100L)
            .nsiStatus(NsiStatus.builder().code("STX").description("").build())
            .nsiSubType(StandardReference.builder().codeDescription("Sub Type Desc").codeValue("STC").build())
            .nsiType(NsiType.builder().code("TYPE").description("Type Desc").build())
            .actualStartDate(actualStartDate)
            .expectedStartDate(expectedStartDate)
            .referralDate(referralDate)
            .nsiManagers(Arrays.asList(nsiManager1, nsiManager2))
            .length(12L)
            .event(Event.builder()
                    .court(court)
                    .build())
            .rqmnt(Requirement.builder().activeFlag(1L).build()).build();

        final Nsi nsi = transformer.nsiOf(nsiEntity);

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

        var manager1 = nsi.getNsiManagers().get(0);
        assertThat(manager1.getStartDate()).isEqualTo(LocalDate.of(2020, 5, 4));
        assertThat(manager1.getEndDate()).isEqualTo(LocalDate.of(2021, 5, 4));
        assertThat(manager1.getProbationArea()).isEqualTo(probationArea1);
        assertThat(manager1.getTeam()).isEqualTo(mockTeam1);

        var manager2 = nsi.getNsiManagers().get(1);
        assertThat(manager2.getStartDate()).isEqualTo(LocalDate.of(2019, 5, 4));
        assertThat(manager2.getEndDate()).isEqualTo(LocalDate.of(2020, 5, 3));
        assertThat(manager2.getProbationArea()).isEqualTo(probationArea2);
        assertThat(nsi.getCourt()).isNotNull();
        assertThat(nsi.getCourt()).isEqualTo(mockCourt);
        assertThat(manager2.getTeam()).isEqualTo(mockTeam2);

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
