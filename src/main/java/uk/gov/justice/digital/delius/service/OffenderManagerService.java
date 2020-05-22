package uk.gov.justice.digital.delius.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.controller.InvalidRequestException;
import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.CreatePrisonOffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;
import uk.gov.justice.digital.delius.jpa.standard.repository.*;
import uk.gov.justice.digital.delius.transformers.OffenderManagerTransformer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class OffenderManagerService {

    private final OffenderRepository offenderRepository;
    private final OffenderManagerTransformer offenderManagerTransformer;
    private final ProbationAreaRepository probationAreaRepository;
    private final PrisonOffenderManagerRepository prisonOffenderManagerRepository;
    private final ResponsibleOfficerRepository responsibleOfficerRepository;
    private final StaffService staffService;
    private final TeamService teamService;
    private final ReferenceDataService referenceDataService;
    private final ContactService contactService;




    @Autowired
    public OffenderManagerService(OffenderRepository offenderRepository, OffenderManagerTransformer offenderManagerTransformer, ProbationAreaRepository probationAreaRepository, PrisonOffenderManagerRepository prisonOffenderManagerRepository, ResponsibleOfficerRepository responsibleOfficerRepository, StaffService staffService, TeamService teamService, ReferenceDataService referenceDataService, ContactService contactService) {
        this.offenderRepository = offenderRepository;
        this.offenderManagerTransformer = offenderManagerTransformer;
        this.probationAreaRepository = probationAreaRepository;
        this.prisonOffenderManagerRepository = prisonOffenderManagerRepository;
        this.responsibleOfficerRepository = responsibleOfficerRepository;
        this.staffService = staffService;
        this.teamService = teamService;
        this.referenceDataService = referenceDataService;
        this.contactService = contactService;
    }
    @Transactional(readOnly = true)
    public Optional<List<CommunityOrPrisonOffenderManager>> getAllOffenderManagersForNomsNumber(String nomsNumber) {
        return offenderRepository.findByNomsNumber(nomsNumber).map(
                offender -> combine(
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
                ) );
    }

    @Transactional
    public Optional<CommunityOrPrisonOffenderManager> allocatePrisonOffenderManagerByStaffCode(String nomsNumber, String staffCode, CreatePrisonOffenderManager prisonOffenderManager) {
        final var maybeStaff = staffService.findByOfficerCode(staffCode);
        final var maybeOffender = offenderRepository.findByNomsNumber(nomsNumber);
        final var probationArea = probationAreaRepository.findByInstitutionByNomsCDECode(prisonOffenderManager.getNomsPrisonInstitutionCode())
                .orElseThrow(() -> new InvalidRequestException(String.format("Prison NOMS code %s not found", prisonOffenderManager.getNomsPrisonInstitutionCode())));

        return maybeStaff
                .flatMap(staff -> maybeOffender
                        .map(offender -> allocatePrisonOffenderManager(probationArea, staff, offender)));
    }

    @Transactional
    public Optional<CommunityOrPrisonOffenderManager> allocatePrisonOffenderManagerByName(String nomsNumber, CreatePrisonOffenderManager prisonOffenderManager) {

        final var maybeOffender = offenderRepository.findByNomsNumber(nomsNumber);
        final var probationArea = probationAreaRepository.findByInstitutionByNomsCDECode(prisonOffenderManager.getNomsPrisonInstitutionCode())
                .orElseThrow(() -> new InvalidRequestException(String.format("Prison NOMS code %s not found", prisonOffenderManager.getNomsPrisonInstitutionCode())));

         return maybeOffender.map(offender ->
                    allocatePrisonOffenderManager(
                            probationArea,
                            staffService.findOrCreateStaffInArea(prisonOffenderManager.getOfficer(), probationArea),
                            offender));
    }


    boolean isPrisonOffenderManagerAtInstitution(Offender offender, RInstitution institution) {
        return offender.getPrisonOffenderManagers()
                .stream()
                .filter(PrisonOffenderManager::isActive)
                .findFirst()
                .flatMap(pom -> Optional.ofNullable(pom.getProbationArea()))
                .map(ProbationArea::getInstitution)
                .map(pomInstitution -> pomInstitution.getCode().equals(institution.getCode()))
                .orElse(false);
    }

    CommunityOrPrisonOffenderManager autoAllocatePrisonOffenderManagerAtInstitution(Offender offender, RInstitution institution) {
        final var allocationReason = referenceDataService.pomAllocationAutoTransferReason();
        final var probationArea = probationAreaRepository.findByInstitutionByNomsCDECode(institution.getNomisCdeCode()).orElseThrow();
        final var team = teamService.findUnallocatedTeam(probationArea).orElseThrow();
        final var staff = staffService.findUnallocatedForTeam(team).orElseThrow();

        return allocatePrisonOffenderManager(probationArea, staff, offender, team, allocationReason);
    }

    private CommunityOrPrisonOffenderManager allocatePrisonOffenderManager(ProbationArea probationArea, Staff staff, Offender offender) {
        return allocatePrisonOffenderManager(
                probationArea,
                staff,
                offender,
                teamService.findOrCreatePrisonOffenderManagerTeamInArea(probationArea),
                getAllocationReason(probationArea, findExistingPrisonOffenderManager(offender)));
    }

    private CommunityOrPrisonOffenderManager allocatePrisonOffenderManager(ProbationArea probationArea, Staff staff, Offender offender, Team team, StandardReference allocationReason) {
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
                        newPrisonOffenderManager.setResponsibleOfficer(responsibleOfficerRepository.save(
                                ResponsibleOfficer
                                        .builder()
                                        .offenderId(offender.getOffenderId())
                                        .prisonOffenderManagerId(newPrisonOffenderManager.getPrisonOffenderManagerId())
                                        .build()
                        ));
                        contactService.addContactForResponsibleOfficerChange(newPrisonOffenderManager, existingPOM);
                    });
        }, () -> contactService.addContactForPOMAllocation(newPrisonOffenderManager));


        return OffenderManagerTransformer.offenderManagerOf(newPrisonOffenderManager);
    }

    private StandardReference getAllocationReason(ProbationArea probationArea, Optional<PrisonOffenderManager> existingPrisonOffenderManager) {
        return existingPrisonOffenderManager
                .map(pom -> sameArea(probationArea, pom.getProbationArea())
                        ? referenceDataService.pomAllocationInternalTransferReason()
                        : referenceDataService.pomAllocationExternalTransferReason())
                .orElseGet(referenceDataService::pomAllocationAutoTransferReason);
    }

    private boolean sameArea(ProbationArea newProbationArea, ProbationArea oldProbationArea) {
        return newProbationArea.getCode().equals(oldProbationArea.getCode());
    }

    private Optional<PrisonOffenderManager> findExistingPrisonOffenderManager(Offender offender) {
        return offender.getPrisonOffenderManagers()
                .stream()
                .filter(PrisonOffenderManager::isActive)
                .findFirst();
    }

    private boolean isStaffInTeam(Team team, Staff staff) {
        return staff
                .getTeams()
                .stream()
                .anyMatch(teamToMatch -> teamToMatch.getTeamId().equals(team.getTeamId()));
    }

    private static <T> List<T> combine(List<T> first, List<T> second) {
        return Stream.of(first, second)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
