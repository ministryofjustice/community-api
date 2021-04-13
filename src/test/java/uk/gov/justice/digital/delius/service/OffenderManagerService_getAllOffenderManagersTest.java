package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;
import uk.gov.justice.digital.delius.data.api.StaffDetails;
import uk.gov.justice.digital.delius.jpa.standard.entity.Staff;
import uk.gov.justice.digital.delius.jpa.standard.entity.User;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.PrisonOffenderManagerRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ProbationAreaRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ResponsibleOfficerRepository;
import uk.gov.justice.digital.delius.util.EntityHelper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.*;

@ExtendWith(MockitoExtension.class)
class OffenderManagerService_getAllOffenderManagersTest {
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

    private OffenderManagerService offenderManagerService;

    @BeforeEach
    void setup() {
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
    @DisplayName("getAllOffenderManagersForNomsNumber")
    class byNomsNumber {

        @Test
        void willReturnEmptyWhenOffenderNotFound() {
            when(offenderRepository.findByNomsNumber("G9542VP")).thenReturn(Optional.empty());

            assertThat(offenderManagerService.getAllOffenderManagersForNomsNumber("G9542VP")).isNotPresent();
        }

        @Test
        void willReturnAListWhenOffenderFound() {
            when(offenderRepository.findByNomsNumber("G9542VP")).thenReturn(Optional.of(anOffender()));

            assertThat(offenderManagerService.getAllOffenderManagersForNomsNumber("G9542VP")).isPresent();
        }

        @Test
        void willReturnAnEmptyListWhenOffenderHasNoCOMorPOMS() {
            when(offenderRepository.findByNomsNumber("G9542VP"))
                .thenReturn(Optional.of(anOffender(
                    List.of(),
                    List.of())));

            assertThat(offenderManagerService.getAllOffenderManagersForNomsNumber("G9542VP")).get().asList()
                .hasSize(0);
        }

        @Test
        void willReturnActiveCommunityOffenderManagers() {
            when(offenderRepository.findByNomsNumber("G9542VP"))
                .thenReturn(Optional.of(anOffender(
                    List.of(anActiveOffenderManager()),
                    List.of())));

            assertThat(offenderManagerService.getAllOffenderManagersForNomsNumber("G9542VP")).get().asList()
                .hasSize(1);
        }

        @Test
        void willOnlyReturnActiveCommunityOffenderManagers() {
            when(offenderRepository.findByNomsNumber("G9542VP"))
                .thenReturn(Optional.of(anOffender(
                    List.of(
                        anActiveOffenderManager("AA"),
                        anInactiveOffenderManager("BB"),
                        anEndDatedActiveOffenderManager("CC")
                    ),
                    List.of())));

            assertThat(offenderManagerService.getAllOffenderManagersForNomsNumber("G9542VP")).get().asList()
                .hasSize(1).allMatch(offenderManager -> ((CommunityOrPrisonOffenderManager) offenderManager).getStaffCode().equals("AA"));
        }

        @Test
        void willReturnActivePrisonOffenderManagers() {
            when(offenderRepository.findByNomsNumber("G9542VP"))
                .thenReturn(Optional.of(anOffender(
                    List.of(),
                    List.of(anActivePrisonOffenderManager())
                )));

            assertThat(offenderManagerService.getAllOffenderManagersForNomsNumber("G9542VP")).get().asList()
                .hasSize(1);
        }

        @Test
        void willOnlyReturnActivePrisonOffenderManagers() {
            when(offenderRepository.findByNomsNumber("G9542VP"))
                .thenReturn(Optional.of(anOffender(
                    List.of(),
                    List.of(
                        anActivePrisonOffenderManager("AA"),
                        anInactivePrisonOffenderManager("BB"),
                        anEndDatedActivePrisonOffenderManager("CC")
                    ))));

            assertThat(offenderManagerService.getAllOffenderManagersForNomsNumber("G9542VP")).get().asList()
                .hasSize(1).allMatch(offenderManager -> ((CommunityOrPrisonOffenderManager) offenderManager).getStaffCode().equals("AA"));
        }
    }

    @Nested
    @DisplayName("getAllOffenderManagersForCrn")
    class byCrn {

        private static final String CRN = "X320741";

        @Test
        void willReturnEmptyWhenOffenderNotFound() {
            when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.empty());

            assertThat(offenderManagerService.getAllOffenderManagersForCrn(CRN)).isNotPresent();
        }

        @Test
        void willReturnAListWhenOffenderFound() {
            when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(anOffender()));

            assertThat(offenderManagerService.getAllOffenderManagersForCrn(CRN)).isPresent();
        }

        @Test
        void willReturnAnEmptyListWhenOffenderHasNoCOMorPOMS() {
            when(offenderRepository.findByCrn(CRN))
                .thenReturn(Optional.of(anOffender(
                    List.of(),
                    List.of())));

            assertThat(offenderManagerService.getAllOffenderManagersForCrn(CRN)).get().asList()
                .hasSize(0);
        }

        @Test
        void willReturnActiveCommunityOffenderManagers() {
            var staffDetails = StaffDetails.builder()
                .email("no-one@nowhere.com")
                .telephoneNumber("020 1111 2222")
                .build();

            when(offenderRepository.findByCrn(CRN))
                .thenReturn(Optional.of(anOffender(
                    List.of(anActiveOffenderManager()),
                    List.of())));
            when(staffService.getStaffDetailsByUsername("XX")).thenReturn(Optional.of(staffDetails));

            var offenderMgrs  = offenderManagerService.getAllOffenderManagersForCrn(CRN).get();
            assertThat(offenderMgrs).hasSize(1);
            assertThat(offenderMgrs.get(0).getStaff().getEmail()).isEqualTo("no-one@nowhere.com");
            assertThat(offenderMgrs.get(0).getStaff().getPhoneNumber()).isEqualTo("020 1111 2222");

            verify(staffService).getStaffDetailsByUsername("XX");
        }

        @Test
        void givenNullStaff_whenEnhanceLdapFields_thenIgnore() {
            var offenderManager = EntityHelper.anOffenderManager(null, null);

            assertThat(offenderManagerService.addLdapFields(offenderManager)).isEqualTo(offenderManager);
        }

        @Test
        void givenNullUser_whenEnhanceLdapFields_thenIgnore() {
            var offenderManager = EntityHelper.anOffenderManager(Staff.builder().build(), null);

            assertThat(offenderManagerService.addLdapFields(offenderManager)).isEqualTo(offenderManager);
        }

        @Test
        void givenNullDistinguishedName_whenEnhanceLdapFields_thenIgnore() {
            var staff = Staff.builder()
                .user(User.builder().build())
                .build();
            var offenderManager = EntityHelper.anOffenderManager(staff, null);

            assertThat(offenderManagerService.addLdapFields(offenderManager)).isEqualTo(offenderManager);
        }

    }
}
