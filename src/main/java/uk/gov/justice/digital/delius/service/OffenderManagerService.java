package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.PrisonOffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.RInstitution;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.entity.User;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.OffenderManagerTransformer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@Slf4j
@AllArgsConstructor
public class OffenderManagerService {

    private final OffenderRepository offenderRepository;
    private final StaffService staffService;

    @Transactional(readOnly = true)
    public Optional<List<CommunityOrPrisonOffenderManager>> getAllOffenderManagersForCrn(final String crn, final boolean includeProbationAreaTeams) {
        return offenderRepository.findByCrn(crn).map(offender -> getAllOffenderManagers(offender, includeProbationAreaTeams));
    }

    private static <T> List<T> combine(final List<T> first, final List<T> second) {
        return Stream.of(first, second)
                .flatMap(Collection::stream)
                .toList();
    }

    private List<CommunityOrPrisonOffenderManager> getAllOffenderManagers(final Offender offender, final boolean includeProbationAreaTeams) {
        return combine(
                    offender.getOffenderManagers()
                        .stream()
                        .filter(OffenderManager::isActive)
                        .map(this::addLdapFields)
                        .map(offMgr -> OffenderManagerTransformer.offenderManagerOf(offMgr, includeProbationAreaTeams))
                        .toList(),
                offender.getPrisonOffenderManagers()
                        .stream()
                        .filter(PrisonOffenderManager::isActive)
                        .map(offMgr -> OffenderManagerTransformer.offenderManagerOf(offMgr, includeProbationAreaTeams))
                        .toList()
        );
    }

    OffenderManager addLdapFields(OffenderManager offenderManager) {
        Optional.ofNullable(offenderManager.getStaff())
            .map(Staff::getUser)
            .map(User::getDistinguishedName)
            .flatMap(staffService::getStaffDetailsByUsername)
            .ifPresent(staffDetails -> {
                offenderManager.setTelephoneNumber(staffDetails.getTelephoneNumber());
                offenderManager.setEmailAddress(staffDetails.getEmail());
            });
        return offenderManager;
    }

}
