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
    private final String UNALLOCATED_STAFF_CODE_SUFFIX = "U";

    @Autowired
    public OffenderManagerTransformer(StaffTransformer staffTransformer, TeamTransformer teamTransformer, ProbationAreaTransformer probationAreaTransformer) {
        this.staffTransformer = staffTransformer;
        this.teamTransformer = teamTransformer;
        this.probationAreaTransformer = probationAreaTransformer;
    }

    public CommunityOrPrisonOffenderManager offenderManagerOf(OffenderManager offenderManager) {
        return CommunityOrPrisonOffenderManager
                .builder()
                .staffCode(staffCodeOf(offenderManager))
                .isUnallocated(isUnallocated(offenderManager))
                .staff(Optional
                        .ofNullable(offenderManager.getStaff())
                        .map(staffTransformer::humanOf)
                        .orElse(null))
                .team(Optional
                        .ofNullable(offenderManager.getTeam())
                        .map(teamTransformer::teamOf)
                        .orElse(null))
                .isPrisonOffenderManager(false)
                .probationArea(Optional
                        .ofNullable(offenderManager.getProbationArea())
                        .map(probationAreaTransformer::probationAreaOf)
                        .orElse(null))
                .isResponsibleOfficer(isResponsibleOfficer(offenderManager.getResponsibleOfficer()))
                .build();
    }

    public CommunityOrPrisonOffenderManager offenderManagerOf(PrisonOffenderManager offenderManager) {
        return CommunityOrPrisonOffenderManager
                .builder()
                .staffCode(staffCodeOf(offenderManager))
                .isUnallocated(isUnallocated(offenderManager))
                .staff(Optional
                        .ofNullable(offenderManager.getStaff())
                        .map(staffTransformer::humanOf)
                        .orElse(null))
                .team(Optional
                        .ofNullable(offenderManager.getTeam())
                        .map(teamTransformer::teamOf)
                        .orElse(null))
                .isPrisonOffenderManager(true)
                .probationArea(Optional
                        .ofNullable(offenderManager.getProbationArea())
                        .map(probationAreaTransformer::probationAreaOf)
                        .orElse(null))
                .isResponsibleOfficer(isResponsibleOfficer(offenderManager.getResponsibleOfficer()))
                .build();
    }

    private String staffCodeOf(OffenderManager offenderManager) {
        return Optional
                .ofNullable(offenderManager.getStaff())
                .map(Staff::getOfficerCode)
                .orElse(null);
    }

    private String staffCodeOf(PrisonOffenderManager offenderManager) {
        return Optional
                .ofNullable(offenderManager.getStaff())
                .map(Staff::getOfficerCode)
                .orElse(null);
    }

    private boolean isUnallocated(OffenderManager offenderManager) {
        return Optional.ofNullable(staffCodeOf(offenderManager))
                .map(staffCode -> staffCode.endsWith(UNALLOCATED_STAFF_CODE_SUFFIX))
                .orElse(false);
    }

    private boolean isUnallocated(PrisonOffenderManager offenderManager) {
        return Optional.ofNullable(staffCodeOf(offenderManager))
                .map(staffCode -> staffCode.endsWith(UNALLOCATED_STAFF_CODE_SUFFIX))
                .orElse(false);
    }

    private boolean isResponsibleOfficer(ResponsibleOfficer responsibleOfficer) {
        return Optional
                .ofNullable(responsibleOfficer)
                .filter(officer -> officer.getEndDate() == null)
                .isPresent();
    }
}
