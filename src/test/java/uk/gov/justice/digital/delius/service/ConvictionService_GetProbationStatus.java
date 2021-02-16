package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.config.FeatureSwitches;
import uk.gov.justice.digital.delius.data.api.ProbationStatus;
import uk.gov.justice.digital.delius.entitybuilders.EventEntityBuilder;
import uk.gov.justice.digital.delius.entitybuilders.KeyDateEntityBuilder;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
public class ConvictionService_GetProbationStatus {

    private ConvictionService convictionService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private OffenderRepository offenderRepository;

    @Mock
    private EventEntityBuilder eventEntityBuilder;

    @Mock
    private SpgNotificationService spgNotificationService;

    @Mock
    private LookupSupplier lookupSupplier;

    @Mock
    private IAPSNotificationService iapsNotificationService;

    @Mock
    private ContactService contactService;

    @Mock
    private TelemetryClient telemetryClient;


    @BeforeEach
    public void setUp(){
        final var featureSwitches = new FeatureSwitches();
        convictionService = new ConvictionService(eventRepository, offenderRepository, eventEntityBuilder, spgNotificationService, lookupSupplier, new KeyDateEntityBuilder(lookupSupplier), iapsNotificationService, contactService, telemetryClient, featureSwitches);
    }

    @Test
    public void canGetProbationStatusForCurrentOffender() {
        final var probationStatusDetail = convictionService.probationStatusFor("A_CRN").get();

        assertThat(probationStatusDetail.getProbationStatus()).isEqualTo(ProbationStatus.CURRENT);
        assertThat(probationStatusDetail.getPreviouslyKnownTerminationDate()).isEqualTo(LocalDate.of(2020, 1, 4));
        assertThat(probationStatusDetail.getInBreach()).isEqualTo(true);
        assertThat(probationStatusDetail.getPreSentenceActivity()).isEqualTo(false);
    }

    @Test
    public void canGetProbationStatusForPreviouslyKnownOffender() {
        final var probationStatusDetail = convictionService.probationStatusFor("A_CRN").get();

        assertThat(probationStatusDetail.getProbationStatus()).isEqualTo(ProbationStatus.PREVIOUSLY_KNOWN);
        assertThat(probationStatusDetail.getPreviouslyKnownTerminationDate()).isEqualTo(LocalDate.of(2020, 1, 4));
        assertThat(probationStatusDetail.getInBreach()).isEqualTo(null);
        assertThat(probationStatusDetail.getPreSentenceActivity()).isEqualTo(false);
    }

    @Test
    public void canGetProbationStatusForOffenderWithPreSentenceActivity() {
        final var probationStatusDetail = convictionService.probationStatusFor("A_CRN").get();

        assertThat(probationStatusDetail.getProbationStatus()).isEqualTo(ProbationStatus.NOT_SENTENCED);
        assertThat(probationStatusDetail.getPreviouslyKnownTerminationDate()).isEqualTo(null);
        assertThat(probationStatusDetail.getInBreach()).isEqualTo(null);
        assertThat(probationStatusDetail.getPreSentenceActivity()).isEqualTo(true);
    }

    @Test
    public void canGetProbationStatusForOffenderWithNoEvents() {
        final var probationStatusDetail = convictionService.probationStatusFor("A_CRN").get();

        assertThat(probationStatusDetail.getProbationStatus()).isEqualTo(ProbationStatus.NOT_SENTENCED);
        assertThat(probationStatusDetail.getPreviouslyKnownTerminationDate()).isEqualTo(null);
        assertThat(probationStatusDetail.getInBreach()).isEqualTo(null);
        assertThat(probationStatusDetail.getPreSentenceActivity()).isEqualTo(false);
    }

    @Test
    public void returnEmptyForNonExistentOffender() {
        final var probationStatusDetail = convictionService.probationStatusFor("NOT_THERE");

        assertThat(probationStatusDetail).isEmpty();
    }
}
