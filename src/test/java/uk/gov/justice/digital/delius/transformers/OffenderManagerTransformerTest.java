package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.gov.justice.digital.delius.data.api.Human;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.digital.delius.util.EntityHelper.*;

public class OffenderManagerTransformerTest {
    private final OffenderManagerTransformer offenderManagerTransformer =
            new OffenderManagerTransformer(
                    new StaffTransformer(
                            new TeamTransformer()),
                    new TeamTransformer(),
                    new ProbationAreaTransformer(
                            new InstitutionTransformer()));

    @Test
    public void staffNameDetailsTakenFromStaffInOffenderManager() {
        assertThat(OffenderManagerTransformer.offenderManagerOf(
                anOffenderManager(
                        aStaff()
                                .toBuilder()
                                .forename("John")
                                .surname("Smith")
                                .forname2("George")
                                .build(),
                        aTeam()
                )
        ).getStaff())
                .isEqualTo(Human.builder().forenames("John George").surname("Smith").build());
    }

    @Test
    public void staffCodeTakenFromStaffInOffenderManager() {
        assertThat(OffenderManagerTransformer.offenderManagerOf(
                anOffenderManager(
                        aStaff()
                                .toBuilder()
                                .forename("John")
                                .surname("Smith")
                                .officerCode("XXXXX")
                                .build(),
                        aTeam())).getStaffCode())
                .isEqualTo("XXXXX");
    }

    @Test
    public void teamsTakenFromTeamInOffenderManager() {
        assertThat(OffenderManagerTransformer.offenderManagerOf(
                anOffenderManager(
                        aStaff()
                                .toBuilder()
                                .teams(ImmutableList.of(aTeam("BB"), aTeam("AA")))
                                .build(),
                        aTeam("AA"))).getTeam().getCode())
                .isEqualTo("AA");
    }

    @Test
    public void staffNameDetailsTakenFromStaffInPrisonOffenderManager() {
        assertThat(OffenderManagerTransformer.offenderManagerOf(
                aPrisonOffenderManager(
                        aStaff()
                                .toBuilder()
                                .forename("John")
                                .surname("Smith")
                                .forname2("George")
                                .build(),
                        aTeam()
                )
        ).getStaff())
                .isEqualTo(Human.builder().forenames("John George").surname("Smith").build());
    }

    @Test
    public void staffCodeTakenFromStaffInPrisonOffenderManager() {
        assertThat(OffenderManagerTransformer.offenderManagerOf(
                aPrisonOffenderManager(
                        aStaff()
                                .toBuilder()
                                .forename("John")
                                .surname("Smith")
                                .officerCode("XXXXX")
                                .build(),
                        aTeam())).getStaffCode())
                .isEqualTo("XXXXX");
    }

    @Test
    public void teamsTakenFromTeamInPrisonOffenderManager() {
        assertThat(OffenderManagerTransformer.offenderManagerOf(
                aPrisonOffenderManager(
                        aStaff()
                                .toBuilder()
                                .teams(ImmutableList.of(aTeam("BB"), aTeam("AA")))
                                .build(),
                        aTeam("AA"))).getTeam().getCode())
                .isEqualTo("AA");
    }

    @Test
    public void unallocatedSetWhenStaffCodeEndsInLetterU() {
        assertThat(OffenderManagerTransformer.offenderManagerOf(
                anOffenderManager(
                        aStaff()
                                .toBuilder()
                                .officerCode("CO171T")
                                .build(),
                        aTeam()
                )
        ).getIsUnallocated()).isFalse();
        assertThat(OffenderManagerTransformer.offenderManagerOf(
                anOffenderManager(
                        aStaff()
                                .toBuilder()
                                .officerCode("CO171U")
                                .build(),
                        aTeam()
                )
        ).getIsUnallocated()).isTrue();
    }

    @Test
    public void unallocatedSetWhenStaffCodeEndsInLetterUForPrisonOffenderManager() {
        assertThat(OffenderManagerTransformer.offenderManagerOf(
                aPrisonOffenderManager(
                        aStaff()
                                .toBuilder()
                                .officerCode("CO171T")
                                .build(),
                        aTeam()
                )
        ).getIsUnallocated()).isFalse();
        assertThat(OffenderManagerTransformer.offenderManagerOf(
                aPrisonOffenderManager(
                        aStaff()
                                .toBuilder()
                                .officerCode("CO171U")
                                .build(),
                        aTeam()
                )
        ).getIsUnallocated()).isTrue();
    }

