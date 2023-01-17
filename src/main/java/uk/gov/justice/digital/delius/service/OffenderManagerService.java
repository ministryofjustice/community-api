package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.controller.ConflictingRequestException;
import uk.gov.justice.digital.delius.controller.InvalidRequestException;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.ContactableHuman;
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
import uk.gov.justice.digital.delius.jpa.standard.entity.User;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.PrisonOffenderManagerRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ProbationAreaRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ResponsibleOfficerRepository;
import uk.gov.justice.digital.delius.transformers.OffenderManagerTransformer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static uk.gov.justice.digital.delius.service.TeamService.UNALLOCATED_TEAM_SUFFIX;

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
    private final TelemetryClient telemetryClient;

    @Transactional(readOnly = true)
    public Optional<List<CommunityOrPrisonOffenderManager>> getAllOffenderManagersForNomsNumber(final String nomsNumber, final boolean includeProbationAreaTeams) {
        return offenderRepository.findByNomsNumber(nomsNumber).map(offender -> getAllOffenderManagers(offender, includeProbationAreaTeams));
    }

    @Transactional(readOnly = true)
    public Optional<List<CommunityOrPrisonOffenderManager>> getAllOffenderManagersForCrn(final String crn, final boolean includeProbationAreaTeams) {
        return offenderRepository.findByCrn(crn).map(offender -> getAllOffenderManagers(offender, includeProbationAreaTeams));
    }

    @Transactional
    public Optional<CommunityOrPrisonOffenderManager> allocatePrisonOffenderManagerByStaffId(final String nomsNumber, final Long staffId, final CreatePrisonOffenderManager prisonOffenderManager) {
        final var maybeStaff = staffService.findByStaffId(staffId);
        final var maybeOffender = offenderRepository.findMostLikelyByNomsNumber(nomsNumber).getOrElseThrow(e -> new ConflictingRequestException(e.getMessage()));
        final var probationArea = probationAreaRepository.findByInstitutionByNomsCDECode(prisonOffenderManager.getNomsPrisonInstitutionCode())
                .orElseThrow(() -> new InvalidRequestException(String.format("Prison NOMS code %s not found", prisonOffenderManager.getNomsPrisonInstitutionCode())));

        return maybeStaff
                .flatMap(staff -> maybeOffender
                        .map(offender -> allocatePrisonOffenderManager(probationArea, staff, offender, Optional.ofNullable(prisonOffenderManager.getOfficer()))));
    }

    @Transactional
    public Optional<CommunityOrPrisonOffenderManager> allocatePrisonOffenderManagerByName(final String nomsNumber, final CreatePrisonOffenderManager prisonOffenderManager) {

        final var maybeOffender = offenderRepository.findMostLikelyByNomsNumber(nomsNumber).getOrElseThrow(e -> new ConflictingRequestException(e.getMessage()));
        final var probationArea = probationAreaRepository.findByInstitutionByNomsCDECode(prisonOffenderManager.getNomsPrisonInstitutionCode())
                .orElseThrow(() -> new InvalidRequestException(String.format("Prison NOMS code %s not found", prisonOffenderManager.getNomsPrisonInstitutionCode())));

        return maybeOffender.map(offender ->
                allocatePrisonOffenderManager(
                        probationArea,
                        staffService.findOrCreateStaffInArea(prisonOffenderManager.getOfficer().capitalise(), probationArea),
                        offender, Optional.ofNullable(prisonOffenderManager.getOfficer())));
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
        final var probationArea = probationAreaRepository.findByInstitutionByNomsCDECode(institution.getNomisCdeCode())
            .orElseThrow(() -> new NotFoundException("institution with CdeCode: " + institution.getNomisCdeCode()));
        final var team = teamService.findUnallocatedTeam(probationArea)
            .orElseThrow(() -> new NotFoundException("Team Not Found with code: " + probationArea.getCode() + UNALLOCATED_TEAM_SUFFIX));
        final var staff = staffService.findUnallocatedForTeam(team)
            .orElseThrow(() -> new NotFoundException("Staff Not Found with code: " + team.getCode() + "U"));

        return allocatePrisonOffenderManager(probationArea, staff, offender, team, allocationReason, empty());
    }

    private CommunityOrPrisonOffenderManager allocatePrisonOffenderManager(final ProbationArea probationArea, final Staff staff, final Offender offender, final Optional<ContactableHuman> officer) {
        return allocatePrisonOffenderManager(
                probationArea,
                staff,
                offender,
                teamService.findOrCreatePrisonOffenderManagerTeamInArea(probationArea),
                getAllocationReason(probationArea, findExistingPrisonOffenderManager(offender)),
                officer);
    }

    private CommunityOrPrisonOffenderManager allocatePrisonOffenderManager(final ProbationArea probationArea, final Staff staff, final Offender offender, final Team team, final StandardReference allocationReason, final Optional<ContactableHuman> officer) {
        if (!probationArea.getProbationAreaId().equals(staff.getProbationArea().getProbationAreaId())) {
            throw new InvalidRequestException(
                    String.format("Staff with code %s is in probation area %s but was expected to be in prison area of %s", staff.getOfficerCode(), staff.getProbationArea().getDescription(), probationArea.getDescription()));
        }
        final var telemetryProperties = Map.of("probationArea", probationArea.getCode(),
                "staffCode", staff.getOfficerCode(),
                "crn", offender.getCrn());


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
                .emailAddress(officer.map(ContactableHuman::getEmail).orElse(null))
                .telephoneNumber(officer.map(ContactableHuman::getPhoneNumber).orElse(null))
                .build());


        // deactivate existing POM
        findExistingPrisonOffenderManager(offender).ifPresentOrElse(existingPOM -> {
            contactService.addContactForPOMAllocation(newPrisonOffenderManager, existingPOM);

            existingPOM.setEndDate(LocalDate.now());
            existingPOM.setActiveFlag(0L);

            Optional.ofNullable(existingPOM.getActiveResponsibleOfficer())
                    .ifPresent(activeRo -> {
                        // deactivate old RO and add a new one
                        activeRo.setEndDateTime(LocalDateTime.now());
                        responsibleOfficerRepository.saveAndFlush(activeRo);
                        newPrisonOffenderManager.addResponsibleOfficer(responsibleOfficerRepository.save(responsibleOfficerOf(offender, newPrisonOffenderManager)));
                        contactService.addContactForResponsibleOfficerChange(newPrisonOffenderManager, existingPOM);
                        telemetryClient.trackEvent("POMResponsibleOfficerSet", telemetryProperties, null);
                    });
            offender.getPrisonOffenderManagers().remove(existingPOM);
        }, () -> contactService.addContactForPOMAllocation(newPrisonOffenderManager));

        offender.getPrisonOffenderManagers().add(newPrisonOffenderManager);
        telemetryClient.trackEvent("POMAllocated", telemetryProperties, null);

        return OffenderManagerTransformer.offenderManagerOf(newPrisonOffenderManager, true);
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
        final var offender = offenderRepository.findMostLikelyByNomsNumber(nomsNumber)
            .getOrElseThrow((e) -> e)
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
            currentResponsibleOfficer(offender).orElseThrow(() -> {
                throw new ConflictingRequestException(String.format("Cannot find a current RO for %s", offender.getNomsNumber()));
            });
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
                            currentCOMResponsibleOfficer.getActiveResponsibleOfficer().makeInactive();
                            prisonOffenderManager.addResponsibleOfficer(responsibleOfficerRepository
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
                                    currentPOMResponsibleOfficer.getActiveResponsibleOfficer().makeInactive();
                                    communityOffenderManager.addResponsibleOfficer(responsibleOfficerRepository
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
        return getAllOffenderManagers(offender, true);
    }

    private List<CommunityOrPrisonOffenderManager> getAllOffenderManagers(final Offender offender, final boolean includeProbationAreaTeams) {
        return combine(
                    offender.getOffenderManagers()
                        .stream()
                        .filter(OffenderManager::isActive)
                        .map(this::addLdapFields)
                        .map(offMgr -> OffenderManagerTransformer.offenderManagerOf(offMgr, includeProbationAreaTeams))
                        .collect(Collectors.toList()),
                offender.getPrisonOffenderManagers()
                        .stream()
                        .filter(PrisonOffenderManager::isActive)
                        .map(offMgr -> OffenderManagerTransformer.offenderManagerOf(offMgr, includeProbationAreaTeams))
                        .collect(Collectors.toList())
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
