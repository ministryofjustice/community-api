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
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

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
        when(offender.getEvents()).thenReturn(List.of(event));
        when(offender.getSoftDeleted()).thenReturn(0L);
        // Event in breach flag used to determine if offender is in breach
        when(event.getSoftDeleted()).thenReturn(0L);
        when(event.getActiveFlag()).thenReturn(1L);
        when(event.getDisposal()).thenReturn(Disposal.builder().build());
        when(event.getInBreach()).thenReturn(1L);

        final var probationStatusDetail = convictionService.probationStatusFor(CRN).get();

        assertThat(probationStatusDetail.getStatus()).isEqualTo(ProbationStatus.CURRENT);
        assertThat(probationStatusDetail.getPreviouslyKnownTerminationDate()).isNull();
        assertThat(probationStatusDetail.getInBreach()).isEqualTo(true);
        assertThat(probationStatusDetail.getPreSentenceActivity()).isEqualTo(false);
    }

    @Test
    public void canGetProbationStatusForCurrentOffenderWithMultipleCurrentEventsInBreach() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        // If current disposal == 1L then probation status is CURRENT
        when(offender.getCurrentDisposal()).thenReturn(1L);
        when(offender.getEvents()).thenReturn(List.of(event, event2, event3));
        when(offender.getSoftDeleted()).thenReturn(0L);
        // If any active event is in breach then offender is in breach
        when(event.getSoftDeleted()).thenReturn(0L);
        when(event.getActiveFlag()).thenReturn(1L);
        when(event.getInBreach()).thenReturn(0L);
        when(event2.getSoftDeleted()).thenReturn(0L);
        when(event2.getActiveFlag()).thenReturn(1L);
        when(event2.getInBreach()).thenReturn(1L);

        final var probationStatusDetail = convictionService.probationStatusFor(CRN).get();

        assertThat(probationStatusDetail.getStatus()).isEqualTo(ProbationStatus.CURRENT);
        assertThat(probationStatusDetail.getPreviouslyKnownTerminationDate()).isNull();
        assertThat(probationStatusDetail.getInBreach()).isEqualTo(true);
        assertThat(probationStatusDetail.getPreSentenceActivity()).isEqualTo(true);
    }

    @Test
    public void canGetProbationStatusForCurrentOffenderWithNoCurrentBreach() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        // If current disposal == 1L then probation status is CURRENT
        when(offender.getSoftDeleted()).thenReturn(0L);
        when(offender.getCurrentDisposal()).thenReturn(1L);
        when(offender.getEvents()).thenReturn(List.of(event, event2, event3));
        // If no active event is in breach then offender is not in breach
        when(event.getSoftDeleted()).thenReturn(0L);
        when(event.getActiveFlag()).thenReturn(1L);
        when(event.getInBreach()).thenReturn(0L);

        // Don't check deleted
        when(event2.getSoftDeleted()).thenReturn(1L);
        // Don't check inactive
        when(event3.getSoftDeleted()).thenReturn(0L);
        when(event3.getActiveFlag()).thenReturn(0L);

        final var probationStatusDetail = convictionService.probationStatusFor(CRN).get();

        assertThat(probationStatusDetail.getStatus()).isEqualTo(ProbationStatus.CURRENT);
        assertThat(probationStatusDetail.getPreviouslyKnownTerminationDate()).isNull();
        assertThat(probationStatusDetail.getInBreach()).isEqualTo(false);
        assertThat(probationStatusDetail.getPreSentenceActivity()).isEqualTo(true);
    }

    @Test
    public void canGetProbationStatusForPreviouslyKnownOffender() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        // PREVIOUSLY_KNOWN if currentDisposal is false and they have at least 1 event with a disposal
        when(offender.getCurrentDisposal()).thenReturn(0L);
        when(offender.getEvents()).thenReturn(List.of(event));
        when(event.getDisposal()).thenReturn(disposal);
        when(disposal.getTerminationDate()).thenReturn(LocalDate.of(2020, 1, 4));

        final var probationStatusDetail = convictionService.probationStatusFor(CRN).get();

        assertThat(probationStatusDetail.getStatus()).isEqualTo(ProbationStatus.PREVIOUSLY_KNOWN);
        assertThat(probationStatusDetail.getPreviouslyKnownTerminationDate()).isEqualTo(LocalDate.of(2020, 1, 4));
        assertThat(probationStatusDetail.getInBreach()).isNull();
        assertThat(probationStatusDetail.getPreSentenceActivity()).isEqualTo(false);
    }

    @Test
    public void canGetProbationStatusForPreviouslyKnownOffenderWithPreSentenceActivity() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        // PREVIOUSLY_KNOWN if currentDisposal is false and they have at least 1 event with a disposal
        when(offender.getCurrentDisposal()).thenReturn(0L);
        when(offender.getEvents()).thenReturn(List.of(event, event2));
        when(event.getDisposal()).thenReturn(disposal);
        when(disposal.getTerminationDate()).thenReturn(LocalDate.of(2020, 1, 4));
        // preSentenceActivity is true if they have an active event with no disposal
        when(event2.getDisposal()).thenReturn(null);

        final var probationStatusDetail = convictionService.probationStatusFor(CRN).get();

        assertThat(probationStatusDetail.getStatus()).isEqualTo(ProbationStatus.PREVIOUSLY_KNOWN);
        assertThat(probationStatusDetail.getPreviouslyKnownTerminationDate()).isEqualTo(LocalDate.of(2020, 1, 4));
        assertThat(probationStatusDetail.getInBreach()).isNull();
        assertThat(probationStatusDetail.getPreSentenceActivity()).isEqualTo(true);
    }

    @Test
    public void canGetProbationStatusForOffenderWithNoSentenceAndPreSentenceActivity() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        when(offender.getCurrentDisposal()).thenReturn(0L);
        when(offender.getEvents()).thenReturn(List.of(event));
        // preSentenceActivity is true if they have an active event with no disposal
        when(event.getDisposal()).thenReturn(null);

        final var probationStatusDetail = convictionService.probationStatusFor(CRN).get();

        assertThat(probationStatusDetail.getStatus()).isEqualTo(ProbationStatus.NOT_SENTENCED);
        assertThat(probationStatusDetail.getPreviouslyKnownTerminationDate()).isNull();
        assertThat(probationStatusDetail.getInBreach()).isNull();
        assertThat(probationStatusDetail.getPreSentenceActivity()).isEqualTo(true);
    }

    @Test
    public void canGetProbationStatusForOffenderWithNoEvents() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        when(offender.getCurrentDisposal()).thenReturn(0L);
        when(offender.getEvents()).thenReturn(Collections.emptyList());

        final var probationStatusDetail = convictionService.probationStatusFor(CRN).get();

        assertThat(probationStatusDetail.getStatus()).isEqualTo(ProbationStatus.NOT_SENTENCED);
        assertThat(probationStatusDetail.getPreviouslyKnownTerminationDate()).isNull();
        assertThat(probationStatusDetail.getInBreach()).isNull();
        assertThat(probationStatusDetail.getPreSentenceActivity()).isEqualTo(false);
    }

    @Test
    public void returnEmptyForNonExistentOffender() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.empty());
        final var probationStatusDetail = convictionService.probationStatusFor(CRN);

        assertThat(probationStatusDetail).isEmpty();
    }
}
