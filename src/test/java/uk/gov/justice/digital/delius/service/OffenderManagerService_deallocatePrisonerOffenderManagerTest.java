package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.jpa.standard.entity.PrisonOffenderManager;
import uk.gov.justice.digital.delius.jpa.standard.entity.ResponsibleOfficer;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository.DuplicateOffenderException;
import uk.gov.justice.digital.delius.jpa.standard.repository.PrisonOffenderManagerRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ProbationAreaRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ResponsibleOfficerRepository;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.aPrisonProbationArea;
import static uk.gov.justice.digital.delius.util.EntityHelper.aStaff;
import static uk.gov.justice.digital.delius.util.EntityHelper.aTeam;
import static uk.gov.justice.digital.delius.util.EntityHelper.anOffender;

@ExtendWith(MockitoExtension.class)
public class OffenderManagerService_deallocatePrisonerOffenderManagerTest {
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

    @Test
    public void deallocatePrisonerOffenderManagerThrowsDuplicateIfMoreThanOneNoms() {
        when(offenderRepository.findMostLikelyByNomsNumber(anyString()))
            .thenReturn(Either.left(new DuplicateOffenderException("some problem")));
        assertThatThrownBy(() -> offenderManagerService.deallocatePrisonerOffenderManager("A1234AB"))
            .isInstanceOf(DuplicateOffenderException.class);
    }

    @Test
    public void deallocatePrisonerOffenderManagerSuccess() {
        when(offenderRepository.findMostLikelyByNomsNumber(anyString())).thenReturn(Either.right(Optional.of(anOffender())));
        when(probationAreaRepository.findByInstitutionInstitutionId(any())).thenAnswer(args -> {
            final var code = args.getArgument(0).toString();
            return Optional.of(aPrisonProbationArea()
                .toBuilder()
                .code(code)
                .build());
        });
        when(prisonOffenderManagerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
        when(responsibleOfficerRepository.save(any())).thenAnswer(args -> args.getArgument(0));
        when(teamService.findUnallocatedTeam(any())).thenReturn(Optional.of(aTeam()));
        when(staffService.findUnallocatedForTeam(any())).thenReturn(Optional.of(aStaff()));

        offenderManagerService.deallocatePrisonerOffenderManager("A1234AB");

        verify(telemetryClient).trackEvent("POMAllocated",
            Map.of("probationArea", "12", "staffCode", "A1234", "crn", "crn123"), null);
    }
}
