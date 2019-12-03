package uk.gov.justice.digital.delius.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.data.api.CommunityOrPrisonOffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.PrisonOffenderManagerRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ProbationAreaRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ResponsibleOfficerRepository;
import uk.gov.justice.digital.delius.transformers.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.*;

@RunWith(MockitoJUnitRunner.class)
public class OffenderManagerService_getAllOffenderManagersTest {
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

    private OffenderManagerService offenderManagerService;

    @Before
    public void setup() {
        offenderManagerService = new OffenderManagerService(
                offenderRepository,
                new OffenderManagerTransformer(
                        new StaffTransformer(
                                new TeamTransformer()),
                        new TeamTransformer(),
                        new ProbationAreaTransformer(
                                new InstitutionTransformer())),
                probationAreaRepository,
                prisonOffenderManagerRepository,
                responsibleOfficerRepository,
                staffService,
                teamService,
                referenceDataService);
    }

    @Test
    public void willReturnEmptyWhenOffenderNotFound() {
        when(offenderRepository.findByNomsNumber("G9542VP")).thenReturn(Optional.empty());

        assertThat(offenderManagerService.getAllOffenderManagersForNomsNumber("G9542VP")).isNotPresent();
    }

    @Test
    public void willReturnAListWhenOffenderFound() {
        when(offenderRepository.findByNomsNumber("G9542VP")).thenReturn(Optional.of(anOffender()));

        assertThat(offenderManagerService.getAllOffenderManagersForNomsNumber("G9542VP")).isPresent();
    }

    @Test
    public void willReturnAnEmptyListWhenOffenderHasNoCOMorPOMS() {
        when(offenderRepository.findByNomsNumber("G9542VP"))
                .thenReturn(Optional.of(anOffender(
                        List.of(),
                        List.of())));

        assertThat(offenderManagerService.getAllOffenderManagersForNomsNumber("G9542VP")).get().asList()
                .hasSize(0);
    }

    @Test
    public void willReturnActiveCommunityOffenderManagers() {
        when(offenderRepository.findByNomsNumber("G9542VP"))
                .thenReturn(Optional.of(anOffender(
                        List.of(anActiveOffenderManager()),
                        List.of())));

        assertThat(offenderManagerService.getAllOffenderManagersForNomsNumber("G9542VP")).get().asList()
                .hasSize(1);
    }

    @Test
    public void willOnlyReturnActiveCommunityOffenderManagers() {
        when(offenderRepository.findByNomsNumber("G9542VP"))
                .thenReturn(Optional.of(anOffender(
                        List.of(
                                anActiveOffenderManager("AA"),
                                anInactiveOffenderManager("BB"),
                                anEndDatedActiveOffenderManager("CC")
                        ),
                        List.of())));

        assertThat(offenderManagerService.getAllOffenderManagersForNomsNumber("G9542VP")).get().asList()
                .hasSize(1).allMatch(offenderManager -> ((CommunityOrPrisonOffenderManager)offenderManager).getStaffCode().equals("AA"));
    }

    @Test
    public void willReturnActivePrisonOffenderManagers() {
        when(offenderRepository.findByNomsNumber("G9542VP"))
                .thenReturn(Optional.of(anOffender(
                        List.of(),
                        List.of(anActivePrisonOffenderManager())
                        )));

        assertThat(offenderManagerService.getAllOffenderManagersForNomsNumber("G9542VP")).get().asList()
                .hasSize(1);
    }

    @Test
    public void willOnlyReturnActivePrisonOffenderManagers() {
        when(offenderRepository.findByNomsNumber("G9542VP"))
                .thenReturn(Optional.of(anOffender(
                        List.of(),
                        List.of(
                                anActivePrisonOffenderManager("AA"),
                                anInactivePrisonOffenderManager("BB"),
                                anEndDatedActivePrisonOffenderManager("CC")
                        ))));

        assertThat(offenderManagerService.getAllOffenderManagersForNomsNumber("G9542VP")).get().asList()
                .hasSize(1).allMatch(offenderManager -> ((CommunityOrPrisonOffenderManager)offenderManager).getStaffCode().equals("AA"));
    }

}