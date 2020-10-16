package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.controller.ConflictingRequestException;
import uk.gov.justice.digital.delius.controller.InvalidRequestException;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.CreatePrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.ResponsibleOfficerSwitch;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.PrisonOffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.RInstitution;
import uk.gov.justice.digital.delius.jpa.standard.entity.ResponsibleOfficer;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.entity.Team;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.PrisonOffenderManagerRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ProbationAreaRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ResponsibleOfficerRepository;
import uk.gov.justice.digital.delius.transformers.OffenderManagerTransformer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;

@Service
@Slf4j
@AllArgsConstructor
public class OffenderManagerService {

    private final OffenderRepository offenderRepository;
    private final ProbationAreaRepository probationAreaRepository;
    private final PrisonOffenderManagerRepository prisonOffenderManagerRepository;
    private final ResponsibleOfficerRepository responsibleOfficerRepository;
    private final StaffService staffService;
    private final TeamService teamService;
    private final ReferenceDataService referenceDataService;
    private final ContactService contactService;

    @Transactional(readOnly = true)
    public Optional<List<CommunityOrPrisonOffenderManager>> getAllOffenderManagersForNomsNumber(final String nomsNumber) {
        return offenderRepository.findByNomsNumber(nomsNumber).map(this::getAllOffenderManagers);
    }

    @Transactional
    public Optional<CommunityOrPrisonOffenderManager> allocatePrisonOffenderManagerByStaffId(final String nomsNumber, final Long staffId, final CreatePrisonOffenderManager prisonOffenderManager) {
        final var maybeStaff = staffService.findByStaffId(staffId);
        final var maybeOffender = offenderRepository.findByNomsNumber(nomsNumber);
        final var probationArea = probationAreaRepository.findByInstitutionByNomsCDECode(prisonOffenderManager.getNomsPrisonInstitutionCode())
                .orElseThrow(() -> new InvalidRequestException(String.format("Prison NOMS code %s not found", prisonOffenderManager.getNomsPrisonInstitutionCode())));

        return maybeStaff
                .flatMap(staff -> maybeOffender
                        .map(offender -> allocatePrisonOffenderManager(probationArea, staff, offender)));
    }

    @Transactional
    public Optional<CommunityOrPrisonOffenderManager> allocatePrisonOffenderManagerByName(final String nomsNumber, final CreatePrisonOffenderManager prisonOffenderManager) {

        final var maybeOffender = offenderRepository.findByNomsNumber(nomsNumber);
        final var probationArea = probationAreaRepository.findByInstitutionByNomsCDECode(prisonOffenderManager.getNomsPrisonInstitutionCode())
                .orElseThrow(() -> new InvalidRequestException(String.format("Prison NOMS code %s not found", prisonOffenderManager.getNomsPrisonInstitutionCode())));

        return maybeOffender.map(offender ->
                allocatePrisonOffenderManager(
                        probationArea,
                        staffService.findOrCreateStaffInArea(prisonOffenderManager.getOfficer(), probationArea),
                        offender));
    }


    boolean isPrisonOffenderManagerAtInstitution(final Offender offender, final RInstitution institution) {
        return offender.getPrisonOffenderManagers()
                .stream()
                .filter(PrisonOffenderManager::isActive)
                .findFirst()
                .flatMap(pom -> Optional.ofNullable(pom.getProbationArea()))
                .map(ProbationArea::getInstitution)
                .map(pomInstitution -> pomInstitution.getCode().equals(institution.getCode()))
                .orElse(false);
    }

    public CommunityOrPrisonOffenderManager autoAllocatePrisonOffenderManagerAtInstitution(final Offender offender, final RInstitution institution) {
        final var allocationReason = referenceDataService.pomAllocationAutoTransferReason();
        final var probationArea = probationAreaRepository.findByInstitutionByNomsCDECode(institution.getNomisCdeCode()).orElseThrow();
        final var team = teamService.findUnallocatedTeam(probationArea).orElseThrow();
        final var staff = staffService.findUnallocatedForTeam(team).orElseThrow();

        return allocatePrisonOffenderManager(probationArea, staff, offender, team, allocationReason);
    }

