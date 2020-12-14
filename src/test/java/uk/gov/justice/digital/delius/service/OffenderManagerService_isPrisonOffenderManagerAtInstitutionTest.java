package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.PrisonOffenderManagerRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ProbationAreaRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ResponsibleOfficerRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.digital.delius.util.EntityHelper.*;

@ExtendWith(MockitoExtension.class)
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
    @Mock
    private TelemetryClient telemetryClient;

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