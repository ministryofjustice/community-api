package uk.gov.justice.digital.delius.service;

import lombok.val;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.jpa.national.repository.UserRepository;
import uk.gov.justice.digital.delius.jpa.oracle.UserProxy;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;
import uk.gov.justice.digital.delius.jpa.standard.repository.*;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
public class LookupSupplier {
    public static final String COURT_APPEARANCE_OUTCOME_REF_DATASET = "COURT APPEARANCE OUTCOME";
    public static final String ORDER_ALLOCATION_REASON_REF_DATASET = "ORDER ALLOCATION REASON";
    private final OffenceRepository offenceRepository;
    private final UserRepository userRepository;
    private final StandardReferenceRepository standardReferenceRepository;
    private final CourtRepository courtRepository;
    private final ProbationAreaRepository probationAreaRepository;
    private final TeamRepository teamRepository;
    private final StaffRepository staffRepository;
    private final TransferReasonRepository transferReasonRepository;

    public static final String INITIAL_ORDER_ALLOCATION = "IN1";
    public static final String TRANSFER_CASE_INITIAL_REASON = "CASE ORDER";

    public LookupSupplier(OffenceRepository offenceRepository, UserRepository userRepository, StandardReferenceRepository standardReferenceRepository, CourtRepository courtRepository, ProbationAreaRepository probationAreaRepository, TeamRepository teamRepository, StaffRepository staffRepository, TransferReasonRepository transferReasonRepository) {
        this.offenceRepository = offenceRepository;
        this.userRepository = userRepository;
        this.standardReferenceRepository = standardReferenceRepository;
        this.courtRepository = courtRepository;
        this.probationAreaRepository = probationAreaRepository;
        this.teamRepository = teamRepository;
        this.staffRepository = staffRepository;
        this.transferReasonRepository = transferReasonRepository;
    }

    public Function<String, Offence> offenceSupplier() {
        return offenceCode -> offenceRepository.findByCode(offenceCode)
                .orElseThrow(() -> new RuntimeException(String.format("Offence not found for %s", offenceCode)));
    }

    public Supplier<User> userSupplier() {
        return () -> userRepository.findByDistinguishedNameIgnoreCase(UserProxy.username())
                .orElseThrow(() -> new RuntimeException(String.format("User in context %s not found", UserProxy.username())));
    }

    public Function<String, StandardReference> courtAppearanceOutcomeSupplier() {
        return code -> standardReferenceRepository.findByCodeAndCodeSetName(code, COURT_APPEARANCE_OUTCOME_REF_DATASET)
                .orElseThrow(() -> new RuntimeException(String.format("No outcome found for %s", code)));
    }

    public Function<String, TransferReason> transferReasonSupplier() {
        return code -> transferReasonRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException(String.format("No transfer reason found for %s", code)));
    }

    public Function<String, StandardReference> orderAllocationReasonSupplier() {
        return code -> standardReferenceRepository.findByCodeAndCodeSetName(code, ORDER_ALLOCATION_REASON_REF_DATASET)
                .orElseThrow(() -> new RuntimeException(String.format("No allocation reason found for %s", code)));
    }

    public Function<Long, Court> courtSupplier() {
        return courtId -> courtRepository.findById(courtId)
                .orElseThrow(() ->  new RuntimeException(String.format("No court found for %d", courtId)));
    }

    public Function<uk.gov.justice.digital.delius.data.api.OrderManager, ProbationArea> probationAreaSupplier() {
        return orderManager -> probationAreaRepository.findById(orderManager.getProbationAreaId())
                        .orElseThrow(() -> new RuntimeException(String.format("No probation area found for %s", orderManager.toString())));
    }
    public Function<uk.gov.justice.digital.delius.data.api.OrderManager, Team> teamSupplier() {
        return orderManager ->
                Optional.ofNullable(orderManager.getTeamId())
                        .map(this::findTeamById)
                        .orElseGet(() -> findUnallocatedTeam(orderManager.getProbationAreaId()));
    }

    private Team findTeamById(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException(String.format("No team found for %d", teamId)));
    }

    private Team findUnallocatedTeam(Long probationAreaId) {
        return teamRepository
                .findByCode(unallocatedTeamCodeForProbationArea(probationAreaId))
                .orElseThrow(() -> new RuntimeException(String.format("No unallocated team found for %d", probationAreaId)));
    }
    public Function<uk.gov.justice.digital.delius.data.api.OrderManager, Staff> staffSupplier() {
        return orderManager ->
                Optional.ofNullable(orderManager.getOfficerId())
                        .map(this::findStaffById                        )
                        .orElseGet(() -> findUnallocatedStaff(orderManager));
    }

    private Staff findUnallocatedStaff(uk.gov.justice.digital.delius.data.api.OrderManager orderManager) {
        return staffRepository
                .findByOfficerCode(unallocatedStaffCodeForProbationArea(orderManager))
                .orElseThrow(() -> new RuntimeException(String.format("No unallocated staff found for %s", orderManager)));
    }

    private Staff findStaffById(Long staffId) {
        return staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException(String.format("No staff found for %d", staffId)));
    }

    private String unallocatedTeamCodeForProbationArea(Long probationAreaId) {
        return probationAreaRepository.findById(probationAreaId)
                .map(probationArea -> String.format("%sUAT", probationArea.getCode())).orElseThrow(() -> new RuntimeException(String.format("No probation area found for %d", probationAreaId)));
    }

    private String unallocatedStaffCodeForProbationArea(uk.gov.justice.digital.delius.data.api.OrderManager orderManager) {
        val team = teamSupplier().apply(orderManager);

        return String.format("%sU", team.getCode());
    }
}
