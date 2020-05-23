package uk.gov.justice.digital.delius.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.justice.digital.delius.data.api.ReplaceCustodyKeyDates;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.EventTransformer;
import uk.gov.justice.digital.delius.transformers.CustodyKeyDateTransformer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.aCustodyEvent;
import static uk.gov.justice.digital.delius.util.EntityHelper.aKeyDate;
import static uk.gov.justice.digital.delius.util.EntityHelper.anOffender;

@ExtendWith(MockitoExtension.class)
public class ConvictionService_AddOrReplaceOrDeleteCustodyKeyDatesTest {

    private ConvictionService convictionService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private OffenderRepository offenderRepository;

    @Mock
    private EventTransformer eventTransformer;

    @Mock
    private SpgNotificationService spgNotificationService;

    @Mock
    private IAPSNotificationService iapsNotificationService;

    @Mock
    private LookupSupplier lookupSupplier;

    @Mock
    private ContactService contactService;

    @Captor
    private ArgumentCaptor<Map<String, LocalDate>> datesAmendedOrUpdatedArgumentCaptor;
    @Captor
    private ArgumentCaptor<Map<String, LocalDate>> datesRemovedArgumentCaptor;

    @BeforeEach
    public void setUp() {
        convictionService = new ConvictionService(true, eventRepository, offenderRepository, eventTransformer, spgNotificationService, lookupSupplier, new CustodyKeyDateTransformer(lookupSupplier), iapsNotificationService, contactService);
        when(lookupSupplier.userSupplier()).thenReturn(() -> User.builder().userId(88L).build());
        when(lookupSupplier.custodyKeyDateTypeSupplier()).thenReturn(code -> Optional.of(StandardReference
                .builder()
                .codeDescription("description")
                .codeValue(code)
                .build()));
        when(offenderRepository.findByOffenderId(anyLong())).thenReturn(Optional.of(anOffender()));
    }

    @Test
    void willDeleteKeyDatesNotPresentInRequestAndAddOnesThatArePresent() {
        when(eventRepository.findById(88L)).thenReturn(Optional.of(aCustodyEvent(88L, new ArrayList<>(List.of(
                aKeyDate("LED", "licenceExpiryDate", LocalDate.of(2039, 9, 30))
        )))));

        final var custody = convictionService.addOrReplaceOrDeleteCustodyKeyDates(99L, 88L, ReplaceCustodyKeyDates
                .builder()
                .conditionalReleaseDate(LocalDate.of(2030, 1, 1))
                .build());

        assertThat(custody.getKeyDates().getLicenceExpiryDate()).isNull();
        assertThat(custody.getKeyDates().getConditionalReleaseDate()).isEqualTo(LocalDate.of(2030, 1, 1));
    }
    @Test
    void willLeaveAloneKeyDatesNotPresentButNotCustodyManagedDatesInRequestAndAddOnesThatArePresent() {
        when(eventRepository.findById(88L)).thenReturn(Optional.of(aCustodyEvent(88L, new ArrayList<>(List.of(
                aKeyDate("POM1", "POM", LocalDate.of(2039, 9, 30))
        )))));

        final var custody = convictionService.addOrReplaceOrDeleteCustodyKeyDates(99L, 88L, ReplaceCustodyKeyDates
                .builder()
                .conditionalReleaseDate(LocalDate.of(2030, 1, 1))
                .build());

        assertThat(custody.getKeyDates().getConditionalReleaseDate()).isEqualTo(LocalDate.of(2030, 1, 1));
        assertThat(custody.getKeyDates().getExpectedPrisonOffenderManagerHandoverStartDate()).isEqualTo(LocalDate.of(2039, 9, 30));

    }
    @Test
    void willAddReplaceDeleteKeyDate() {
        when(eventRepository.findById(88L)).thenReturn(Optional.of(aCustodyEvent(88L, new ArrayList<>(List.of(
                aKeyDate("XX", "whatever", LocalDate.of(1995, 1, 1)),
                aKeyDate("LED", "licenceExpiryDate", LocalDate.of(2039, 9, 30)),
                aKeyDate("SED", "sentenceExpiryDate", LocalDate.of(2039, 9, 30))
        )))));

        final var custody = convictionService.addOrReplaceOrDeleteCustodyKeyDates(99L, 88L, ReplaceCustodyKeyDates
                .builder()
                .conditionalReleaseDate(LocalDate.of(2030, 1, 1))
                .licenceExpiryDate(LocalDate.of(2030, 1, 2))
                .build());

        assertThat(custody.getKeyDates().getConditionalReleaseDate()).isEqualTo(LocalDate.of(2030, 1, 1));
        assertThat(custody.getKeyDates().getLicenceExpiryDate()).isEqualTo(LocalDate.of(2030, 1, 2));

    }
    @Test
    void willUpdateExistingKeyDates() {
        when(eventRepository.findById(88L)).thenReturn(Optional.of(aCustodyEvent(88L, new ArrayList<>(List.of(
                aKeyDate("SED", "sentenceExpiryDate", LocalDate.of(2039, 9, 30))
        )))));

        final var custody = convictionService.addOrReplaceOrDeleteCustodyKeyDates(99L, 88L, ReplaceCustodyKeyDates
                .builder()
                .sentenceExpiryDate(LocalDate.of(2030, 1, 1))
                .build());

        assertThat(custody.getKeyDates().getSentenceExpiryDate()).isEqualTo(LocalDate.of(2030, 1, 1));
    }

