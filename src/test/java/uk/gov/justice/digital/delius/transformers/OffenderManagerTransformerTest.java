package uk.gov.justice.digital.delius.transformers;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.data.api.ContactableHuman;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.digital.delius.util.EntityHelper.*;

class OffenderManagerTransformerTest {

    @Test
    void staffNameDetailsTakenFromStaffInOffenderManager() {
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
                .isEqualTo(ContactableHuman.builder().forenames("John George").surname("Smith").email("no-one@nowhere.com").phoneNumber("020 1111 2222").build());
    }

    @Test
    void staffCodeTakenFromStaffInOffenderManager() {
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
    void teamsTakenFromTeamInOffenderManager() {
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
    void staffNameDetailsTakenFromStaffInPrisonOffenderManager() {
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
                .isEqualTo(ContactableHuman.builder().forenames("John George").surname("Smith").build());
    }

    @Test
    void staffCodeTakenFromStaffInPrisonOffenderManager() {
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
    void teamsTakenFromTeamInPrisonOffenderManager() {
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
    void unallocatedSetWhenStaffCodeEndsInLetterU() {
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
    void unallocatedSetWhenStaffCodeEndsInLetterUForPrisonOffenderManager() {
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
    void willSetPrisonOffenderManagerIndicator() {
        assertThat(OffenderManagerTransformer
                .offenderManagerOf(anActivePrisonOffenderManager())
                .getIsPrisonOffenderManager()).isTrue();
        assertThat(OffenderManagerTransformer.offenderManagerOf(anActiveOffenderManager())
                .getIsPrisonOffenderManager()).isFalse();

    }

    @Test
    void probationAreaCopiedFromOffenderManager() {
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
    void probationAreaCopiedFromPrisonOffenderManager() {
        var probationArea = OffenderManagerTransformer.offenderManagerOf(
                anActivePrisonOffenderManager()
                        .toBuilder()
                        .probationArea(
                                aPrisonProbationArea()
                                        .toBuilder()
                                        .code("WWI")
                                        .build())
                        .build()
        ).getProbationArea();

        assertThat(probationArea.getProbationAreaId()).isEqualTo(1L);
        assertThat(probationArea.getCode()).isEqualTo("WWI");
        assertThat(probationArea.getTeams()).hasSize(1);
    }

    @Test
    void probationAreaCopiedFromPrisonOffenderManagerTeamsExcluded() {
        var probationArea = OffenderManagerTransformer.offenderManagerOf(
            anActivePrisonOffenderManager()
                .toBuilder()
                .probationArea(
                    aPrisonProbationArea()
                        .toBuilder()
                        .code("WWI")
                        .build())
                .build(),
            false
        ).getProbationArea();

        assertThat(probationArea.getProbationAreaId()).isEqualTo(1L);
        assertThat(probationArea.getCode()).isEqualTo("WWI");
        assertThat(probationArea.getTeams()).isNull();
    }

    @Test
    void institutionCopiedFromPrisonOffenderManagerProbationArea() {
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
    void OffenderManagerMarkedAsResponsibleOfficerWhenLinkedAndNotEndDated() {
        assertThat(OffenderManagerTransformer.offenderManagerOf(
                anActiveOffenderManager()
                        .toBuilder()
                        .responsibleOfficers(
                                List.of(aResponsibleOfficer()
                                        .toBuilder()
                                        .endDateTime(null)
                                        .build()))
                        .build()
        ).getIsResponsibleOfficer()).isTrue();

        assertThat(OffenderManagerTransformer.offenderManagerOf(
                anActiveOffenderManager()
                        .toBuilder()
                        .responsibleOfficers(
                                List.of(aResponsibleOfficer()
                                        .toBuilder()
                                        .endDateTime(LocalDateTime.now())
                                        .build()))
                        .build()
        ).getIsResponsibleOfficer()).isFalse();

        assertThat(OffenderManagerTransformer.offenderManagerOf(
                anActiveOffenderManager()
                        .toBuilder()
                        .responsibleOfficers(List.of())
                        .build()
        ).getIsResponsibleOfficer()).isFalse();

    }
    @Test
    void prisonerOffenderManagerMarkedAsResponsibleOfficerWhenLinkedAndNotEndDated() {
        assertThat(OffenderManagerTransformer.offenderManagerOf(
                anActivePrisonOffenderManager()
                        .toBuilder()
                        .responsibleOfficers(
                                List.of(aResponsibleOfficer()
                                        .toBuilder()
                                        .endDateTime(null)
                                        .build()))
                        .build()
        ).getIsResponsibleOfficer()).isTrue();

        assertThat(OffenderManagerTransformer.offenderManagerOf(
                anActivePrisonOffenderManager()
                        .toBuilder()
                        .responsibleOfficers(List.of(
                                aResponsibleOfficer()
                                        .toBuilder()
                                        .endDateTime(LocalDateTime.now())
                                        .build()))
                        .build()
        ).getIsResponsibleOfficer()).isFalse();

        assertThat(OffenderManagerTransformer.offenderManagerOf(
                anActivePrisonOffenderManager()
                        .toBuilder()
                        .responsibleOfficers(List.of())
                        .build()
        ).getIsResponsibleOfficer()).isFalse();

    }
}
