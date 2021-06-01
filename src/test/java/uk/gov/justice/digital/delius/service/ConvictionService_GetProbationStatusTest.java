package uk.gov.justice.digital.delius.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.service.ReferenceDataService.REFERENCE_DATA_PSR_ADJOURNED_CODE;
import static uk.gov.justice.digital.delius.util.EntityHelper.aCourtAppearanceWithOutcome;

@ExtendWith(MockitoExtension.class)
public class ConvictionService_GetProbationStatusTest {

    private static final String CRN = "CRN";

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

    @Mock
    private Offender offender;

    @Mock
    private Event event;
    @Mock
    private Event event2;
    @Mock
    private Event event3;
    @Mock
    private Disposal disposal;

    @BeforeEach
    public void setUp(){
        final var featureSwitches = new FeatureSwitches();
        convictionService = new ConvictionService(eventRepository, offenderRepository, eventEntityBuilder, spgNotificationService, lookupSupplier, new KeyDateEntityBuilder(lookupSupplier), iapsNotificationService, contactService, telemetryClient, featureSwitches);
    }

    @Test
    public void canGetProbationStatusForCurrentOffenderInBreach() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        // If current disposal == 1L then probation status is CURRENT
        when(offender.getCurrentDisposal()).thenReturn(1L);
        when(offender.getActiveEvents()).thenReturn(List.of(event));
        when(offender.getSoftDeleted()).thenReturn(0L);
        when(event.getDisposal()).thenReturn(Disposal.builder().build());
        when(event.isInBreach()).thenReturn(true);

        final var probationStatusDetail = convictionService.probationStatusFor(CRN).orElseThrow();