    @Test
    void willNotNotifyIAPSWhenSentenceExpiryDateChangesUpdateExistingKeyDates() {
        when(eventRepository.findById(88L)).thenReturn(Optional.of(aCustodyEvent(88L, new ArrayList<>(List.of(
                aKeyDate("SED", "sentenceExpiryDate", LocalDate.of(2039, 9, 30))
        )))));

        convictionService.addOrReplaceOrDeleteCustodyKeyDates(99L, 88L, ReplaceCustodyKeyDates
                .builder()
                .sentenceExpiryDate(LocalDate.of(2030, 1, 1))
                .build());

        verify(iapsNotificationService, never()).notifyEventUpdated(any());
    }

    @Test
    void willNotNotifyIAPSWhenSentenceExpiryDateChangesDeletesExistingKeyDate() {
        when(eventRepository.findById(88L)).thenReturn(Optional.of(aCustodyEvent(88L, new ArrayList<>(List.of(
                aKeyDate("SED", "sentenceExpiryDate", LocalDate.now())
        )))));

        convictionService.addOrReplaceOrDeleteCustodyKeyDates(99L, 88L, ReplaceCustodyKeyDates
                .builder()
                .paroleEligibilityDate(LocalDate.now())
                .build());

        verify(iapsNotificationService, never()).notifyEventUpdated(any());
    }

    @Test
    void willAddContactWithUpdatedRemovedAndDeletedDates() {
        final var event = aCustodyEvent(88L, new ArrayList<>(List.of(
                aKeyDate("XX", "whatever", LocalDate.of(1995, 1, 1)),
                aKeyDate("LED", "licenceExpiryDate", LocalDate.of(2039, 9, 30)),
                aKeyDate("SED", "sentenceExpiryDate", LocalDate.of(2039, 9, 30)),
                aKeyDate("PED", "paroleEligibilityDate", LocalDate.of(2030, 1, 4)),
                aKeyDate("PSSED", "pssEndDate", LocalDate.of(2039, 8, 30))
        )));

        final var offender = anOffender();

        when(offenderRepository.findByOffenderId(anyLong())).thenReturn(Optional.of(offender));
        when(eventRepository.findById(88L)).thenReturn(Optional.of(event));

        convictionService.addOrReplaceOrDeleteCustodyKeyDates(99L, 88L, ReplaceCustodyKeyDates
                .builder()
                .conditionalReleaseDate(LocalDate.of(2030, 1, 1))
                .licenceExpiryDate(LocalDate.of(2030, 1, 2))
                .hdcEligibilityDate(LocalDate.of(2030, 1, 3))
                .paroleEligibilityDate(LocalDate.of(2030, 1, 4))
                .expectedReleaseDate(LocalDate.of(2030, 1, 5))
                .build());


        verify(contactService).addContactForBulkCustodyKeyDateUpdate(eq(offender), eq(event), datesAmendedOrUpdatedArgumentCaptor.capture(), datesRemovedArgumentCaptor
                .capture());

        assertThat(datesAmendedOrUpdatedArgumentCaptor.getValue())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                // all new and updated
                "Conditional Release Date", LocalDate.of(2030, 1, 1),
                "Licence Expiry Date", LocalDate.of(2030, 1, 2),
                "HDC Eligibility Date", LocalDate.of(2030, 1, 3),
                "Expected Release Date", LocalDate.of(2030, 1, 5)
        ))      // not changed - so not present
                .doesNotContain(Map.entry("Parole Eligibility Date", LocalDate.of(2030, 1, 4)));

        assertThat(datesRemovedArgumentCaptor.getValue()).containsExactlyInAnyOrderEntriesOf(Map.of(
                "Sentence Expiry Date", LocalDate.of(2039, 9, 30),
                "PSS End Date", LocalDate.of(2039, 8, 30)
        ));
    }
    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void willDoNothingWhenNoDatesHaveChanged() {
        final var event = aCustodyEvent(88L, new ArrayList<>(List.of(
                aKeyDate("XX", "whatever", LocalDate.of(1995, 1, 1)),
                aKeyDate("LED", "licenceExpiryDate", LocalDate.of(2039, 9, 30)),
                aKeyDate("SED", "sentenceExpiryDate", LocalDate.of(2039, 9, 30)),
                aKeyDate("PED", "paroleEligibilityDate", LocalDate.of(2030, 1, 4)),
                aKeyDate("PSSED", "pssEndDate", LocalDate.of(2039, 8, 30))
        )));

        when(eventRepository.findById(88L)).thenReturn(Optional.of(event));

        convictionService.addOrReplaceOrDeleteCustodyKeyDates(99L, 88L, ReplaceCustodyKeyDates
                .builder()
                .licenceExpiryDate(LocalDate.of(2039, 9, 30))
                .sentenceExpiryDate(LocalDate.of(2039, 9, 30))
                .paroleEligibilityDate(LocalDate.of(2030, 1, 4))
                .postSentenceSupervisionEndDate(LocalDate.of(2039, 8, 30))
                .build());


        verify(contactService, never()).addContactForBulkCustodyKeyDateUpdate(any(), any(), any(), any());
        verify(lookupSupplier, never()).userSupplier();
        verify(lookupSupplier, never()).custodyKeyDateTypeSupplier();
        verify(eventRepository, never()).save(any());
    }

}
