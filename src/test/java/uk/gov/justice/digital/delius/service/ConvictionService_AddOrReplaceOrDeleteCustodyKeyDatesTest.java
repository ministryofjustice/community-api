package uk.gov.justice.digital.delius.service;

import org.assertj.core.groups.Tuple;
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
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.entity.KeyDate;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.ConvictionTransformer;
import uk.gov.justice.digital.delius.transformers.CustodyKeyDateTransformer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.justice.digital.delius.util.EntityHelper.*;

@ExtendWith(MockitoExtension.class)
public class ConvictionService_AddOrReplaceOrDeleteCustodyKeyDatesTest {

    private ConvictionService convictionService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private OffenderRepository offenderRepository;

    @Mock
    private ConvictionTransformer convictionTransformer;

    @Mock
    private SpgNotificationService spgNotificationService;

    @Mock
    private IAPSNotificationService iapsNotificationService;

    @Mock
    private LookupSupplier lookupSupplier;

    @Mock
    private ContactService contactService;

    @Captor
    private ArgumentCaptor<Custody> custodyArgumentCaptor;

    @Captor
    private ArgumentCaptor<Map<String, LocalDate>> datesAmendedOrUpdatedArgumentCaptor;
    @Captor
    private ArgumentCaptor<Map<String, LocalDate>> datesRemovedArgumentCaptor;

    @BeforeEach
    public void setUp() {
        convictionService = new ConvictionService(true, eventRepository, offenderRepository, convictionTransformer, spgNotificationService, lookupSupplier, new CustodyKeyDateTransformer(lookupSupplier), iapsNotificationService, contactService);
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

        convictionService.addOrReplaceOrDeleteCustodyKeyDates(99L, 88L, ReplaceCustodyKeyDates
                .builder()
                .conditionalReleaseDate(LocalDate.of(2030, 1, 1))
                .build());

        verify(convictionTransformer).custodyOf(custodyArgumentCaptor.capture());

        final var custody = custodyArgumentCaptor.getValue();

        assertThat(custody.getKeyDates()).hasSize(1)
                .extracting( keyDate -> keyDate.getKeyDateType().getCodeValue(), KeyDate::getKeyDate)
                .containsExactlyInAnyOrder(
                        Tuple.tuple("ACR", LocalDate.of(2030, 1, 1)));
    }
    @Test
    void willLeaveAloneKeyDatesNotPresentButNotCustodyManagedDatesInRequestAndAddOnesThatArePresent() {
        when(eventRepository.findById(88L)).thenReturn(Optional.of(aCustodyEvent(88L, new ArrayList<>(List.of(
                aKeyDate("POM1", "POM", LocalDate.of(2039, 9, 30))
        )))));

        convictionService.addOrReplaceOrDeleteCustodyKeyDates(99L, 88L, ReplaceCustodyKeyDates
                .builder()
                .conditionalReleaseDate(LocalDate.of(2030, 1, 1))
                .build());

        verify(convictionTransformer).custodyOf(custodyArgumentCaptor.capture());

        final var custody = custodyArgumentCaptor.getValue();

        assertThat(custody.getKeyDates()).hasSize(2)
                .extracting( keyDate -> keyDate.getKeyDateType().getCodeValue(), KeyDate::getKeyDate)
                .containsExactlyInAnyOrder(
                        Tuple.tuple("POM1",  LocalDate.of(2039, 9, 30)),
                        Tuple.tuple("ACR", LocalDate.of(2030, 1, 1)));
    }
    @Test
    void willAddReplaceDeleteKeyDate() {
        when(eventRepository.findById(88L)).thenReturn(Optional.of(aCustodyEvent(88L, new ArrayList<>(List.of(
                aKeyDate("XX", "whatever", LocalDate.of(1995, 1, 1)),
                aKeyDate("LED", "licenceExpiryDate", LocalDate.of(2039, 9, 30)),
                aKeyDate("SED", "sentenceExpiryDate", LocalDate.of(2039, 9, 30))
        )))));

        convictionService.addOrReplaceOrDeleteCustodyKeyDates(99L, 88L, ReplaceCustodyKeyDates
                .builder()
                .conditionalReleaseDate(LocalDate.of(2030, 1, 1))
                .licenceExpiryDate(LocalDate.of(2030, 1, 2))
                .build());

        verify(convictionTransformer).custodyOf(custodyArgumentCaptor.capture());

        final var custody = custodyArgumentCaptor.getValue();

        assertThat(custody.getKeyDates()).hasSize(3)
                .extracting( keyDate -> keyDate.getKeyDateType().getCodeValue(), KeyDate::getKeyDate)
                .containsExactlyInAnyOrder(
                        Tuple.tuple("XX", LocalDate.of(1995, 1, 1)),
                        Tuple.tuple("LED", LocalDate.of(2030, 1, 2)),
                        Tuple.tuple("ACR", LocalDate.of(2030, 1, 1)));
    }
    @Test
    void willUpdateExistingKeyDates() {
        when(eventRepository.findById(88L)).thenReturn(Optional.of(aCustodyEvent(88L, new ArrayList<>(List.of(
                aKeyDate("SED", "sentenceExpiryDate", LocalDate.of(2039, 9, 30))
        )))));

        convictionService.addOrReplaceOrDeleteCustodyKeyDates(99L, 88L, ReplaceCustodyKeyDates
                .builder()
                .sentenceExpiryDate(LocalDate.of(2030, 1, 1))
                .build());

        verify(convictionTransformer).custodyOf(custodyArgumentCaptor.capture());

        final var custody = custodyArgumentCaptor.getValue();

        assertThat(custody.getKeyDates()).hasSize(1)
                .extracting( keyDate -> keyDate.getKeyDateType().getCodeValue(), KeyDate::getKeyDate)
                .containsExactlyInAnyOrder(
                        Tuple.tuple("SED",LocalDate.of(2030, 1, 1)));
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