    private CommunityOrPrisonOffenderManager allocatePrisonOffenderManager(final ProbationArea probationArea, final Staff staff, final Offender offender) {
        return allocatePrisonOffenderManager(
                probationArea,
                staff,
                offender,
                teamService.findOrCreatePrisonOffenderManagerTeamInArea(probationArea),
                getAllocationReason(probationArea, findExistingPrisonOffenderManager(offender)));
    }

    private CommunityOrPrisonOffenderManager allocatePrisonOffenderManager(final ProbationArea probationArea, final Staff staff, final Offender offender, final Team team, final StandardReference allocationReason) {
        if (!probationArea.getProbationAreaId().equals(staff.getProbationArea().getProbationAreaId())) {
            throw new InvalidRequestException(
                    String.format("Staff with code %s is in probation area %s but was expected to be in prison area of %s", staff.getOfficerCode(), staff.getProbationArea().getDescription(), probationArea.getDescription()));
        }

        if (!isStaffInTeam(team, staff)) {
            teamService.addStaffToTeam(staff, team);
        }

        final var newPrisonOffenderManager = prisonOffenderManagerRepository.save(PrisonOffenderManager
                .builder()
                .probationArea(probationArea)
                .staff(staff)
                .team(team)
                .allocationReason(allocationReason)
                .managedOffender(offender)
                .offenderId(offender.getOffenderId())
                .build());

        // deactivate existing POM
        findExistingPrisonOffenderManager(offender).ifPresentOrElse(existingPOM -> {
            contactService.addContactForPOMAllocation(newPrisonOffenderManager, existingPOM);

            existingPOM.setEndDate(LocalDate.now());
            existingPOM.setActiveFlag(0L);

            Optional.ofNullable(existingPOM.getResponsibleOfficer())
                    .filter(ro -> ro.getEndDateTime() == null)
                    .ifPresent(activeRo -> {
                        // deactivate old RO and add a new one
                        activeRo.setEndDateTime(LocalDateTime.now());
                        newPrisonOffenderManager.setResponsibleOfficer(responsibleOfficerRepository.save(responsibleOfficerOf(offender, newPrisonOffenderManager)));
                        contactService.addContactForResponsibleOfficerChange(newPrisonOffenderManager, existingPOM);
                    });
        }, () -> contactService.addContactForPOMAllocation(newPrisonOffenderManager));


        return OffenderManagerTransformer.offenderManagerOf(newPrisonOffenderManager);
    }

    private StandardReference getAllocationReason(final ProbationArea probationArea, final Optional<PrisonOffenderManager> existingPrisonOffenderManager) {
        return existingPrisonOffenderManager
                .map(pom -> sameArea(probationArea, pom.getProbationArea())
                        ? referenceDataService.pomAllocationInternalTransferReason()
                        : referenceDataService.pomAllocationExternalTransferReason())
                .orElseGet(referenceDataService::pomAllocationAutoTransferReason);
    }

    private boolean sameArea(final ProbationArea newProbationArea, final ProbationArea oldProbationArea) {
        return newProbationArea.getCode().equals(oldProbationArea.getCode());
    }

    private Optional<PrisonOffenderManager> findExistingPrisonOffenderManager(final Offender offender) {
        return offender.getActivePrisonOffenderManager();
    }

    private boolean isStaffInTeam(final Team team, final Staff staff) {
        return staff
                .getTeams()
                .stream()
                .anyMatch(teamToMatch -> teamToMatch.getTeamId().equals(team.getTeamId()));
    }

