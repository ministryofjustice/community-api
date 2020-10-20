package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.controller.ConflictingRequestException;
import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.ResponsibleOfficerSwitch;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.PrisonOffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.ResponsibleOfficer;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.PrisonOffenderManagerRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ProbationAreaRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ResponsibleOfficerRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.aResponsibleOfficer;
import static uk.gov.justice.digital.delius.util.EntityHelper.anActiveOffenderManager;
import static uk.gov.justice.digital.delius.util.EntityHelper.anActivePrisonOffenderManager;
import static uk.gov.justice.digital.delius.util.EntityHelper.anInactiveOffenderManager;
import static uk.gov.justice.digital.delius.util.EntityHelper.anInactivePrisonOffenderManager;
import static uk.gov.justice.digital.delius.util.EntityHelper.anOffender;

@ExtendWith(MockitoExtension.class)
public class OffenderManagerService_switchResponsibleOfficerTest {
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
                contactService);

    }

    protected void setupOffender(final OffenderManager com, final PrisonOffenderManager pom) {
        when(offenderRepository.findByNomsNumber("G0560UO"))
                .thenReturn(Optional.of(anOffender(List.of(com), List.of(pom)).toBuilder()
                        .nomsNumber("G0560UO")
                        .offenderId(99L)
                        .build()));
    }

    protected void setupOffender(final OffenderManager com) {
        when(offenderRepository.findByNomsNumber("G0560UO"))
                .thenReturn(Optional.of(anOffender(List.of(com), List.of()).toBuilder()
                        .nomsNumber("G0560UO")
                        .offenderId(99L)
                        .build()));
    }

    @Nested
    @DisplayName("When switching to a community offender manager")
    class WhenSwitchingToCOM {
        private final ResponsibleOfficerSwitch comSwitchRequest = ResponsibleOfficerSwitch.builder()
                .switchToCommunityOffenderManager(true).build();

        @Nested
        @DisplayName("and there is no responsible officer")
        class WhenSwitchingROIsNotPresent {
            @BeforeEach
            void setUp() {
                setupOffender(
                        anActiveOffenderManager().toBuilder().responsibleOfficers(List.of()).build());
            }

            @Test
            @DisplayName("then an exception will be thrown indicating a conflicting request")
            void willNotDoAnything() {
                assertThatThrownBy(() -> offenderManagerService.switchResponsibleOfficer("G0560UO", comSwitchRequest))
                        .isInstanceOf(ConflictingRequestException.class)
                        .hasMessage("Cannot find a current RO for G0560UO");

                verifyNoInteractions(responsibleOfficerRepository);
                verifyNoInteractions(contactService);
            }
        }

        @Nested
        @DisplayName("and responsible officer is already a community offender manager")
        class WhenSwitchingROIsAlreadyTheCOM {
            @BeforeEach
            void setUp() {
                setupOffender(
                        anActiveOffenderManager().toBuilder().responsibleOfficers(List.of(aResponsibleOfficer())).build(),
                        anActivePrisonOffenderManager().toBuilder().responsibleOfficers(List.of()).build());
            }

            @Test
            @DisplayName("then nothing will be updated")
            void willNotDoAnything() {
                offenderManagerService.switchResponsibleOfficer("G0560UO", comSwitchRequest);

                verifyNoInteractions(responsibleOfficerRepository);
                verifyNoInteractions(contactService);
            }
        }

        @Nested
        @DisplayName("and offender has no active community offender manager")
        class WhenThereIsNotActiveCOM {
            @BeforeEach
            void setUp() {
                setupOffender(
                        anInactiveOffenderManager("ABC"),
                        anActivePrisonOffenderManager().toBuilder().responsibleOfficers(List.of(aResponsibleOfficer())).build());
            }

            @Test
            @DisplayName("then an exception will be thrown indicating a conflicting request")
            void willNotDoAnything() {
                assertThatThrownBy(() -> offenderManagerService.switchResponsibleOfficer("G0560UO", comSwitchRequest))
                        .isInstanceOf(ConflictingRequestException.class)
                        .hasMessage("Cannot find an active COM for G0560UO");

                verifyNoInteractions(responsibleOfficerRepository);
                verifyNoInteractions(contactService);
            }

        }

        @Nested
        @DisplayName("and it is successfully switched")
        class WhenSuccess {
            private OffenderManager com;
            private PrisonOffenderManager pom;
            private CommunityOrPrisonOffenderManager ro;

            @BeforeEach
            void setUp() {
                when(responsibleOfficerRepository.save(any())).thenReturn(aResponsibleOfficer());
                com = anActiveOffenderManager();
                com.getResponsibleOfficers().clear();
                pom = anActivePrisonOffenderManager();
                pom.getResponsibleOfficers().clear();
                pom.addResponsibleOfficer(aResponsibleOfficer());

                setupOffender(com, pom);

                ro = offenderManagerService.switchResponsibleOfficer("G0560UO", comSwitchRequest);
            }

            @Test
            @DisplayName("then a new responsible officer will be created")
            void willCreateAResponsibleOfficer() {
                verify(responsibleOfficerRepository).save(responsibleOfficerArgumentCaptor.capture());
                assertThat(responsibleOfficerArgumentCaptor.getValue().getOffenderId()).isEqualTo(99L);
            }

            @Test
            @DisplayName("then community offender manager will be set as the responsible officer")
            void willSetCOMAsRo() {
                assertThat(com.getActiveResponsibleOfficer()).isNotNull();
                assertThat(pom.getActiveResponsibleOfficer()).isNull();
            }

            @Test
            @DisplayName("then a contact will be created using the new and old responsible officer")
            void willCreateAContact() {
                verify(contactService).addContactForResponsibleOfficerChange(com, pom);
            }


            @Test
            @DisplayName("then the new responsible officer will be returned")
            void willReturnNewRo() {
                assertThat(ro.getIsPrisonOffenderManager()).isFalse();
                assertThat(ro.getIsResponsibleOfficer()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("When switching to a prison offender manager")
    class WhenSwitchingToPOM {
        private final ResponsibleOfficerSwitch comSwitchRequest = ResponsibleOfficerSwitch.builder()
                .switchToPrisonOffenderManager(true).build();

        @Nested
        @DisplayName("and there is no responsible officer")
        class WhenSwitchingROIsNotPresent {
            @BeforeEach
            void setUp() {
                setupOffender(
                        anActiveOffenderManager().toBuilder().responsibleOfficers(List.of()).build());
            }

            @Test
            @DisplayName("then an exception will be thrown indicating a conflicting request")
            void willNotDoAnything() {
                assertThatThrownBy(() -> offenderManagerService.switchResponsibleOfficer("G0560UO", comSwitchRequest))
                        .isInstanceOf(ConflictingRequestException.class)
                        .hasMessage("Cannot find a current RO for G0560UO");

                verifyNoInteractions(responsibleOfficerRepository);
                verifyNoInteractions(contactService);
            }
        }


        @Nested
        @DisplayName("and responsible officer is already a prison offender manager")
        class WhenSwitchingROIsAlreadyThePOM {
            @BeforeEach
            void setUp() {
                setupOffender(
                        anActiveOffenderManager().toBuilder().responsibleOfficers(List.of()).build(),
                        anActivePrisonOffenderManager().toBuilder().responsibleOfficers(List.of(aResponsibleOfficer())).build());
            }

            @Test
            @DisplayName("then nothing will be updated")
            void willNotDoAnything() {
                offenderManagerService.switchResponsibleOfficer("G0560UO", comSwitchRequest);

                verifyNoInteractions(responsibleOfficerRepository);
                verifyNoInteractions(contactService);
            }
        }

        @Nested
        @DisplayName("and offender has no active prison offender manager")
        class WhenThereIsNotActivePOM {

            @BeforeEach
            void setUp() {
                setupOffender(
                        anActiveOffenderManager().toBuilder().responsibleOfficers(List.of(aResponsibleOfficer())).build(),
                        anInactivePrisonOffenderManager("ABC").toBuilder().build());
            }

            @Test
            @DisplayName("then an exception will be thrown indicating a conflicting request")
            void willNotDoAnything() {
                assertThatThrownBy(() -> offenderManagerService.switchResponsibleOfficer("G0560UO", comSwitchRequest))
                        .isInstanceOf(ConflictingRequestException.class)
                        .hasMessage("Cannot find an active POM for G0560UO");

                verifyNoInteractions(responsibleOfficerRepository);
                verifyNoInteractions(contactService);
            }

        }

        @Nested
        @DisplayName("and it is successfully switched")
        class WhenSuccess {
            private OffenderManager com;
            private PrisonOffenderManager pom;
            private CommunityOrPrisonOffenderManager ro;

            @BeforeEach
            void setUp() {
                when(responsibleOfficerRepository.save(any())).thenReturn(aResponsibleOfficer());
                com = anActiveOffenderManager();
                com.getResponsibleOfficers().clear();
                com.addResponsibleOfficer(aResponsibleOfficer());
                pom = anActivePrisonOffenderManager();
                pom.getResponsibleOfficers().clear();

                setupOffender(com, pom);

                ro = offenderManagerService.switchResponsibleOfficer("G0560UO", comSwitchRequest);
            }

            @Test
            @DisplayName("then a new responsible officer will be created")
            void willCreateAResponsibleOfficer() {
                verify(responsibleOfficerRepository).save(responsibleOfficerArgumentCaptor.capture());
                assertThat(responsibleOfficerArgumentCaptor.getValue().getOffenderId()).isEqualTo(99L);
            }

            @Test
            @DisplayName("then prison offender manager will be set as the responsible officer")
            void willSetPOMAsRo() {
                assertThat(pom.getActiveResponsibleOfficer()).isNotNull();
                assertThat(com.getActiveResponsibleOfficer()).isNull();
            }

            @Test
            @DisplayName("then a contact will be created using the new and old responsible officer")
            void willCreateAContact() {
                verify(contactService).addContactForResponsibleOfficerChange(pom, com);
            }


            @Test
            @DisplayName("then the new responsible officer will be returned")
            void willReturnNewRo() {
                assertThat(ro.getIsPrisonOffenderManager()).isTrue();
                assertThat(ro.getIsResponsibleOfficer()).isTrue();
            }
        }
    }
}
