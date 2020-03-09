package uk.gov.justice.digital.delius.service;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.ReplaceCustodyKeyDates;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.entity.KeyDate;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.transformers.ConvictionTransformer;
import uk.gov.justice.digital.delius.transformers.CustodyKeyDateTransformer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.aCustodyEvent;
import static uk.gov.justice.digital.delius.util.EntityHelper.aKeyDate;

@ExtendWith(MockitoExtension.class)
public class ConvictionService_AddOrReplaceOrDeleteCustodyKeyDatesTest {

    private ConvictionService convictionService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ConvictionTransformer convictionTransformer;

    @Mock
    private SpgNotificationService spgNotificationService;

    @Mock
    private IAPSNotificationService iapsNotificationService;

    @Mock
    private LookupSupplier lookupSupplier;

    @Captor
    private ArgumentCaptor<Custody> custodyArgumentCaptor;

    @BeforeEach
    public void setUp() {
        convictionService = new ConvictionService(true, eventRepository, convictionTransformer, spgNotificationService, lookupSupplier, new CustodyKeyDateTransformer(lookupSupplier), iapsNotificationService);
        when(lookupSupplier.userSupplier()).thenReturn(() -> User.builder().userId(88L).build());
        when(lookupSupplier.custodyKeyDateTypeSupplier()).thenReturn(code -> Optional.of(StandardReference
                .builder()
                .codeDescription("description")
                .codeValue(code)
                .build()));
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

}