    private static <T> List<T> combine(final List<T> first, final List<T> second) {
        return Stream.of(first, second)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deallocatePrisonerOffenderManager(final String nomsNumber) {
        final var offender = offenderRepository.findByNomsNumber(nomsNumber)
                .orElseThrow(() -> new NotFoundException(format("Offender %s not found", nomsNumber)));
        final var prisonerOffenderManager = findExistingPrisonOffenderManager(offender)
                .orElseThrow(() -> new ConflictingRequestException(format("Offender %s does not have a prisoner offender manager", nomsNumber)));

        // Nothing to do if the POM is already unallocated
        if (prisonerOffenderManager.getStaff().isUnallocated()) {
            return;
        }

        autoAllocatePrisonOffenderManagerAtInstitution(offender, prisonerOffenderManager.getProbationArea().getInstitution());
    }

    @Transactional
    public CommunityOrPrisonOffenderManager switchResponsibleOfficer(String nomsNumber, ResponsibleOfficerSwitch responsibleOfficerSwitch) {
        return offenderRepository.findByNomsNumber(nomsNumber).map(offender -> {
            if (responsibleOfficerSwitch.isSwitchToCommunityOffenderManager()) {
                switchResponsibleOfficeToCommunityOffenderManager(offender);
            } else {
                switchResponsibleOfficerToPrisonOffenderManager(offender);
            }
            return currentResponsibleOfficer(offender).orElseThrow();
        }).orElseThrow(() -> new NotFoundException(String.format("Offender with nomsNumber %s not found", nomsNumber)));
    }

    private void switchResponsibleOfficerToPrisonOffenderManager(Offender offender) {
        offender.getResponsibleOfficerWhoIsCommunityOffenderManager()
                .ifPresentOrElse(currentCOMResponsibleOfficer -> offender.getActivePrisonOffenderManager()
                        .ifPresentOrElse(prisonOffenderManager -> {
                            currentCOMResponsibleOfficer.getResponsibleOfficer().setEndDateTime(LocalDateTime.now());
                            prisonOffenderManager.setResponsibleOfficer(responsibleOfficerRepository
                                    .save(responsibleOfficerOf(offender, prisonOffenderManager)));
                            contactService
                                    .addContactForResponsibleOfficerChange(prisonOffenderManager, currentCOMResponsibleOfficer);
                        }, () -> {
                            throw new ConflictingRequestException(String
                                    .format("Cannot find an active POM for %s", offender
                                            .getNomsNumber()));
                        }),
                        () -> log.info(String.format("Current RO is not a COM for %s, so not doing anything", offender.getNomsNumber())));
    }

    private void switchResponsibleOfficeToCommunityOffenderManager(Offender offender) {
        offender.getResponsibleOfficerWhoIsPrisonOffenderManager()
                .ifPresentOrElse(currentPOMResponsibleOfficer -> offender.getActiveCommunityOffenderManager()
                                .ifPresentOrElse(communityOffenderManager -> {
                                    currentPOMResponsibleOfficer.getResponsibleOfficer()
                                            .setEndDateTime(LocalDateTime.now());
                                    communityOffenderManager.setResponsibleOfficer(responsibleOfficerRepository
                                            .save(responsibleOfficerOf(offender, communityOffenderManager)));
                                    contactService
                                            .addContactForResponsibleOfficerChange(communityOffenderManager, currentPOMResponsibleOfficer);
                                }, () -> {
                                    throw new ConflictingRequestException(String
                                            .format("Cannot find an active COM for %s", offender
                                                    .getNomsNumber()));
                                }),
                        () -> log.info(String.format("Current RO is not a POM for %s, so not doing anything", offender
                                .getNomsNumber())));
    }

    private ResponsibleOfficer responsibleOfficerOf(Offender offender, OffenderManager communityOffenderManager) {
        return ResponsibleOfficer
                .builder()
                .offenderId(offender.getOffenderId())
                .offenderManagerId(communityOffenderManager.getOffenderManagerId())
                .build();
    }

    private ResponsibleOfficer responsibleOfficerOf(Offender offender, PrisonOffenderManager prisonOffenderManager) {
        return ResponsibleOfficer
                .builder()
                .offenderId(offender.getOffenderId())
                .prisonOffenderManagerId(prisonOffenderManager.getPrisonOffenderManagerId())
                .build();
    }

    private Optional<CommunityOrPrisonOffenderManager> currentResponsibleOfficer(final Offender offender) {
        return getAllOffenderManagers(offender)
                .stream()
                .filter(CommunityOrPrisonOffenderManager::getIsResponsibleOfficer)
                .findAny();
    }

    private List<CommunityOrPrisonOffenderManager> getAllOffenderManagers(final Offender offender) {
        return combine(
                offender.getOffenderManagers()
                        .stream()
                        .filter(OffenderManager::isActive)
                        .map(OffenderManagerTransformer::offenderManagerOf)
                        .collect(Collectors.toList()),
                offender.getPrisonOffenderManagers()
                        .stream()
                        .filter(PrisonOffenderManager::isActive)
                        .map(OffenderManagerTransformer::offenderManagerOf)
                        .collect(Collectors.toList())
        );
    }

}
