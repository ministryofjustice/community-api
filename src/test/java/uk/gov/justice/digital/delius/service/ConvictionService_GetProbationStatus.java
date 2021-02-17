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
public class ConvictionService_GetProbationStatus {

    private static final String CRN = "CRN";
    private static final long OFFENDER_ID = 123456L;

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

    @BeforeEach
    public void setUp(){
        final var featureSwitches = new FeatureSwitches();
        convictionService = new ConvictionService(eventRepository, offenderRepository, eventEntityBuilder, spgNotificationService, lookupSupplier, new KeyDateEntityBuilder(lookupSupplier), iapsNotificationService, contactService, telemetryClient, featureSwitches);
    }

    @Test
    public void canGetProbationStatusForCurrentOffender() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        when(offender.getOffenderId()).thenReturn(OFFENDER_ID);
        when(offender.getCurrentDisposal()).thenReturn(1L);
        when(offender.getEvents()).thenReturn(List.of(event));
        when(event.getActiveFlag()).thenReturn(1L);
        when(event.getInBreach()).thenReturn(1L);

        final var probationStatusDetail = convictionService.probationStatusFor(CRN).get();

        assertThat(probationStatusDetail.getProbationStatus()).isEqualTo(ProbationStatus.CURRENT);
        assertThat(probationStatusDetail.getPreviouslyKnownTerminationDate()).isNull();
        assertThat(probationStatusDetail.getInBreach()).isEqualTo(true);
        assertThat(probationStatusDetail.getPreSentenceActivity()).isEqualTo(false);
    }

    @Test
    public void canGetProbationStatusForCurrentOffenderWithMultipleEventsInBreach() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        when(offender.getOffenderId()).thenReturn(OFFENDER_ID);
        when(offender.getCurrentDisposal()).thenReturn(1L);
        when(offender.getEvents()).thenReturn(List.of(event, event2, event3));
        when(event.getActiveFlag()).thenReturn(1L);
        when(event.getInBreach()).thenReturn(0L);
        when(event2.getActiveFlag()).thenReturn(1L);
        when(event2.getInBreach()).thenReturn(1L);
        when(event3.getActiveFlag()).thenReturn(0L);
        when(event3.getInBreach()).thenReturn(0L);

        final var probationStatusDetail = convictionService.probationStatusFor(CRN).get();

        assertThat(probationStatusDetail.getProbationStatus()).isEqualTo(ProbationStatus.CURRENT);
        assertThat(probationStatusDetail.getPreviouslyKnownTerminationDate()).isNull();
        assertThat(probationStatusDetail.getInBreach()).isEqualTo(true);
        assertThat(probationStatusDetail.getPreSentenceActivity()).isEqualTo(false);
    }

    @Test
    public void canGetProbationStatusForPreviouslyKnownOffender() {
        final var probationStatusDetail = convictionService.probationStatusFor(CRN).get();

        assertThat(probationStatusDetail.getProbationStatus()).isEqualTo(ProbationStatus.PREVIOUSLY_KNOWN);
        assertThat(probationStatusDetail.getPreviouslyKnownTerminationDate()).isEqualTo(LocalDate.of(2020, 1, 4));
        assertThat(probationStatusDetail.getInBreach()).isNull();
        assertThat(probationStatusDetail.getPreSentenceActivity()).isEqualTo(false);
    }

    @Test
    public void canGetProbationStatusForOffenderWithPreSentenceActivity() {
        final var probationStatusDetail = convictionService.probationStatusFor(CRN).get();

        assertThat(probationStatusDetail.getProbationStatus()).isEqualTo(ProbationStatus.NOT_SENTENCED);
        assertThat(probationStatusDetail.getPreviouslyKnownTerminationDate()).isNull();
        assertThat(probationStatusDetail.getInBreach()).isNull();
        assertThat(probationStatusDetail.getPreSentenceActivity()).isEqualTo(true);
    }

    @Test
    public void canGetProbationStatusForOffenderWithNoEvents() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(offender));
        when(offender.getOffenderId()).thenReturn(OFFENDER_ID);
        when(offender.getCurrentDisposal()).thenReturn(0L);
        when(convictionService.convictionsFor(OFFENDER_ID)).thenReturn(Collections.emptyList());

        final var probationStatusDetail = convictionService.probationStatusFor(CRN).get();

        assertThat(probationStatusDetail.getProbationStatus()).isEqualTo(ProbationStatus.NOT_SENTENCED);
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
