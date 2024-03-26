package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.jpa.standard.entity.PrisonOffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.ResponsibleOfficer;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.PrisonOffenderManagerRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ProbationAreaRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ResponsibleOfficerRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.*;

@ExtendWith(MockitoExtension.class)
public class OffenderManagerService_autoAllocatePrisonOffenderManagerAtInstitutionTest {
    @Mock
    private OffenderRepository offenderRepository;
    @Mock
    private ProbationAreaRepository probationAreaRepository;
    @Mock
    private PrisonOffenderManagerRepository prisonOffenderManagerRepository;
    @Mock
    private ResponsibleOfficerRepository responsibleOfficerRepository;
    @Mock
    private StaffService staffService;
    @Mock
    private TeamService teamService;
    @Mock
    private ReferenceDataService referenceDataService;
    @Mock
    private ContactService contactService;
    @Mock
    private TelemetryClient telemetryClient;
    @Captor
    private ArgumentCaptor<PrisonOffenderManager> prisonOffenderManagerArgumentCaptor;
    @Captor
    private ArgumentCaptor<ResponsibleOfficer> responsibleOfficerArgumentCaptor;

    private OffenderManagerService offenderManagerService;

    @BeforeEach
    public void setup() {
        offenderManagerService = new OffenderManagerService(
                offenderRepository,
                probationAreaRepository,
                prisonOffenderManagerRepository,
                responsibleOfficerRepository,
                staffService,
                teamService,
                referenceDataService,
                contactService,
                telemetryClient);

        when(probationAreaRepository.findByInstitutionInstitutionId(any())).thenAnswer(args -> {
            var code = args.getArgument(0).toString();
            return Optional.of(aPrisonProbationArea()
                    .toBuilder()
                    .code(code)
                    .build());
        });
        when(prisonOffenderManagerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
        when(teamService.findUnallocatedTeam(any())).thenReturn(Optional.of(aTeam()));
        when(staffService.findUnallocatedForTeam(any())).thenReturn(Optional.of(aStaff()));
    }


    @Test
    public void allocationReasonIsAutomaticTransfer() {
        when(responsibleOfficerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
        when(referenceDataService.pomAllocationAutoTransferReason()).thenReturn(StandardReference
                .builder()
                .codeValue("AUT")
                .build());


        offenderManagerService.autoAllocatePrisonOffenderManagerAtInstitution(anOffender(), aPrisonInstitution());


        verify(referenceDataService).pomAllocationAutoTransferReason();
        verify(prisonOffenderManagerRepository).save(prisonOffenderManagerArgumentCaptor.capture());
        assertThat(prisonOffenderManagerArgumentCaptor.getValue().getAllocationReason().getCodeValue()).isEqualTo("AUT");
    }

    @Test
    public void unallocatedTeamWillBeUsed() {
        when(responsibleOfficerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
        when(teamService.findUnallocatedTeam(any())).thenReturn(Optional.of(aTeam("NO2ALL")));

        offenderManagerService.autoAllocatePrisonOffenderManagerAtInstitution(anOffender(), aPrisonInstitution());

        verify(prisonOffenderManagerRepository).save(prisonOffenderManagerArgumentCaptor.capture());
        assertThat(prisonOffenderManagerArgumentCaptor.getValue().getTeam().getCode()).isEqualTo("NO2ALL");
    }

    @Test
    public void unallocatedStaffInTeamWillBeUsed() {
        final var team = aTeam("NO2ALL");
        when(teamService.findUnallocatedTeam(any())).thenReturn(Optional.of(team));
        when(staffService.findUnallocatedForTeam(any())).thenReturn(Optional.of(aStaff("NO2ALLU")));
        when(responsibleOfficerRepository.save(any())).thenAnswer(args -> args.getArgument(0));

        offenderManagerService.autoAllocatePrisonOffenderManagerAtInstitution(anOffender(), aPrisonInstitution());

        verify(prisonOffenderManagerRepository).save(prisonOffenderManagerArgumentCaptor.capture());
        verify(staffService).findUnallocatedForTeam(team);
        assertThat(prisonOffenderManagerArgumentCaptor.getValue().getStaff().getOfficerCode()).isEqualTo("NO2ALLU");
    }


    @Test
    public void existingPrisonerOffenderManagerIsDeactivated() {
        var existingPOM  = anActivePrisonOffenderManager();
        var offender = anOffender(new ArrayList<>(), new ArrayList<>(List.of(existingPOM)));

        when(responsibleOfficerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
        offenderManagerService.autoAllocatePrisonOffenderManagerAtInstitution(offender, aPrisonInstitution());

        assertThat(existingPOM.getEndDate()).isNotNull();
        assertThat(existingPOM.getActiveFlag()).isZero();
    }

    @Test
    public void existingResponsibleOfficerIsDeactivatedWhenActivePOM() {
        final ResponsibleOfficer existingPOMResponsibleOfficer = aResponsibleOfficer()
                .toBuilder()
                .endDateTime(null)
                .build();
        var existingPOM  = anActivePrisonOffenderManager()
                .toBuilder()
                .responsibleOfficers(List.of(existingPOMResponsibleOfficer))
                .build();
        var offender = anOffender(new ArrayList<>(), new ArrayList<>(List.of(existingPOM)));

        when(responsibleOfficerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
        offenderManagerService.autoAllocatePrisonOffenderManagerAtInstitution(offender, aPrisonInstitution());

        assertThat(existingPOMResponsibleOfficer.getEndDateTime()).isNotNull();
    }

    @Test
    public void newPrisonerOffenderManagerIsMadeResponsibleOfficerWhenExistingPOMIsAlsoCurrentRO() {
        final ResponsibleOfficer existingPOMResponsibleOfficer = aResponsibleOfficer()
                .toBuilder()
                .endDateTime(null)
                .build();
        var existingPOM  = anActivePrisonOffenderManager()
                .toBuilder()
                .responsibleOfficers(List.of(existingPOMResponsibleOfficer))
                .build();

        var offender = anOffender(new ArrayList<>(), new ArrayList<>(List.of(existingPOM)));

        when(prisonOffenderManagerRepository.save(any())).thenAnswer(args -> {
            final PrisonOffenderManager newPOM = args.getArgument(0);
            newPOM.setPrisonOffenderManagerId(99L);
            return newPOM;
        });
        when(responsibleOfficerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
        var newPrisonOffenderManager = offenderManagerService.autoAllocatePrisonOffenderManagerAtInstitution(offender, aPrisonInstitution());

        verify(responsibleOfficerRepository).save(responsibleOfficerArgumentCaptor.capture());
        assertThat(responsibleOfficerArgumentCaptor.getValue().getPrisonOffenderManagerId()).isEqualTo(99L);
        assertThat(newPrisonOffenderManager.getIsResponsibleOfficer()).isTrue();
    }

    @Test
    public void shouldAddAPOMAllocationContact() {
        offenderManagerService.autoAllocatePrisonOffenderManagerAtInstitution(anOffender(new ArrayList<>(), new ArrayList<>()), aPrisonInstitution());

        verify(contactService).addContactForPOMAllocation(isA(PrisonOffenderManager.class));
    }

    @Test
    public void shouldAddAPOMAllocationContactNotingOldPOM() {
        when(responsibleOfficerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
        final var existingPOM = anActivePrisonOffenderManager();

        var offender = anOffender(new ArrayList<>(), new ArrayList<>(List.of(existingPOM)));

        offenderManagerService.autoAllocatePrisonOffenderManagerAtInstitution(offender, aPrisonInstitution());

        verify(contactService).addContactForPOMAllocation(isA(PrisonOffenderManager.class), eq(existingPOM));
    }

    @Test
    public void shouldAddAResponsibleOfficeMoveContactNotingOldPOM() {
        final ResponsibleOfficer existingPOMResponsibleOfficer = aResponsibleOfficer()
                .toBuilder()
                .endDateTime(null)
                .build();
        var existingPOM  = anActivePrisonOffenderManager()
                .toBuilder()
                .responsibleOfficers(List.of(existingPOMResponsibleOfficer))
                .build();

        when(prisonOffenderManagerRepository.save(any())).thenAnswer(args -> {
            final PrisonOffenderManager newPOM = args.getArgument(0);
            newPOM.setPrisonOffenderManagerId(99L);
            return newPOM;
        });

        var offender = anOffender(new ArrayList<>(), new ArrayList<>(List.of(existingPOM)));

        when(responsibleOfficerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
        offenderManagerService.autoAllocatePrisonOffenderManagerAtInstitution(offender, aPrisonInstitution());

        verify(contactService).addContactForResponsibleOfficerChange(isA(PrisonOffenderManager.class), eq(existingPOM));
    }

}