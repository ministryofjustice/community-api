package uk.gov.justice.digital.delius.transformers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.PrisonOffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.ResponsibleOfficer;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;

import java.util.Optional;

@Component
public class OffenderManagerTransformer {
    private final StaffTransformer staffTransformer;
    private final TeamTransformer teamTransformer;
    private final ProbationAreaTransformer probationAreaTransformer;
    private static final String UNALLOCATED_STAFF_CODE_SUFFIX = "U";

    @Autowired
    public OffenderManagerTransformer(StaffTransformer staffTransformer, TeamTransformer teamTransformer, ProbationAreaTransformer probationAreaTransformer) {
        this.staffTransformer = staffTransformer;
        this.teamTransformer = teamTransformer;
        this.probationAreaTransformer = probationAreaTransformer;
    }

    public static CommunityOrPrisonOffenderManager offenderManagerOf(OffenderManager offenderManager) {
        return CommunityOrPrisonOffenderManager
                .builder()
                .staffCode(staffCodeOf(offenderManager))
                .isUnallocated(isUnallocated(offenderManager))
                .staff(Optional
                        .ofNullable(offenderManager.getStaff())
                        .map(StaffTransformer::humanOf)
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
                .isResponsibleOfficer(isResponsibleOfficer(offenderManager.getResponsibleOfficer()))
                .fromDate(offenderManager.getAllocationDate())
                .build();
    }

    public static CommunityOrPrisonOffenderManager offenderManagerOf(PrisonOffenderManager offenderManager) {
        return CommunityOrPrisonOffenderManager
                .builder()
                .staffCode(staffCodeOf(offenderManager))
                .isUnallocated(isUnallocated(offenderManager))
                .staff(Optional
                        .ofNullable(offenderManager.getStaff())
                        .map(StaffTransformer::humanOf)
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
                .isResponsibleOfficer(isResponsibleOfficer(offenderManager.getResponsibleOfficer()))
                .fromDate(offenderManager.getAllocationDate())
                .build();
    }

    private static String staffCodeOf(OffenderManager offenderManager) {
        return Optional
                .ofNullable(offenderManager.getStaff())
                .map(Staff::getOfficerCode)
                .orElse(null);
    }

    private static String staffCodeOf(PrisonOffenderManager offenderManager) {
        return Optional
                .ofNullable(offenderManager.getStaff())
                .map(Staff::getOfficerCode)
                .orElse(null);
    }

    private static boolean isUnallocated(OffenderManager offenderManager) {
        return Optional.ofNullable(OffenderManagerTransformer.staffCodeOf(offenderManager))
                .map(staffCode -> staffCode.endsWith(UNALLOCATED_STAFF_CODE_SUFFIX))
                .orElse(false);
    }

    private static boolean isUnallocated(PrisonOffenderManager offenderManager) {
        return Optional.ofNullable(OffenderManagerTransformer.staffCodeOf(offenderManager))
                .map(staffCode -> staffCode.endsWith(UNALLOCATED_STAFF_CODE_SUFFIX))
                .orElse(false);
    }

    private static boolean isResponsibleOfficer(ResponsibleOfficer responsibleOfficer) {
        return Optional
                .ofNullable(responsibleOfficer)
                .filter(officer -> officer.getEndDateTime() == null)
                .isPresent();
    }
}
