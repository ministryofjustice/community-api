package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.justice.digital.delius.controller.ConflictingRequestException;
import uk.gov.justice.digital.delius.controller.InvalidRequestException;
import uk.gov.justice.digital.delius.data.api.ContactableHuman;
import uk.gov.justice.digital.delius.data.api.CreatePrisonOffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.PrisonOffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.ResponsibleOfficer;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.PrisonOffenderManagerRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ProbationAreaRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ResponsibleOfficerRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.aPrisonProbationArea;
import static uk.gov.justice.digital.delius.util.EntityHelper.aProbationArea;
import static uk.gov.justice.digital.delius.util.EntityHelper.aResponsibleOfficer;
import static uk.gov.justice.digital.delius.util.EntityHelper.aStaff;
import static uk.gov.justice.digital.delius.util.EntityHelper.aTeam;
import static uk.gov.justice.digital.delius.util.EntityHelper.anActivePrisonOffenderManager;
import static uk.gov.justice.digital.delius.util.EntityHelper.anOffender;

@ExtendWith(MockitoExtension.class)
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
    }

    @Nested
    @DisplayName("allocatePrisonOffenderManagerByStaffId")
    class AllocatePrisonOffenderManagerByStaffId {
        @BeforeEach
        public void setup() {
            when(offenderRepository.findMostLikelyByNomsNumber(any())).thenReturn(Either.right(Optional.of(anOffender())));
            when(staffService.findByStaffId(anyLong())).thenReturn(Optional.of(aStaff()));
            when(probationAreaRepository.findByInstitutionByNomsCDECode(any())).thenAnswer(args -> {
                final var code = args.getArgument(0).toString();
                return Optional.of(aPrisonProbationArea()
                        .toBuilder()
                        .code(code)
                        .build());
            });
        }


        @Test
        public void willReturnEmptyWhenOffenderNotFound() {
            when(offenderRepository.findMostLikelyByNomsNumber("G9542VP")).thenReturn(Either.right(Optional.empty()));
            when(staffService.findByStaffId(anyLong())).thenReturn(Optional.of(aStaff()));
            when(probationAreaRepository.findByInstitutionByNomsCDECode("BWI")).thenReturn(Optional.of(aProbationArea()));

            assertThat(offenderManagerService.allocatePrisonOffenderManagerByStaffId(
                    "G9542VP",
                    12345L,
                    CreatePrisonOffenderManager
                            .builder()
                            .nomsPrisonInstitutionCode("BWI")
                            .build()))
                    .isNotPresent();
        }

        @Test
        public void willReturnEmptyWhenStaffNotFound() {
            when(offenderRepository.findMostLikelyByNomsNumber("G9542VP")).thenReturn(Either.right(Optional.of(anOffender())));
            when(staffService.findByStaffId(anyLong())).thenReturn(Optional.empty());
            when(probationAreaRepository.findByInstitutionByNomsCDECode("BWI")).thenReturn(Optional.of(aProbationArea()));

            assertThat(offenderManagerService.allocatePrisonOffenderManagerByStaffId(
                    "G9542VP",
                    12345L,
                    CreatePrisonOffenderManager
                            .builder()
                            .nomsPrisonInstitutionCode("BWI")
                            .build()))
                    .isNotPresent();
        }

        @Test
        public void willThrowInvalidRequestWhenPrisonProbationAreaNotFound() {
            when(offenderRepository.findMostLikelyByNomsNumber("G9542VP")).thenReturn(Either.right(Optional.of(anOffender())));
            when(staffService.findByStaffId(anyLong())).thenReturn(Optional.of(aStaff()));
            when(probationAreaRepository.findByInstitutionByNomsCDECode("BWI")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> offenderManagerService.allocatePrisonOffenderManagerByStaffId(
                    "G9542VP",
                    12345L,
                    CreatePrisonOffenderManager
                            .builder()
                            .nomsPrisonInstitutionCode("BWI")
                            .build()))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("Prison NOMS code BWI not found");
        }

        @Test
        public void willNotAllowAllocationOfStaffInDifferentProbationArea() {
            when(offenderRepository.findMostLikelyByNomsNumber("G9542VP")).thenReturn(Either.right(Optional.of(anOffender())));
            when(staffService.findByStaffId(anyLong())).thenReturn(Optional.of(aStaff()
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
            when(teamService.findOrCreatePrisonOffenderManagerTeamInArea(any())).thenReturn(aTeam());

            assertThatThrownBy(() -> offenderManagerService.allocatePrisonOffenderManagerByStaffId(
                    "G9542VP",
                    12345L,
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
            when(staffService.findByStaffId(anyLong())).thenReturn(Optional.of(staff));

            final var team = aTeam()
                    .toBuilder()
                    .teamId(99L)
                    .build();
            when(teamService.findOrCreatePrisonOffenderManagerTeamInArea(any())).thenReturn(
                    team);
            when(responsibleOfficerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
            when(prisonOffenderManagerRepository.save(any())).thenAnswer(args -> args.getArgument(0));

            offenderManagerService.allocatePrisonOffenderManagerByStaffId(
                    "G9542VP",
                    12345L,
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

            when(offenderRepository.findMostLikelyByNomsNumber(any())).thenReturn(Either.right(Optional.of(
                    anOffender()
                            .toBuilder()
                            .prisonOffenderManagers(new ArrayList<>(List.of()))
                            .build()
            )));
            when(prisonOffenderManagerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
            when(teamService.findOrCreatePrisonOffenderManagerTeamInArea(any())).thenReturn(aTeam());

            offenderManagerService.allocatePrisonOffenderManagerByStaffId(
                    "G9542VP",
                    12345L,
                    CreatePrisonOffenderManager
                            .builder()
                            .nomsPrisonInstitutionCode("N01")
                            .build());


            verify(prisonOffenderManagerRepository).save(prisonOffenderManagerArgumentCaptor.capture());
            assertThat(prisonOffenderManagerArgumentCaptor
                    .getValue()
                    .getAllocationReason()
                    .getCodeValue()).isEqualTo("AUT");
        }

        @Test
        public void allocationReasonIsInternalTransferWhenExistingAndNewPOMInSameArea() {
            when(referenceDataService.pomAllocationInternalTransferReason()).thenReturn(StandardReference
                    .builder()
                    .codeValue("INA")
                    .build());


            final var existingPOM = anActivePrisonOffenderManager()
                    .toBuilder()
                    .probationArea(aPrisonProbationArea()
                            .toBuilder()
                            .code("BWI")
                            .build())
                    .build();

            when(offenderRepository.findMostLikelyByNomsNumber(any())).thenReturn(Either.right(Optional.of(
                    anOffender()
                            .toBuilder()
                            .prisonOffenderManagers(new ArrayList<>(List.of(existingPOM)))
                            .build()
            )));
            when(responsibleOfficerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
            when(prisonOffenderManagerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
            when(teamService.findOrCreatePrisonOffenderManagerTeamInArea(any())).thenReturn(aTeam());

            offenderManagerService.allocatePrisonOffenderManagerByStaffId(
                    "G9542VP",
                    12345L,
                    CreatePrisonOffenderManager
                            .builder()
                            .nomsPrisonInstitutionCode("BWI")
                            .build());


            verify(prisonOffenderManagerRepository).save(prisonOffenderManagerArgumentCaptor.capture());
            assertThat(prisonOffenderManagerArgumentCaptor
                    .getValue()
                    .getAllocationReason()
                    .getCodeValue()).isEqualTo("INA");
        }

        @Test
        public void allocationReasonIsExternalTransferWhenExistingAndNewPOMDifferentSameArea() {
            when(referenceDataService.pomAllocationExternalTransferReason()).thenReturn(StandardReference
                    .builder()
                    .codeValue("EXT")
                    .build());


            final var existingPOM = anActivePrisonOffenderManager()
                    .toBuilder()
                    .probationArea(aPrisonProbationArea()
                            .toBuilder()
                            .code("BWI")
                            .build())
                    .build();

            when(offenderRepository.findMostLikelyByNomsNumber(any())).thenReturn(Either.right(Optional.of(
                    anOffender()
                            .toBuilder()
                            .prisonOffenderManagers(new ArrayList<>(List.of(existingPOM)))
                            .build()
            )));
            when(responsibleOfficerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
            when(prisonOffenderManagerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
            when(teamService.findOrCreatePrisonOffenderManagerTeamInArea(any())).thenReturn(aTeam());

            offenderManagerService.allocatePrisonOffenderManagerByStaffId(
                    "G9542VP",
                    12345L,
                    CreatePrisonOffenderManager
                            .builder()
                            .nomsPrisonInstitutionCode("BRI")
                            .build());


            verify(prisonOffenderManagerRepository).save(prisonOffenderManagerArgumentCaptor.capture());
            assertThat(prisonOffenderManagerArgumentCaptor
                    .getValue()
                    .getAllocationReason()
                    .getCodeValue()).isEqualTo("EXT");
        }


        @Test
        public void existingPrisonerOffenderManagerIsDeactivated() {
            final var existingPOM = anActivePrisonOffenderManager();

            when(offenderRepository.findMostLikelyByNomsNumber(any())).thenReturn(Either.right(Optional.of(
                    anOffender()
                            .toBuilder()
                            .prisonOffenderManagers(new ArrayList<>(List.of(existingPOM)))
                            .build()
            )));
            when(responsibleOfficerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
            when(prisonOffenderManagerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
            when(teamService.findOrCreatePrisonOffenderManagerTeamInArea(any())).thenReturn(aTeam());

            offenderManagerService.allocatePrisonOffenderManagerByStaffId(
                    "G9542VP",
                    12345L,
                    CreatePrisonOffenderManager
                            .builder()
                            .nomsPrisonInstitutionCode("N01")
                            .build());

            assertThat(existingPOM.getEndDate()).isNotNull();
            assertThat(existingPOM.getActiveFlag()).isEqualTo(0L);
        }

        @Test
        public void existingResponsibleOfficerIsDeactivatedWhenActivePOM() {
            final var existingPOMResponsibleOfficer = aResponsibleOfficer()
                    .toBuilder()
                    .endDateTime(null)
                    .build();
            final var existingPOM = anActivePrisonOffenderManager()
                    .toBuilder()
                    .responsibleOfficers(List.of(existingPOMResponsibleOfficer))
                    .build();

            when(offenderRepository.findMostLikelyByNomsNumber(any())).thenReturn(Either.right(Optional.of(
                    anOffender()
                            .toBuilder()
                            .prisonOffenderManagers(new ArrayList<>(List.of(existingPOM)))
                            .build()
            )));
            when(responsibleOfficerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
            when(prisonOffenderManagerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
            when(teamService.findOrCreatePrisonOffenderManagerTeamInArea(any())).thenReturn(aTeam());

            offenderManagerService.allocatePrisonOffenderManagerByStaffId(
                    "G9542VP",
                    12345L,
                    CreatePrisonOffenderManager
                            .builder()
                            .nomsPrisonInstitutionCode("N01")
                            .build());


            assertThat(existingPOMResponsibleOfficer.getEndDateTime()).isNotNull();
        }

        @Test
        public void newPrisonerOffenderManagerIsMadeResponsibleOfficerWhenExistingPOMIsAlsoCurrentRO() {
            final var existingPOMResponsibleOfficer = aResponsibleOfficer()
                    .toBuilder()
                    .endDateTime(null)
                    .build();
            final var existingPOM = anActivePrisonOffenderManager()
                    .toBuilder()
                    .responsibleOfficers(List.of(existingPOMResponsibleOfficer))
                    .build();

            when(offenderRepository.findMostLikelyByNomsNumber(any())).thenReturn(Either.right(Optional.of(
                    anOffender()
                            .toBuilder()
                            .prisonOffenderManagers(new ArrayList<>(List.of(existingPOM)))
                            .build()
            )));

            when(prisonOffenderManagerRepository.save(any())).thenAnswer(args -> {
                final PrisonOffenderManager newPOM = args.getArgument(0);
                newPOM.setPrisonOffenderManagerId(99L);
                return newPOM;
            });
            when(responsibleOfficerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
            when(teamService.findOrCreatePrisonOffenderManagerTeamInArea(any())).thenReturn(aTeam());

            final var newPrisonOffenderManager = offenderManagerService.allocatePrisonOffenderManagerByStaffId(
                    "G9542VP",
                    12345L,
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
            when(offenderRepository.findMostLikelyByNomsNumber(any())).thenReturn(Either.right(Optional.of(
                    anOffender()
                            .toBuilder()
                            .prisonOffenderManagers(new ArrayList<>(List.of()))
                            .build())));
            when(prisonOffenderManagerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
            when(teamService.findOrCreatePrisonOffenderManagerTeamInArea(any())).thenReturn(aTeam());

            offenderManagerService.allocatePrisonOffenderManagerByStaffId(
                    "G9542VP",
                    12345L,
                    CreatePrisonOffenderManager
                            .builder()
                            .nomsPrisonInstitutionCode("N01")
                            .build());


            verify(contactService).addContactForPOMAllocation(isA(PrisonOffenderManager.class));
        }

        @Test
        public void shouldAddTelemetryEventForPOMAllocation() {
            when(offenderRepository.findMostLikelyByNomsNumber(any())).thenReturn(Either.right(Optional.of(
                    anOffender()
                            .toBuilder()
                            .prisonOffenderManagers(new ArrayList<>(List.of()))
                            .crn("X12345")
                            .build())));
            when(prisonOffenderManagerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
            when(teamService.findOrCreatePrisonOffenderManagerTeamInArea(any())).thenReturn(aTeam()
                    .toBuilder()
                    .probationArea(aPrisonProbationArea().toBuilder().code("N01").build())
                    .build());
            when(staffService.findByStaffId(anyLong())).thenReturn(Optional.of(aStaff("N01ABC")));

            offenderManagerService.allocatePrisonOffenderManagerByStaffId(
                    "G9542VP",
                    12345L,
                    CreatePrisonOffenderManager
                            .builder()
                            .nomsPrisonInstitutionCode("N01")
                            .build());

            verify(telemetryClient).trackEvent(eq("POMAllocated"), eq(Map.of("probationArea", "N01", "crn", "X12345", "staffCode", "N01ABC")), isNull());
        }

        @Test
        public void shouldAddAPOMAllocationContactNotingOldPOM() {
            final var existingPOM = anActivePrisonOffenderManager();
            when(offenderRepository.findMostLikelyByNomsNumber(any())).thenReturn(Either.right(Optional.of(
                    anOffender()
                            .toBuilder()
                            .prisonOffenderManagers(new ArrayList<>(List.of(existingPOM)))
                            .build())));
            when(responsibleOfficerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
            when(prisonOffenderManagerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
            when(teamService.findOrCreatePrisonOffenderManagerTeamInArea(any())).thenReturn(aTeam());

            offenderManagerService.allocatePrisonOffenderManagerByStaffId(
                    "G9542VP",
                    12345L,
                    CreatePrisonOffenderManager
                            .builder()
                            .nomsPrisonInstitutionCode("N01")
                            .build());


            verify(contactService).addContactForPOMAllocation(isA(PrisonOffenderManager.class), eq(existingPOM));
        }

        @Test
        public void shouldAddAResponsibleOfficeMoveContactNotingOldPOM() {
            final var existingPOMResponsibleOfficer = aResponsibleOfficer()
                    .toBuilder()
                    .endDateTime(null)
                    .build();
            final var existingPOM = anActivePrisonOffenderManager()
                    .toBuilder()
                    .responsibleOfficers(List.of(existingPOMResponsibleOfficer))
                    .build();

            when(offenderRepository.findMostLikelyByNomsNumber(any())).thenReturn(Either.right(Optional.of(
                    anOffender()
                            .toBuilder()
                            .prisonOffenderManagers(new ArrayList<>(List.of(existingPOM)))
                            .build()
            )));

            when(prisonOffenderManagerRepository.save(any())).thenAnswer(args -> {
                final PrisonOffenderManager newPOM = args.getArgument(0);
                newPOM.setPrisonOffenderManagerId(99L);
                return newPOM;
            });
            when(responsibleOfficerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
            when(teamService.findOrCreatePrisonOffenderManagerTeamInArea(any())).thenReturn(aTeam());

            offenderManagerService.allocatePrisonOffenderManagerByStaffId(
                    "G9542VP",
                    12345L,
                    CreatePrisonOffenderManager
                            .builder()
                            .nomsPrisonInstitutionCode("N01")
                            .build());

            verify(contactService).addContactForResponsibleOfficerChange(isA(PrisonOffenderManager.class), eq(existingPOM));
        }

        @Test
        public void shouldAddTelemetryEventForROAllocation() {
            final var existingPOMResponsibleOfficer = aResponsibleOfficer()
                    .toBuilder()
                    .endDateTime(null)
                    .build();
            final var existingPOM = anActivePrisonOffenderManager()
                    .toBuilder()
                    .responsibleOfficers(List.of(existingPOMResponsibleOfficer))
                    .build();

            when(offenderRepository.findMostLikelyByNomsNumber(any())).thenReturn(Either.right(Optional.of(
                    anOffender()
                            .toBuilder()
                            .crn("X12345")
                            .prisonOffenderManagers(new ArrayList<>(List.of(existingPOM)))
                            .build()
            )));

            when(prisonOffenderManagerRepository.save(any())).thenAnswer(args -> {
                final PrisonOffenderManager newPOM = args.getArgument(0);
                newPOM.setPrisonOffenderManagerId(99L);
                return newPOM;
            });
            when(responsibleOfficerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
            when(teamService.findOrCreatePrisonOffenderManagerTeamInArea(any())).thenReturn(aTeam());
            when(staffService.findByStaffId(anyLong())).thenReturn(Optional.of(aStaff("N01ABC")));

            offenderManagerService.allocatePrisonOffenderManagerByStaffId(
                    "G9542VP",
                    12345L,
                    CreatePrisonOffenderManager
                            .builder()
                            .nomsPrisonInstitutionCode("N01")
                            .build());

            verify(telemetryClient).trackEvent(eq("POMResponsibleOfficerSet"), eq(Map.of("probationArea", "N01", "crn", "X12345", "staffCode", "N01ABC")), isNull());
        }
    }

    @Nested
    @DisplayName("allocatePrisonOffenderManagerByName")
    class AllocatePrisonOffenderManagerByName {
        @Captor
        private ArgumentCaptor<ContactableHuman> humanCaptor;

        @BeforeEach
        public void setup() {
            when(offenderRepository.findMostLikelyByNomsNumber(any())).thenReturn(Either.right(Optional.of(
                    anOffender()
                            .toBuilder()
                            .prisonOffenderManagers(new ArrayList<>(List.of()))
                            .build())));
            when(probationAreaRepository.findByInstitutionByNomsCDECode(any())).thenAnswer(args -> {
                final var code = args.getArgument(0).toString();
                return Optional.of(aPrisonProbationArea()
                        .toBuilder()
                        .code(code)
                        .build());
            });
        }

        @Test
        public void willReturnEmptyWhenOffenderNotFound() {
            when(offenderRepository.findMostLikelyByNomsNumber("G9542VP")).thenReturn(Either.right(Optional.empty()));

            assertThat(offenderManagerService.allocatePrisonOffenderManagerByName(
                    "G9542VP",
                    CreatePrisonOffenderManager
                            .builder()
                            .nomsPrisonInstitutionCode("BWI")
                            .officer(ContactableHuman
                                    .builder()
                                    .surname("Smith")
                                    .forenames("John")
                                    .build())
                            .build()))
                    .isNotPresent();
        }

        @Test
        @DisplayName("will throw a conflict if multiple active offenders found for NOMS number")
        @MockitoSettings(strictness = Strictness.LENIENT)
        void willThrowAConflictIfMultipleActiveOffendersFoundForNOMSNumber() {
            when(offenderRepository.findMostLikelyByNomsNumber("G9542VP")).thenReturn(Either.left(new OffenderRepository.DuplicateOffenderException("two found!")));

            assertThatThrownBy(() -> offenderManagerService.allocatePrisonOffenderManagerByName(
                    "G9542VP",
                    CreatePrisonOffenderManager
                            .builder()
                            .nomsPrisonInstitutionCode("BWI")
                            .officer(ContactableHuman
                                    .builder()
                                    .surname("Smith")
                                    .forenames("John")
                                    .build())
                            .build()))
                    .isInstanceOf(ConflictingRequestException.class);

        }

        @Test
        public void willThrowInvalidRequestWhenPrisonProbationAreaNotFound() {
            when(probationAreaRepository.findByInstitutionByNomsCDECode("BWI")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> offenderManagerService.allocatePrisonOffenderManagerByName(
                    "G9542VP",
                    CreatePrisonOffenderManager
                            .builder()
                            .nomsPrisonInstitutionCode("BWI")
                            .officer(ContactableHuman
                                    .builder()
                                    .build())
                            .build()))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("Prison NOMS code BWI not found");
        }

        @Nested
        @DisplayName("When successful")
        class OnSuccess {
            @BeforeEach
            void setUp() {
                when(staffService.findOrCreateStaffInArea(any(), any())).thenReturn(aStaff());
            }

            @Test
            public void willCapitaliseStaffName() {
                when(prisonOffenderManagerRepository.save(any())).thenAnswer(args -> args.getArgument(0));

                offenderManagerService.allocatePrisonOffenderManagerByName(
                        "G9542VP",
                        CreatePrisonOffenderManager
                                .builder()
                                .nomsPrisonInstitutionCode("N01")
                                .officer(ContactableHuman
                                        .builder()
                                        .surname("SMITH")
                                        .forenames("JOHN")
                                        .build())
                                .build());

                verify(staffService).findOrCreateStaffInArea(humanCaptor.capture(), any());
                assertThat(humanCaptor.getValue().getSurname()).isEqualTo("Smith");
                assertThat(humanCaptor.getValue().getForenames()).isEqualTo("John");
            }
        }
    }
}