        assertThat(probationStatusDetail.getStatus()).isEqualTo(ProbationStatus.CURRENT);
        assertThat(probationStatusDetail.getPreviouslyKnownTerminationDate()).isNull();
        assertThat(probationStatusDetail.getInBreach()).isTrue();
        assertThat(probationStatusDetail.getAwaitingPsr()).isFalse();
        assertThat(probationStatusDetail.getPreSentenceActivity()).isFalse();
    }

    @Test
    public void canGetProbationStatusForCurrentOffenderWithMultipleCurrentEventsInBreach() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        // If current disposal == 1L then probation status is CURRENT
        when(offender.getCurrentDisposal()).thenReturn(1L);
        when(offender.getActiveEvents()).thenReturn(List.of(event, event2, event3));
        when(offender.getSoftDeleted()).thenReturn(0L);
        // If any active event is in breach then offender is in breach
        when(event.isInBreach()).thenReturn(false);
        when(event2.isInBreach()).thenReturn(true);
        when(event2.getCourtAppearances()).thenReturn(List.of(aCourtAppearanceWithOutcome(REFERENCE_DATA_PSR_ADJOURNED_CODE, "Adjourned - PSR")));

        final var probationStatusDetail = convictionService.probationStatusFor(CRN).orElseThrow();

        assertThat(probationStatusDetail.getStatus()).isEqualTo(ProbationStatus.CURRENT);
        assertThat(probationStatusDetail.getPreviouslyKnownTerminationDate()).isNull();
        assertThat(probationStatusDetail.getInBreach()).isTrue();
        assertThat(probationStatusDetail.getPreSentenceActivity()).isTrue();
        assertThat(probationStatusDetail.getAwaitingPsr()).isTrue();
    }

    @Test
    public void canGetProbationStatusForCurrentOffenderWithNoCurrentBreach() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        // If current disposal == 1L then probation status is CURRENT
        when(offender.getSoftDeleted()).thenReturn(0L);
        when(offender.getCurrentDisposal()).thenReturn(1L);
        when(offender.getActiveEvents()).thenReturn(List.of(event));
        // If no active event is in breach then offender is not in breach
        when(event.isInBreach()).thenReturn(false);

        final var probationStatusDetail = convictionService.probationStatusFor(CRN).orElseThrow();

        assertThat(probationStatusDetail.getStatus()).isEqualTo(ProbationStatus.CURRENT);
        assertThat(probationStatusDetail.getPreviouslyKnownTerminationDate()).isNull();
        assertThat(probationStatusDetail.getInBreach()).isFalse();
        assertThat(probationStatusDetail.getAwaitingPsr()).isFalse();
        assertThat(probationStatusDetail.getPreSentenceActivity()).isTrue();
    }

    @Test
    public void canGetProbationStatusForPreviouslyKnownOffender() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        // PREVIOUSLY_KNOWN if currentDisposal is false and they have at least 1 event with a disposal
        when(offender.getCurrentDisposal()).thenReturn(0L);
        when(offender.getEvents()).thenReturn(List.of(event));
        when(offender.getActiveEvents()).thenReturn(List.of(event));
        when(event.getDisposal()).thenReturn(disposal);
        when(disposal.getTerminationDate()).thenReturn(LocalDate.of(2020, 1, 4));

        final var probationStatusDetail = convictionService.probationStatusFor(CRN).orElseThrow();

        assertThat(probationStatusDetail.getStatus()).isEqualTo(ProbationStatus.PREVIOUSLY_KNOWN);
        assertThat(probationStatusDetail.getPreviouslyKnownTerminationDate()).isEqualTo(LocalDate.of(2020, 1, 4));
        assertThat(probationStatusDetail.getInBreach()).isNull();
        assertThat(probationStatusDetail.getAwaitingPsr()).isFalse();
        assertThat(probationStatusDetail.getPreSentenceActivity()).isFalse();
    }

    @Test
    public void canGetProbationStatusForPreviouslyKnownOffenderWithPreSentenceActivity() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        // PREVIOUSLY_KNOWN if currentDisposal is false and they have at least 1 event with a disposal
        when(offender.getCurrentDisposal()).thenReturn(0L);
        when(offender.getActiveEvents()).thenReturn(List.of(event, event2));
        when(offender.getEvents()).thenReturn(List.of(event, event2));
        when(event.getDisposal()).thenReturn(disposal);
        when(disposal.getTerminationDate()).thenReturn(LocalDate.of(2020, 1, 4));
        // preSentenceActivity is true if they have an active event with no disposal
        when(event2.getDisposal()).thenReturn(null);
        when(event2.getCourtAppearances()).thenReturn(List.of(aCourtAppearanceWithOutcome("AS41", "Deferred")));

        final var probationStatusDetail = convictionService.probationStatusFor(CRN).orElseThrow();

        assertThat(probationStatusDetail.getStatus()).isEqualTo(ProbationStatus.PREVIOUSLY_KNOWN);
        assertThat(probationStatusDetail.getPreviouslyKnownTerminationDate()).isEqualTo(LocalDate.of(2020, 1, 4));
        assertThat(probationStatusDetail.getInBreach()).isNull();
        assertThat(probationStatusDetail.getAwaitingPsr()).isFalse();
        assertThat(probationStatusDetail.getPreSentenceActivity()).isTrue();
    }

    @Test
    public void canGetProbationStatusForOffenderWithNoSentenceAndPreSentenceActivity() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        when(offender.getCurrentDisposal()).thenReturn(0L);
        when(offender.getActiveEvents()).thenReturn(List.of(event));
        // preSentenceActivity is true if they have an active event with no disposal
        when(event.getDisposal()).thenReturn(null);

        final var probationStatusDetail = convictionService.probationStatusFor(CRN).orElseThrow();

        assertThat(probationStatusDetail.getStatus()).isEqualTo(ProbationStatus.NOT_SENTENCED);
        assertThat(probationStatusDetail.getPreviouslyKnownTerminationDate()).isNull();
        assertThat(probationStatusDetail.getInBreach()).isNull();
        assertThat(probationStatusDetail.getPreSentenceActivity()).isTrue();
        assertThat(probationStatusDetail.getAwaitingPsr()).isFalse();
    }

    @Test
    public void canGetProbationStatusForOffenderWithNoEvents() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        when(offender.getCurrentDisposal()).thenReturn(0L);
        when(offender.getActiveEvents()).thenReturn(Collections.emptyList());

        final var probationStatusDetail = convictionService.probationStatusFor(CRN).orElseThrow();

        assertThat(probationStatusDetail.getStatus()).isEqualTo(ProbationStatus.NOT_SENTENCED);
        assertThat(probationStatusDetail.getPreviouslyKnownTerminationDate()).isNull();
        assertThat(probationStatusDetail.getInBreach()).isNull();
        assertThat(probationStatusDetail.getAwaitingPsr()).isFalse();
        assertThat(probationStatusDetail.getPreSentenceActivity()).isFalse();
    }

    @Test
    public void returnEmptyForNonExistentOffender() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.empty());
        final var probationStatusDetail = convictionService.probationStatusFor(CRN);

        assertThat(probationStatusDetail).isEmpty();
    }
}
