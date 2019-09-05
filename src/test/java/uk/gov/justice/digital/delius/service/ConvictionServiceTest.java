package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.CourtCase;
import uk.gov.justice.digital.delius.data.api.OffenceDetail;
import uk.gov.justice.digital.delius.jpa.national.entity.User;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.transformers.*;

import java.time.LocalDate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@Import({ConvictionService.class, ConvictionTransformer.class, MainOffenceTransformer.class, AdditionalOffenceTransformer.class, CourtAppearanceTransformer.class, CourtReportTransformer.class, CourtTransformer.class, InstitutionTransformer.class})
public class ConvictionServiceTest {

    @Autowired
    private ConvictionService convictionService;

    @MockBean
    private EventRepository convictionRepository;

    @MockBean
    private LookupSupplier lookupSupplier;

    @MockBean
    private SpgNotificationService spgNotificationService;


    @Test
    public void convictionsOrderedByCreationDate() {
        Mockito.when(convictionRepository.findByOffenderId(1L))
                .thenReturn(ImmutableList.of(
                        aEvent().toBuilder().eventId(99L).referralDate(LocalDate.now().minusDays(1)).build(),
                        aEvent().toBuilder().eventId(9L).referralDate(LocalDate.now().minusDays(2)).build(),
                        aEvent().toBuilder().eventId(999L).referralDate(LocalDate.now()).build()
                ));

        assertThat(convictionService.convictionsFor(1L)
                .stream().map(uk.gov.justice.digital.delius.data.api.Conviction::getConvictionId)
                .collect(Collectors.toList()))
                .containsSequence(999L, 99L, 9L);

    }

    @Test
    public void deletedRecordsIgnored() {
        Mockito.when(convictionRepository.findByOffenderId(1L))
                .thenReturn(ImmutableList.of(
                        aEvent().toBuilder().eventId(1L).build(),
                        aEvent().toBuilder().eventId(2L).softDeleted(1L).build(),
                        aEvent().toBuilder().eventId(3L).build()
                ));

        assertThat(convictionService.convictionsFor(1L)
                .stream().map(uk.gov.justice.digital.delius.data.api.Conviction::getConvictionId)
                .collect(Collectors.toList()))
                .contains(1L, 3L);


    }

    @Test
    public void addingACourtCaseCreatesAConvictionEventWithNewIndexMatchingNumberOfConvictions() {
        when(lookupSupplier.userSupplier()).thenReturn(() -> User.builder().userId(99L).build());
        when(lookupSupplier.orderAllocationReasonSupplier()).thenReturn(code -> StandardReference.builder().codeValue(code).build());
        when(lookupSupplier.transferReasonSupplier()).thenReturn(code -> TransferReason.builder().code(code).build());
        when(lookupSupplier.probationAreaSupplier()).thenReturn(orderManager -> ProbationArea.builder().probationAreaId(orderManager.getProbationAreaId()).build());
        when(lookupSupplier.teamSupplier()).thenReturn(orderManager -> Team.builder().teamId(orderManager.getTeamId()).build());
        when(lookupSupplier.staffSupplier()).thenReturn(orderManager -> Staff.builder().staffId(orderManager.getOfficerId()).build());
        when(lookupSupplier.offenceSupplier()).thenReturn(code -> Offence.builder().code(code).ogrsOffenceCategory(StandardReference.builder().build()).build());
        when(lookupSupplier.courtSupplier()).thenReturn(courtId -> Court.builder().courtId(courtId).build());

        // echo back what we saved
        when(convictionRepository.save(any(Event.class))).thenAnswer((invocationOnMock -> invocationOnMock.getArgumentAt(0, Event.class)));
        when(convictionRepository.findByOffenderId(1L)).thenReturn(ImmutableList.of(aEvent(), aEvent()));

        final Conviction conviction = convictionService.addCourtCaseFor(
                1L,
                CourtCase
                        .builder()
                        .orderManager(uk.gov.justice.digital.delius.data.api.OrderManager
                                .builder()
                                .build())
                        .offences(ImmutableList.of(uk.gov.justice.digital.delius.data.api.Offence
                                .builder()
                                .detail(OffenceDetail
                                        .builder()
                                        .build())
                                .mainOffence(true)
                                .build()))
                        .courtAppearance(uk.gov.justice.digital.delius.data.api.CourtAppearance
                                .builder()
                                .court(uk.gov.justice.digital.delius.data.api.Court
                                        .builder()
                                        .build())
                                .build())
                        .build());

        assertThat(conviction).isNotNull();
        assertThat(conviction.getIndex()).isEqualTo("3");

        verify(spgNotificationService).notifyNewCourtCaseCreated(any(Event.class));
    }

    private Event aEvent() {
        return Event.builder()
                .referralDate(LocalDate.now())
                .additionalOffences(ImmutableList.of())
                .build();
    }

}