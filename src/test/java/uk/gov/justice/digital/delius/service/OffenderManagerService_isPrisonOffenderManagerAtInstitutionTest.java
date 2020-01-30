package uk.gov.justice.digital.delius.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.PrisonOffenderManagerRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ProbationAreaRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ResponsibleOfficerRepository;
import uk.gov.justice.digital.delius.transformers.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.digital.delius.util.EntityHelper.*;

@RunWith(MockitoJUnitRunner.class)
public class OffenderManagerService_isPrisonOffenderManagerAtInstitutionTest {
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
                referenceDataService,
                contactService);
    }

    @Test
    public void willReturnFalseWhenOffenderHasNoPOM() {
        final var offender = anOffender(List.of(anActiveOffenderManager()), List.of());
        final var institution = aPrisonInstitution();

        assertThat(offenderManagerService.isPrisonOffenderManagerAtInstitution(offender, institution)).isFalse();
    }

    @Test
    public void willReturnFalseWhenOffenderHasPOMAtDifferentInstitution() {
        final var anotherInstitution = aPrisonInstitution().toBuilder().code("WWI").build();
        final var offender = anOffender(List.of(anActiveOffenderManager()), List.of(anActivePrisonOffenderManager("A123")
                .toBuilder()
                .probationArea(aPrisonProbationArea().toBuilder().institution(anotherInstitution).build())
                .build()));
        final var institution = aPrisonInstitution().toBuilder().code("MDI").build();

        assertThat(offenderManagerService.isPrisonOffenderManagerAtInstitution(offender, institution)).isFalse();
    }

    @Test
    public void willReturnTrueWhenOffenderHasPOMAtSameInstitution() {
        final var anotherInstitution = aPrisonInstitution().toBuilder().code("MDI").build();
        final var offender = anOffender(List.of(anActiveOffenderManager()), List.of(anActivePrisonOffenderManager("A123")
                .toBuilder()
                .probationArea(aPrisonProbationArea().toBuilder().institution(anotherInstitution).build())
                .build()));
        final var institution = aPrisonInstitution().toBuilder().code("MDI").build();

        assertThat(offenderManagerService.isPrisonOffenderManagerAtInstitution(offender, institution)).isTrue();
    }

    @Test
    public void willReturnFalseWhenOffenderHasInactivePOMAtSameInstitution() {
        final var anotherInstitution = aPrisonInstitution().toBuilder().code("MDI").build();
        final var offender = anOffender(List.of(anActiveOffenderManager()), List.of(anInactivePrisonOffenderManager("A123")
                .toBuilder()
                .probationArea(aPrisonProbationArea().toBuilder().institution(anotherInstitution).build())
                .build()));
        final var institution = aPrisonInstitution().toBuilder().code("MDI").build();

        assertThat(offenderManagerService.isPrisonOffenderManagerAtInstitution(offender, institution)).isFalse();
    }


}