    @Test
    public void willSetPrisonOffenderManagerIndicator() {
        assertThat(OffenderManagerTransformer
                .offenderManagerOf(anActivePrisonOffenderManager())
                .getIsPrisonOffenderManager()).isTrue();
        assertThat(OffenderManagerTransformer.offenderManagerOf(anActiveOffenderManager())
                .getIsPrisonOffenderManager()).isFalse();

    }

    @Test
    public void probationAreaCopiedFromOffenderManager() {
        assertThat(OffenderManagerTransformer.offenderManagerOf(
                anActiveOffenderManager()
                        .toBuilder()
                        .probationArea(
                                aProbationArea()
                                        .toBuilder()
                                        .code("N02")
                                        .build())
                        .build()
        ).getProbationArea()
                .getCode())
                .isEqualTo("N02");
    }

    @Test
    public void probationAreaCopiedFromPrisonOffenderManager() {
        assertThat(OffenderManagerTransformer.offenderManagerOf(
                anActivePrisonOffenderManager()
                        .toBuilder()
                        .probationArea(
                                aPrisonProbationArea()
                                        .toBuilder()
                                        .code("WWI")
                                        .build())
                        .build()
        ).getProbationArea()
                .getCode())
                .isEqualTo("WWI");
    }

    @Test
    public void institutionCopiedFromPrisonOffenderManagerProbationArea() {
        assertThat(OffenderManagerTransformer.offenderManagerOf(
                anActivePrisonOffenderManager()
                        .toBuilder()
                        .probationArea(
                                aPrisonProbationArea()
                                        .toBuilder()
                                        .institution(aPrisonInstitution()
                                                .toBuilder()
                                                .code("WWIHMP")
                                                .build())
                                        .build())
                        .build()
        ).getProbationArea()
                .getInstitution().getCode())
                .isEqualTo("WWIHMP");
    }

    @Test
    public void OffenderManagerMarkedAsResponsibleOfficerWhenLinkedAndNotEndDated() {
        assertThat(OffenderManagerTransformer.offenderManagerOf(
                anActiveOffenderManager()
                        .toBuilder()
                        .responsibleOfficer(
                                aResponsibleOfficer()
                                        .toBuilder()
                                        .endDateTime(null)
                                        .build())
                        .build()
        ).getIsResponsibleOfficer()).isTrue();

        assertThat(OffenderManagerTransformer.offenderManagerOf(
                anActiveOffenderManager()
                        .toBuilder()
                        .responsibleOfficer(
                                aResponsibleOfficer()
                                        .toBuilder()
                                        .endDateTime(LocalDateTime.now())
                                        .build())
                        .build()
        ).getIsResponsibleOfficer()).isFalse();

        assertThat(OffenderManagerTransformer.offenderManagerOf(
                anActiveOffenderManager()
                        .toBuilder()
                        .responsibleOfficer(null)
                        .build()
        ).getIsResponsibleOfficer()).isFalse();

    }
    @Test
    public void prisonerOffenderManagerMarkedAsResponsibleOfficerWhenLinkedAndNotEndDated() {
        assertThat(OffenderManagerTransformer.offenderManagerOf(
                anActivePrisonOffenderManager()
                        .toBuilder()
                        .responsibleOfficer(
                                aResponsibleOfficer()
                                        .toBuilder()
                                        .endDateTime(null)
                                        .build())
                        .build()
        ).getIsResponsibleOfficer()).isTrue();

        assertThat(OffenderManagerTransformer.offenderManagerOf(
                anActivePrisonOffenderManager()
                        .toBuilder()
                        .responsibleOfficer(
                                aResponsibleOfficer()
                                        .toBuilder()
                                        .endDateTime(LocalDateTime.now())
                                        .build())
                        .build()
        ).getIsResponsibleOfficer()).isFalse();

        assertThat(OffenderManagerTransformer.offenderManagerOf(
                anActivePrisonOffenderManager()
                        .toBuilder()
                        .responsibleOfficer(null)
                        .build()
        ).getIsResponsibleOfficer()).isFalse();

    }
}