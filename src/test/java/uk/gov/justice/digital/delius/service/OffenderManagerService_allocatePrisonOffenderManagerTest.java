package uk.gov.justice.digital.delius.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.controller.InvalidRequestException;
import uk.gov.justice.digital.delius.data.api.CreatePrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.Human;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.*;

@RunWith(MockitoJUnitRunner.class)
public class OffenderManagerService_allocatePrisonOffenderManagerTest {
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
    @Captor
    private ArgumentCaptor<PrisonOffenderManager> prisonOffenderManagerArgumentCaptor;
    @Captor
    private ArgumentCaptor<ResponsibleOfficer> responsibleOfficerArgumentCaptor;

    private OffenderManagerService offenderManagerService;

    @Before
    public void setup() {
        offenderManagerService = new OffenderManagerService(
                offenderRepository,
                probationAreaRepository,
                prisonOffenderManagerRepository,
                responsibleOfficerRepository,
                staffService,
                teamService,
                referenceDataService,
                contactService);

        when(offenderRepository.findByNomsNumber(any())).thenReturn(Optional.of(anOffender()));
        when(staffService.findByOfficerCode(any())).thenReturn(Optional.of(aStaff()));
        when(probationAreaRepository.findByInstitutionByNomsCDECode(any())).thenAnswer(args -> {
            var code = args.getArgument(0).toString();
            return Optional.of(aPrisonProbationArea()
                    .toBuilder()
                    .code(code)
                    .build());
        });
        when(prisonOffenderManagerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
        when(responsibleOfficerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
        when(teamService.findOrCreatePrisonOffenderManagerTeamInArea(any())).thenReturn(aTeam());

    }

    @Test
    public void willReturnEmptyWhenOffenderNotFound() {
        when(offenderRepository.findByNomsNumber("G9542VP")).thenReturn(Optional.empty());
        when(staffService.findByOfficerCode("N01A12345")).thenReturn(Optional.of(aStaff()));
        when(probationAreaRepository.findByInstitutionByNomsCDECode("BWI")).thenReturn(Optional.of(aProbationArea()));

        assertThat(offenderManagerService.allocatePrisonOffenderManagerByStaffCode(
                "G9542VP",
                "N01A12345",
                CreatePrisonOffenderManager
                        .builder()
                        .nomsPrisonInstitutionCode("BWI")
                        .build()))
                .isNotPresent();
        assertThat(offenderManagerService.allocatePrisonOffenderManagerByName(
                "G9542VP",
                CreatePrisonOffenderManager
                        .builder()
                        .nomsPrisonInstitutionCode("BWI")
                        .officer(Human
                                .builder()
                                .build())
                        .build()))
                .isNotPresent();
    }

    @Test
    public void willReturnEmptyWhenStaffNotFound() {
        when(offenderRepository.findByNomsNumber("G9542VP")).thenReturn(Optional.of(anOffender()));
        when(staffService.findByOfficerCode("N01A12345")).thenReturn(Optional.empty());
        when(probationAreaRepository.findByInstitutionByNomsCDECode("BWI")).thenReturn(Optional.of(aProbationArea()));

        assertThat(offenderManagerService.allocatePrisonOffenderManagerByStaffCode(
                "G9542VP",
                "N01A12345",
                CreatePrisonOffenderManager
                        .builder()
                        .nomsPrisonInstitutionCode("BWI")
                        .build()))
                .isNotPresent();
    }

    @Test
    public void willThrowInvalidRequestWhenPrisonProbationAreaNotFound() {
        when(offenderRepository.findByNomsNumber("G9542VP")).thenReturn(Optional.of(anOffender()));
        when(staffService.findByOfficerCode("N01A12345")).thenReturn(Optional.of(aStaff()));
        when(probationAreaRepository.findByInstitutionByNomsCDECode("BWI")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> offenderManagerService.allocatePrisonOffenderManagerByStaffCode(
                "G9542VP",
                "N01A12345",
                CreatePrisonOffenderManager
                        .builder()
                        .nomsPrisonInstitutionCode("BWI")
                        .build()))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Prison NOMS code BWI not found");

        assertThatThrownBy(() -> offenderManagerService.allocatePrisonOffenderManagerByName(
                "G9542VP",
                CreatePrisonOffenderManager
                        .builder()
                        .nomsPrisonInstitutionCode("BWI")
                        .officer(Human
                                .builder()
                                .build())
                        .build()))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Prison NOMS code BWI not found");
    }

    @Test
    public void willNotAllowAllocationOfStaffInDifferentProbationArea() {
        when(offenderRepository.findByNomsNumber("G9542VP")).thenReturn(Optional.of(anOffender()));
        when(staffService.findByOfficerCode("N01A12345")).thenReturn(Optional.of(aStaff()
                .toBuilder()
                .probationArea(
                        aProbationArea()
                        .toBuilder()
                        .code("BWI")
                        .probationAreaId(99L)
                        .build()
                )
                .build())
        );
        when(probationAreaRepository.findByInstitutionByNomsCDECode("N01"))
                .thenReturn(Optional.of(
                        aProbationArea()
                                .toBuilder()
                                .code("N01")
                                .probationAreaId(88L)
                                .build()
                ));

        assertThatThrownBy(() -> offenderManagerService.allocatePrisonOffenderManagerByStaffCode(
                "G9542VP",
                "N01A12345",
                CreatePrisonOffenderManager
                        .builder()
                        .nomsPrisonInstitutionCode("N01")
                        .build()))
                .isInstanceOf(InvalidRequestException.class);

    }
    @Test
    public void shouldAddStaffToTeamIfNotMemberAlready() {
        final var staff = aStaff()
                .toBuilder()
                .teams(new ArrayList<>())
                .build();
        when(staffService.findByOfficerCode("N01A12345")).thenReturn(Optional.of(
                staff
        ));

        final var team = aTeam()
                .toBuilder()
                .teamId(99L)
                .build();
        when(teamService.findOrCreatePrisonOffenderManagerTeamInArea(any())).thenReturn(
                team);

        offenderManagerService.allocatePrisonOffenderManagerByStaffCode(
                "G9542VP",
                "N01A12345",
                CreatePrisonOffenderManager
                        .builder()
                        .nomsPrisonInstitutionCode("N01")
                        .build());

        verify(teamService).addStaffToTeam(staff, team);

    }

    @Test
    public void allocationReasonIsAutomaticTransferWhenExistingPOMIsNotPresent() {
        when(referenceDataService.pomAllocationAutoTransferReason()).thenReturn(StandardReference
                .builder()
                .codeValue("AUT")
                .build());

        when(offenderRepository.findByNomsNumber(any())).thenReturn(Optional.of(
                anOffender()
                        .toBuilder()
                        .prisonOffenderManagers(List.of())
                        .build()
        ));


        offenderManagerService.allocatePrisonOffenderManagerByStaffCode(
                "G9542VP",
                "N01A12345",
                CreatePrisonOffenderManager
                        .builder()
                        .nomsPrisonInstitutionCode("N01")
                        .build());


        verify(prisonOffenderManagerRepository).save(prisonOffenderManagerArgumentCaptor.capture());
        assertThat(prisonOffenderManagerArgumentCaptor.getValue().getAllocationReason().getCodeValue()).isEqualTo("AUT");
    }

    @Test
    public void allocationReasonIsInternalTransferWhenExistingAndNewPOMInSameArea() {
        when(referenceDataService.pomAllocationInternalTransferReason()).thenReturn(StandardReference
                .builder()
                .codeValue("INA")
                .build());


        var existingPOM  = anActivePrisonOffenderManager()
                .toBuilder()
                .probationArea(aPrisonProbationArea()
                        .toBuilder()
                        .code("BWI")
                        .build())
                .build();

        when(offenderRepository.findByNomsNumber(any())).thenReturn(Optional.of(
                anOffender()
                        .toBuilder()
                        .prisonOffenderManagers(List.of(existingPOM))
                        .build()
        ));

        offenderManagerService.allocatePrisonOffenderManagerByStaffCode(
                "G9542VP",
                "N01A12345",
                CreatePrisonOffenderManager
                        .builder()
                        .nomsPrisonInstitutionCode("BWI")
                        .build());


        verify(prisonOffenderManagerRepository).save(prisonOffenderManagerArgumentCaptor.capture());
        assertThat(prisonOffenderManagerArgumentCaptor.getValue().getAllocationReason().getCodeValue()).isEqualTo("INA");
    }

    @Test
    public void allocationReasonIsExternalTransferWhenExistingAndNewPOMDifferentSameArea() {
        when(referenceDataService.pomAllocationExternalTransferReason()).thenReturn(StandardReference
                .builder()
                .codeValue("EXT")
                .build());


        var existingPOM  = anActivePrisonOffenderManager()
                .toBuilder()
                .probationArea(aPrisonProbationArea()
                        .toBuilder()
                        .code("BWI")
                        .build())
                .build();

        when(offenderRepository.findByNomsNumber(any())).thenReturn(Optional.of(
                anOffender()
                        .toBuilder()
                        .prisonOffenderManagers(List.of(existingPOM))
                        .build()
        ));

        offenderManagerService.allocatePrisonOffenderManagerByStaffCode(
                "G9542VP",
                "N01A12345",
                CreatePrisonOffenderManager
                        .builder()
                        .nomsPrisonInstitutionCode("BRI")
                        .build());


        verify(prisonOffenderManagerRepository).save(prisonOffenderManagerArgumentCaptor.capture());
        assertThat(prisonOffenderManagerArgumentCaptor.getValue().getAllocationReason().getCodeValue()).isEqualTo("EXT");
    }



    @Test
    public void existingPrisonerOffenderManagerIsDeactivated() {
        var existingPOM  = anActivePrisonOffenderManager();

        when(offenderRepository.findByNomsNumber(any())).thenReturn(Optional.of(
                anOffender()
                .toBuilder()
                .prisonOffenderManagers(List.of(existingPOM))
                .build()
        ));

        offenderManagerService.allocatePrisonOffenderManagerByStaffCode(
                "G9542VP",
                "N01A12345",
                CreatePrisonOffenderManager
                        .builder()
                        .nomsPrisonInstitutionCode("N01")
                        .build());


        assertThat(existingPOM.getEndDate()).isNotNull();
        assertThat(existingPOM.getActiveFlag()).isEqualTo(0L);
    }

    @Test
    public void existingResponsibleOfficerIsDeactivatedWhenActivePOM() {
        final ResponsibleOfficer existingPOMResponsibleOfficer = aResponsibleOfficer()
                .toBuilder()
                .endDateTime(null)
                .build();
        var existingPOM  = anActivePrisonOffenderManager()
                .toBuilder()
                .responsibleOfficer(existingPOMResponsibleOfficer)
                .build();

        when(offenderRepository.findByNomsNumber(any())).thenReturn(Optional.of(
                anOffender()
                .toBuilder()
                .prisonOffenderManagers(List.of(existingPOM))
                .build()
        ));

        offenderManagerService.allocatePrisonOffenderManagerByStaffCode(
                "G9542VP",
                "N01A12345",
                CreatePrisonOffenderManager
                        .builder()
                        .nomsPrisonInstitutionCode("N01")
                        .build());


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
                .responsibleOfficer(existingPOMResponsibleOfficer)
                .build();

        when(offenderRepository.findByNomsNumber(any())).thenReturn(Optional.of(
                anOffender()
                .toBuilder()
                .prisonOffenderManagers(List.of(existingPOM))
                .build()
        ));

        when(prisonOffenderManagerRepository.save(any())).thenAnswer(args -> {
            final PrisonOffenderManager newPOM = args.getArgument(0);
            newPOM.setPrisonOffenderManagerId(99L);
            return newPOM;
        });

        var newPrisonOffenderManager = offenderManagerService.allocatePrisonOffenderManagerByStaffCode(
                "G9542VP",
                "N01A12345",
                CreatePrisonOffenderManager
                        .builder()
                        .nomsPrisonInstitutionCode("N01")
                        .build());


        verify(responsibleOfficerRepository).save(responsibleOfficerArgumentCaptor.capture());
        assertThat(responsibleOfficerArgumentCaptor.getValue().getPrisonOffenderManagerId()).isEqualTo(99L);
        assertThat(newPrisonOffenderManager.orElseThrow().getIsResponsibleOfficer()).isTrue();
    }

    @Test
    public void shouldAddAPOMAllocationContact() {
        when(offenderRepository.findByNomsNumber(any())).thenReturn(Optional.of(
                anOffender()
                        .toBuilder()
                        .prisonOffenderManagers(List.of())
                        .build()));

        offenderManagerService.allocatePrisonOffenderManagerByStaffCode(
                "G9542VP",
                "N01A12345",
                CreatePrisonOffenderManager
                        .builder()
                        .nomsPrisonInstitutionCode("N01")
                        .build());


        verify(contactService).addContactForPOMAllocation(isA(PrisonOffenderManager.class));
    }

    @Test
    public void shouldAddAPOMAllocationContactNotingOldPOM() {
        final var existingPOM = anActivePrisonOffenderManager();
        when(offenderRepository.findByNomsNumber(any())).thenReturn(Optional.of(
                anOffender()
                        .toBuilder()
                        .prisonOffenderManagers(List.of(existingPOM))
                        .build()));

        offenderManagerService.allocatePrisonOffenderManagerByStaffCode(
                "G9542VP",
                "N01A12345",
                CreatePrisonOffenderManager
                        .builder()
                        .nomsPrisonInstitutionCode("N01")
                        .build());


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
                .responsibleOfficer(existingPOMResponsibleOfficer)
                .build();

        when(offenderRepository.findByNomsNumber(any())).thenReturn(Optional.of(
                anOffender()
                        .toBuilder()
                        .prisonOffenderManagers(List.of(existingPOM))
                        .build()
        ));

        when(prisonOffenderManagerRepository.save(any())).thenAnswer(args -> {
            final PrisonOffenderManager newPOM = args.getArgument(0);
            newPOM.setPrisonOffenderManagerId(99L);
            return newPOM;
        });

        offenderManagerService.allocatePrisonOffenderManagerByStaffCode(
                "G9542VP",
                "N01A12345",
                CreatePrisonOffenderManager
                        .builder()
                        .nomsPrisonInstitutionCode("N01")
                        .build());

        verify(contactService).addContactForResponsibleOfficerChange(isA(PrisonOffenderManager.class), eq(existingPOM));
    }

}