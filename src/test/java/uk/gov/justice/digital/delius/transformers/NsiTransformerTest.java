package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.Nsi;
import uk.gov.justice.digital.delius.jpa.standard.entity.NsiStatus;
import uk.gov.justice.digital.delius.jpa.standard.entity.NsiType;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.Requirement;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;
import uk.gov.justice.digital.delius.util.EntityHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class NsiTransformerTest {

    @Test
    void testTransform() {
        final LocalDate expectedStartDate = LocalDate.of(2020, Month.APRIL, 1);
        final LocalDate actualStartDate = LocalDate.of(2020, Month.APRIL, 1);
        final LocalDate expectedEndDate = LocalDate.of(2021, Month.APRIL, 1);
        final LocalDate actualEndDate = LocalDate.of(2021, Month.APRIL, 1);
        final LocalDate referralDate = LocalDate.of(2020, Month.FEBRUARY, 1);
        final LocalDateTime statusDate = LocalDateTime.of(2020, Month.MAY, 1, 9, 0, 0);

        final var nsiEntity = buildNsiEntity(expectedStartDate, actualStartDate, expectedEndDate, actualEndDate, referralDate, statusDate)
                .toBuilder()
                .nsiManagers(List.of(
                        EntityHelper.aNsiManager()
                                .toBuilder()
                                .startDate(LocalDate.of(2020, 5, 4))
                                .endDate(LocalDate.of(2021, 5, 4))
                                .probationArea(EntityHelper.aProbationArea().toBuilder().code("N01").build())
                                .team(EntityHelper.aTeam().toBuilder().code("N01AAA").build())
                                .staff(EntityHelper.aStaff("N01AAA001"))
                                .build(),
                        EntityHelper.aNsiManager()
                                .toBuilder()
                                .startDate(LocalDate.of(2019, 5, 4))
                                .endDate(LocalDate.of(2020, 5, 3))
                                .probationArea(EntityHelper.aProbationArea().toBuilder().code("N02").build())
                                .team(EntityHelper.aTeam().toBuilder().code("N02AAA").build())
                                .staff(EntityHelper.aStaff("N02AAA001"))
                                .build()
                ))
                .intendedProvider(ProbationArea.builder()
                    .teams(Collections.singletonList(Team.builder()
                        .description("Any Team")
                        .build()))
                    .build())
                .build();

        final Nsi nsi = NsiTransformer.nsiOf(nsiEntity);

        assertThat(nsi.getNsiId()).isEqualTo(100L);
        assertThat(nsi.getActualStartDate()).isEqualTo(actualStartDate);
        assertThat(nsi.getExpectedStartDate()).isEqualTo(expectedStartDate);
        assertThat(nsi.getActualEndDate()).isEqualTo(actualEndDate);
        assertThat(nsi.getExpectedEndDate()).isEqualTo(expectedEndDate);
        assertThat(nsi.getNsiStatus().getCode()).isEqualTo("STX");
        assertThat(nsi.getReferralDate()).isEqualTo(referralDate);
        assertThat(nsi.getStatusDateTime()).isEqualTo(statusDate);
        assertThat(nsi.getNsiType()).isEqualTo(KeyValue.builder().code("TYPE").description("Type Desc").build());
        assertThat(nsi.getNsiSubType()).isEqualTo(KeyValue.builder().code("STC").description("Sub Type Desc").build());
        assertThat(nsi.getNsiOutcome()).isEqualTo(KeyValue.builder().code("OC").description("Outcome Type Desc").build());
        assertThat(nsi.getRequirement().getActive()).isEqualTo(true);
        assertThat(nsi.getLength()).isEqualTo(12L);
        assertThat(nsi.getLengthUnit()).isEqualTo("Months");
        assertThat(nsi.getNotes()).isEqualTo("Some notes");
        assertThat(nsi.getActive()).isEqualTo(true);
        assertThat(nsi.getSoftDeleted()).isEqualTo(false);
        assertThat(nsi.getExternalReference()).isEqualTo("urn:test:hello:123");

        assertThat(nsi.getNsiManagers()).isNotNull();
        assertThat(nsi.getNsiManagers()).hasSize(2);

        var manager1 = nsi.getNsiManagers().get(0);
        assertThat(manager1.getStartDate()).isEqualTo(LocalDate.of(2020, 5, 4));
        assertThat(manager1.getEndDate()).isEqualTo(LocalDate.of(2021, 5, 4));
        assertThat(manager1.getProbationArea().getCode()).isEqualTo("N01");
        assertThat(manager1.getTeam().getCode()).isEqualTo("N01AAA");
        assertThat(manager1.getStaff().getStaffCode()).isEqualTo("N01AAA001");

        var manager2 = nsi.getNsiManagers().get(1);
        assertThat(manager2.getStartDate()).isEqualTo(LocalDate.of(2019, 5, 4));
        assertThat(manager2.getEndDate()).isEqualTo(LocalDate.of(2020, 5, 3));
        assertThat(manager2.getProbationArea().getCode()).isEqualTo("N02");
        assertThat(manager2.getTeam().getCode()).isEqualTo("N02AAA");
        assertThat(manager2.getStaff().getStaffCode()).isEqualTo("N02AAA001");

        assertThat(nsi.getIntendedProvider().getTeams()).isNull();
    }

   private uk.gov.justice.digital.delius.jpa.standard.entity.Nsi buildNsiEntity(LocalDate expectedStartDate,
                                                                                LocalDate actualStartDate,
                                                                                LocalDate expectedEndDate,
                                                                                LocalDate actualEndDate,
                                                                                LocalDate referralDate,
                                                                                LocalDateTime statusDate) {
        return uk.gov.justice.digital.delius.jpa.standard.entity.Nsi.builder()
                .nsiId(100L)
                .nsiStatus(NsiStatus.builder().code("STX").description("").build())
                .nsiSubType(StandardReference.builder().codeDescription("Sub Type Desc").codeValue("STC").build())
                .nsiType(NsiType.builder().code("TYPE").description("Type Desc").build())
                .nsiOutcome(StandardReference.builder().codeDescription("Outcome Type Desc").codeValue("OC").build())
                .actualStartDate(actualStartDate)
                .expectedStartDate(expectedStartDate)
                .expectedEndDate(expectedEndDate)
                .actualEndDate(actualEndDate)
                .nsiStatusDateTime(statusDate)
                .referralDate(referralDate)
                .nsiManagers(Arrays.asList(EntityHelper.aNsiManager(), EntityHelper.aNsiManager()))
                .length(12L)
                .notes("Some notes")
                .activeFlag(1L)
                .softDeleted(false)
                .externalReference("urn:test:hello:123")
                .nsiManagers(List.of(EntityHelper.aNsiManager(), EntityHelper.aNsiManager()))
                .rqmnt(Requirement.builder().activeFlag(true).build()).build();
    }

}
