package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.PrisonOffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.ResponsibleOfficer;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;

import java.util.Objects;
import java.util.Optional;

public class OffenderManagerTransformer {
    private static final String UNALLOCATED_STAFF_CODE_SUFFIX = "U";

    public static CommunityOrPrisonOffenderManager offenderManagerOf(final OffenderManager offenderManager) {
        return CommunityOrPrisonOffenderManager
                .builder()
                .staffCode(staffCodeOf(offenderManager))
                .staffId(staffIdOf(offenderManager))
                .isUnallocated(isUnallocated(offenderManager))
                .staff(Optional
                        .ofNullable(offenderManager.getStaff())
                        .map(StaffTransformer::contactableHumanOf)
                        .orElse(null))
                .team(Optional
                        .ofNullable(offenderManager.getTeam())
                        .map(TeamTransformer::teamOf)
                        .orElse(null))
                .isPrisonOffenderManager(false)
                .probationArea(Optional
                        .ofNullable(offenderManager.getProbationArea())
                        .map(ProbationAreaTransformer::probationAreaOf)
                        .orElse(null))
                .isResponsibleOfficer(Objects.nonNull(offenderManager.getActiveResponsibleOfficer()))
                .fromDate(offenderManager.getAllocationDate())
                .build();
    }

    public static CommunityOrPrisonOffenderManager offenderManagerOf(final PrisonOffenderManager offenderManager) {
        return CommunityOrPrisonOffenderManager
                .builder()
                .staffCode(staffCodeOf(offenderManager))
                .staffId(staffIdOf(offenderManager))
                .isUnallocated(isUnallocated(offenderManager))
                .staff(Optional
                        .ofNullable(offenderManager.getStaff())
                        .map(StaffTransformer::contactableHumanOf)
                        .orElse(null))
                .team(Optional
                        .ofNullable(offenderManager.getTeam())
                        .map(TeamTransformer::teamOf)
                        .orElse(null))
                .isPrisonOffenderManager(true)
                .probationArea(Optional
                        .ofNullable(offenderManager.getProbationArea())
                        .map(ProbationAreaTransformer::probationAreaOf)
                        .orElse(null))
                .isResponsibleOfficer(Objects.nonNull(offenderManager.getActiveResponsibleOfficer()))
                .fromDate(offenderManager.getAllocationDate())
                .build();
    }

    private static String staffCodeOf(final OffenderManager offenderManager) {
        return Optional
                .ofNullable(offenderManager.getStaff())
                .map(Staff::getOfficerCode)
                .orElse(null);
    }

    private static Long staffIdOf(final OffenderManager offenderManager) {
        return Optional
                .ofNullable(offenderManager.getStaff())
                .map(Staff::getStaffId)
                .orElse(null);
    }

    private static String staffCodeOf(final PrisonOffenderManager offenderManager) {
        return Optional
                .ofNullable(offenderManager.getStaff())
                .map(Staff::getOfficerCode)
                .orElse(null);
    }

    private static Long staffIdOf(final PrisonOffenderManager offenderManager) {
        return Optional
                .ofNullable(offenderManager.getStaff())
                .map(Staff::getStaffId)
                .orElse(null);
    }

    private static boolean isUnallocated(final OffenderManager offenderManager) {
        return Optional.ofNullable(OffenderManagerTransformer.staffCodeOf(offenderManager))
                .map(staffCode -> staffCode.endsWith(UNALLOCATED_STAFF_CODE_SUFFIX))
                .orElse(false);
    }

    private static boolean isUnallocated(final PrisonOffenderManager offenderManager) {
        return Optional.ofNullable(OffenderManagerTransformer.staffCodeOf(offenderManager))
                .map(staffCode -> staffCode.endsWith(UNALLOCATED_STAFF_CODE_SUFFIX))
                .orElse(false);
    }

}
