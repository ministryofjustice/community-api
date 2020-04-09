package uk.gov.justice.digital.delius.service;

import com.google.common.collect.ImmutableList;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalIdentifier;
import uk.gov.justice.digital.delius.jpa.standard.entity.BusinessInteraction;
import uk.gov.justice.digital.delius.jpa.standard.entity.BusinessInteractionXmlMap;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtAppearance;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;
import uk.gov.justice.digital.delius.jpa.standard.entity.SpgNotification;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.BusinessInteractionRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.BusinessInteractionXmlMapRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ProbationAreaRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.SpgNotificationHelperRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.SpgNotificationRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StandardReferenceRepository;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.digital.delius.util.EntityHelper.aCustodyEvent;
import static uk.gov.justice.digital.delius.util.EntityHelper.aKeyDate;
import static uk.gov.justice.digital.delius.util.EntityHelper.anOffender;

@ExtendWith(MockitoExtension.class)
public class SpgNotificationServiceTest {
    @Mock
    private BusinessInteractionRepository businessInteractionRepository;
    @Mock
    private BusinessInteractionXmlMapRepository businessInteractionXmlMapRepository;
    @Mock
    private StandardReferenceRepository standardReferenceRepository;
    @Mock
    private ProbationAreaRepository probationAreaRepository;
    @Mock
    private SpgNotificationRepository spgNotificationRepository;
    @Mock
    private SpgNotificationHelperRepository spgNotificationHelperRepository;

    @Captor
    private ArgumentCaptor<List<SpgNotification>> spgNotificationsCaptor;

    private SpgNotificationService spgNotificationService;


    @BeforeEach
    public void before() {
        when(businessInteractionRepository.findByBusinessInteractionCode(any())).thenAnswer(
                params -> Optional.of(
                        BusinessInteraction
                                .builder()
                                .businessInteractionCode(params.getArguments()[0].toString()) // echo back the code looked for
                                .build()));
        when(businessInteractionXmlMapRepository.findByBusinessInteractionId(any())).thenReturn(Optional.of(BusinessInteractionXmlMap
                .builder()
                .dataUpdateMode("I")
                .build()));
        when(standardReferenceRepository.findByCodeAndCodeSetName(any(), any())).thenReturn(Optional.of(StandardReference
                .builder()
                .standardReferenceListId(88L)
                .build()));
        when(probationAreaRepository.findByCode(any())).thenReturn(Optional.of(ProbationArea.builder().build()));
        when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of());
        when(spgNotificationHelperRepository.getNextControlSequence(any())).thenReturn(1L);

        spgNotificationService = new SpgNotificationService(businessInteractionRepository, businessInteractionXmlMapRepository, standardReferenceRepository, probationAreaRepository, spgNotificationRepository, spgNotificationHelperRepository);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    public void whenNoInterestedCRCsThenNothingIsSaved() {
        when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of());

        spgNotificationService.notifyNewCourtCaseCreated(Event
                .builder()
                .offenderId(99L)
                .courtAppearances(ImmutableList.of(
                        CourtAppearance.builder().build(),
                        CourtAppearance.builder().build()
                        ))
                .build());

        verify(spgNotificationHelperRepository, atLeastOnce()).getInterestedCRCs("99");
        verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

        spgNotificationsCaptor.getAllValues().forEach(notificationsSaved -> assertThat(notificationsSaved).isEmpty());
    }

    @Test
    public void withOneInterestedCRCOffenderUpdateNotificationIsSaved() {
        when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of(ProbationArea
                .builder()
                .code("AA")
                .build()));

        spgNotificationService.notifyNewCourtCaseCreated(Event
                .builder()
                .offenderId(99L)
                .courtAppearances(ImmutableList.of(
                        CourtAppearance.builder().build(),
                        CourtAppearance.builder().build()
                ))
                .build());

        verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

        assertThat(findFor(SpgNotificationService.NotificationEvents.UPDATE_OFFENDER.getNotificationCode())).hasSize(1);
    }

    @Test
    public void withManyInterestedCRCsOffenderUpdateNotificationIsSavedForEachProbationArea() {
        when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of(
                ProbationArea
                        .builder()
                        .code("AA")
                        .build(),
                ProbationArea
                        .builder()
                        .code("AB")
                        .build()));

        spgNotificationService.notifyNewCourtCaseCreated(Event
                .builder()
                .offenderId(99L)
                .courtAppearances(ImmutableList.of(
                        CourtAppearance.builder().build(),
                        CourtAppearance.builder().build()
                ))
                .build());

        verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

        assertThat(findFor(SpgNotificationService.NotificationEvents.UPDATE_OFFENDER.getNotificationCode())).hasSize(2);
        assertThat(findFor(SpgNotificationService.NotificationEvents.UPDATE_OFFENDER.getNotificationCode()).stream().anyMatch(notification -> notification.getReceiverIdentity().getCode().equals("AA"))).isTrue();
        assertThat(findFor(SpgNotificationService.NotificationEvents.UPDATE_OFFENDER.getNotificationCode()).stream().anyMatch(notification -> notification.getReceiverIdentity().getCode().equals("AB"))).isTrue();
    }


    @Test
    public void withOneInterestedCRCInsertEventNotificationIsSaved() {
        when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of(ProbationArea
                .builder()
                .code("AA")
                .build()));

        spgNotificationService.notifyNewCourtCaseCreated(Event
                .builder()
                .offenderId(99L)
                .courtAppearances(ImmutableList.of(
                        CourtAppearance.builder().build(),
                        CourtAppearance.builder().build()
                ))
                .build());

        verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

        assertThat(findFor(SpgNotificationService.NotificationEvents.INSERT_EVENT.getNotificationCode())).hasSize(1);
    }

    @Test
    public void withManyInterestedCRCsInsertEventNotificationIsSavedForEachProbationArea() {
        when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of(
                ProbationArea
                    .builder()
                    .code("AA")
                    .build(),
                ProbationArea
                    .builder()
                    .code("AB")
                    .build()));

        spgNotificationService.notifyNewCourtCaseCreated(Event
                .builder()
                .offenderId(99L)
                .courtAppearances(ImmutableList.of(
                        CourtAppearance.builder().build(),
                        CourtAppearance.builder().build()
                ))
                .build());

        verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

        assertThat(findFor(SpgNotificationService.NotificationEvents.INSERT_EVENT.getNotificationCode())).hasSize(2);
        assertThat(findFor(SpgNotificationService.NotificationEvents.INSERT_EVENT.getNotificationCode()).stream().anyMatch(notification -> notification.getReceiverIdentity().getCode().equals("AA"))).isTrue();
        assertThat(findFor(SpgNotificationService.NotificationEvents.INSERT_EVENT.getNotificationCode()).stream().anyMatch(notification -> notification.getReceiverIdentity().getCode().equals("AB"))).isTrue();
    }

    @Test
    public void withOneInterestedCRCInsertCourtAppearanceNotificationIsSavedForEachCourtAppearance() {
        when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of(ProbationArea
                .builder()
                .code("AA")
                .build()));

        spgNotificationService.notifyNewCourtCaseCreated(Event
                .builder()
                .offenderId(99L)
                .courtAppearances(ImmutableList.of(
                        CourtAppearance.builder().build(),
                        CourtAppearance.builder().build()
                ))
                .build());

        verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

        assertThat(findFor(SpgNotificationService.NotificationEvents.INSERT_COURT_APPEARANCE.getNotificationCode())).hasSize(2);
    }

    @Test
    public void withManyInterestedCRCsInsertCourtAppearanceNotificationIsSavedForEachProbationAreaAndForEachCourtAppearance() {
        when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of(
                ProbationArea
                        .builder()
                        .code("AA")
                        .build(),
                ProbationArea
                        .builder()
                        .code("AB")
                        .build()));

        spgNotificationService.notifyNewCourtCaseCreated(Event
                .builder()
                .offenderId(99L)
                .courtAppearances(ImmutableList.of(
                        CourtAppearance.builder().courtAppearanceId(20L).build(),
                        CourtAppearance.builder().courtAppearanceId(21L).build()
                ))
                .build());

        verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

        assertThat(findFor(SpgNotificationService.NotificationEvents.INSERT_COURT_APPEARANCE.getNotificationCode())).hasSize(4);

        assertThat(findFor(SpgNotificationService.NotificationEvents.INSERT_COURT_APPEARANCE.getNotificationCode()).stream()
                .anyMatch(notification ->
                        notification.getReceiverIdentity().getCode().equals("AA") && notification.getUniqueId() == 20L)).isTrue();
        assertThat(findFor(SpgNotificationService.NotificationEvents.INSERT_COURT_APPEARANCE.getNotificationCode())
                .stream().anyMatch(notification ->
                        notification.getReceiverIdentity().getCode().equals("AA") && notification.getUniqueId() == 21L)).isTrue();
        assertThat(findFor(SpgNotificationService.NotificationEvents.INSERT_COURT_APPEARANCE.getNotificationCode())
                .stream().anyMatch(notification ->
                        notification.getReceiverIdentity().getCode().equals("AB") && notification.getUniqueId() == 20L)).isTrue();
        assertThat(findFor(SpgNotificationService.NotificationEvents.INSERT_COURT_APPEARANCE.getNotificationCode())
                .stream().anyMatch(notification ->
                        notification.getReceiverIdentity().getCode().equals("AB") && notification.getUniqueId() == 21L)).isTrue();
    }


    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    public void withNoInterestedCRCsNothingInsertedForNewCustodyKeyDateNotification() {
        when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of());

        val event = aCustodyEvent(99L, ImmutableList.of(aKeyDate(88L, "POM1")));
        spgNotificationService.notifyNewCustodyKeyDate("POM1", event);

        verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

        assertThat(findFor(SpgNotificationService.NotificationEvents.INSERT_CUSTODY_KEY_DATE.getNotificationCode())).isEmpty();
    }

    @Test
    public void withOneInterestedCRCInsertCustodyKeyDateNotificationIsSaved() {
        when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of(ProbationArea
                .builder()
                .code("AA")
                .build()));

        val event = aCustodyEvent(99L, ImmutableList.of(aKeyDate(88L, "POM1")));
        spgNotificationService.notifyNewCustodyKeyDate("POM1", event);

        verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

        assertThat(findFor(SpgNotificationService.NotificationEvents.INSERT_CUSTODY_KEY_DATE.getNotificationCode())).hasSize(1);
    }

    @Test
    public void keyInformationAboutTheInsertedCustodyKeyDateNotificationIsSaved() {
        when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of(ProbationArea
                .builder()
                .code("AA")
                .build()));

        val event = aCustodyEvent(99L, 77L, ImmutableList.of(aKeyDate(88L,"POM1")));
        spgNotificationService.notifyNewCustodyKeyDate("POM1", event);

        verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

        val notification = findFor(SpgNotificationService.NotificationEvents.INSERT_CUSTODY_KEY_DATE.getNotificationCode()).get(0);
        assertThat(notification.getUniqueId()).isEqualTo(88L);
        assertThat(notification.getOffenderId()).isEqualTo(77L);
        assertThat(notification.getParentEntityId()).isEqualTo(99L);
    }

    @Test
    public void withManyInterestedCRCsInsertCustodyKeyDateNotificationIsSavedForEachProbationArea() {
        when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of(
                ProbationArea
                        .builder()
                        .code("AA")
                        .build(),
                ProbationArea
                        .builder()
                        .code("AB")
                        .build()));

        val event = aCustodyEvent(99L, ImmutableList.of(aKeyDate(88L, "POM1")));
        spgNotificationService.notifyNewCustodyKeyDate("POM1", event);

        verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

        assertThat(findFor(SpgNotificationService.NotificationEvents.INSERT_CUSTODY_KEY_DATE.getNotificationCode())).hasSize(2);
        assertThat(findFor(SpgNotificationService.NotificationEvents.INSERT_CUSTODY_KEY_DATE.getNotificationCode()).stream().anyMatch(notification -> notification.getReceiverIdentity().getCode().equals("AA"))).isTrue();
        assertThat(findFor(SpgNotificationService.NotificationEvents.INSERT_CUSTODY_KEY_DATE.getNotificationCode()).stream().anyMatch(notification -> notification.getReceiverIdentity().getCode().equals("AB"))).isTrue();
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    public void withNoInterestedCRCsNothingInsertedForUpdatedCustodyKeyDateNotification() {
        when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of());

        val event = aCustodyEvent(99L, ImmutableList.of(aKeyDate(88L, "POM1")));
        spgNotificationService.notifyUpdateOfCustodyKeyDate("POM1", event);

        verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

        assertThat(findFor(SpgNotificationService.NotificationEvents.UPDATE_CUSTODY_KEY_DATE.getNotificationCode())).isEmpty();
    }


    @Test
    public void withOneInterestedCRCUpdatedCustodyKeyDateNotificationIsSaved() {
        when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of(ProbationArea
                .builder()
                .code("AA")
                .build()));

        val event = aCustodyEvent(99L, ImmutableList.of(aKeyDate(88L, "POM1")));
        spgNotificationService.notifyUpdateOfCustodyKeyDate("POM1", event);

        verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

        assertThat(findFor(SpgNotificationService.NotificationEvents.UPDATE_CUSTODY_KEY_DATE.getNotificationCode())).hasSize(1);
    }

    @Test
    public void keyInformationAboutTheUpdatedCustodyKeyDateNotificationIsSaved() {
        when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of(ProbationArea
                .builder()
                .code("AA")
                .build()));

        val event = aCustodyEvent(99L, 77L, ImmutableList.of(aKeyDate(88L,"POM1")));
        spgNotificationService.notifyUpdateOfCustodyKeyDate("POM1", event);

        verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

        val notification = findFor(SpgNotificationService.NotificationEvents.UPDATE_CUSTODY_KEY_DATE.getNotificationCode()).get(0);
        assertThat(notification.getUniqueId()).isEqualTo(88L);
        assertThat(notification.getOffenderId()).isEqualTo(77L);
        assertThat(notification.getParentEntityId()).isEqualTo(99L);
    }

    @Test
    public void withManyInterestedCRCsUpdateCustodyKeyDateNotificationIsSavedForEachProbationArea() {
        when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of(
                ProbationArea
                        .builder()
                        .code("AA")
                        .build(),
                ProbationArea
                        .builder()
                        .code("AB")
                        .build()));

        val event = aCustodyEvent(99L, ImmutableList.of(aKeyDate(88L, "POM1")));
        spgNotificationService.notifyUpdateOfCustodyKeyDate("POM1", event);

        verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

        assertThat(findFor(SpgNotificationService.NotificationEvents.UPDATE_CUSTODY_KEY_DATE.getNotificationCode())).hasSize(2);
        assertThat(findFor(SpgNotificationService.NotificationEvents.UPDATE_CUSTODY_KEY_DATE.getNotificationCode()).stream().anyMatch(notification -> notification.getReceiverIdentity().getCode().equals("AA"))).isTrue();
        assertThat(findFor(SpgNotificationService.NotificationEvents.UPDATE_CUSTODY_KEY_DATE.getNotificationCode()).stream().anyMatch(notification -> notification.getReceiverIdentity().getCode().equals("AB"))).isTrue();
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    public void withNoInterestedCRCsNothingInsertedForDeletedCustodyKeyDateNotification() {
        when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of());

        val event = aCustodyEvent(99L, 77L, ImmutableList.of());
        spgNotificationService.notifyDeletedCustodyKeyDate(aKeyDate(88L,"POM1"), event);

        verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

        assertThat(findFor(SpgNotificationService.NotificationEvents.DELETE_CUSTODY_KEY_DATE.getNotificationCode())).isEmpty();
    }

    @Test
    public void withOneInterestedCRCDeleteCustodyKeyDateNotificationIsSaved() {
        when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of(ProbationArea
                .builder()
                .code("AA")
                .build()));

        val event = aCustodyEvent(99L, 77L, ImmutableList.of());
        spgNotificationService.notifyDeletedCustodyKeyDate(aKeyDate(88L,"POM1"), event);

        verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

        assertThat(findFor(SpgNotificationService.NotificationEvents.DELETE_CUSTODY_KEY_DATE.getNotificationCode())).hasSize(1);
    }

    @Test
    public void keyInformationAboutTheDeletedCustodyKeyDateNotificationIsSaved() {
        when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of(ProbationArea
                .builder()
                .code("AA")
                .build()));

        val event = aCustodyEvent(99L, 77L, ImmutableList.of());
        spgNotificationService.notifyDeletedCustodyKeyDate(aKeyDate(88L,"POM1"), event);

        verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

        val notification = findFor(SpgNotificationService.NotificationEvents.DELETE_CUSTODY_KEY_DATE.getNotificationCode()).get(0);
        assertThat(notification.getUniqueId()).isEqualTo(88L);
        assertThat(notification.getOffenderId()).isEqualTo(77L);
        assertThat(notification.getParentEntityId()).isEqualTo(99L);
    }

    @Test
    public void withManyInterestedCRCsDeleteCustodyKeyDateNotificationIsSavedForEachProbationArea() {
        when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of(
                ProbationArea
                        .builder()
                        .code("AA")
                        .build(),
                ProbationArea
                        .builder()
                        .code("AB")
                        .build()));

        val event = aCustodyEvent(99L, ImmutableList.of());
        spgNotificationService.notifyDeletedCustodyKeyDate(aKeyDate(88L, "POM1"), event);

        verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

        assertThat(findFor(SpgNotificationService.NotificationEvents.DELETE_CUSTODY_KEY_DATE.getNotificationCode())).hasSize(2);
        assertThat(findFor(SpgNotificationService.NotificationEvents.DELETE_CUSTODY_KEY_DATE.getNotificationCode()).stream().anyMatch(notification -> notification.getReceiverIdentity().getCode().equals("AA"))).isTrue();
        assertThat(findFor(SpgNotificationService.NotificationEvents.DELETE_CUSTODY_KEY_DATE.getNotificationCode()).stream().anyMatch(notification -> notification.getReceiverIdentity().getCode().equals("AB"))).isTrue();
    }

    @Test
    public void keyInformationAboutTheUpdateCustodyStatusIsSaved() {
        when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of(ProbationArea
                .builder()
                .code("AA")
                .build()));

        final var event = aCustodyEvent(99L, ImmutableList.of());
        final var offender = anOffender().toBuilder().offenderId(77L).build();
        spgNotificationService.notifyUpdateOfCustodyLocationChange(offender, event);

        verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

        val notification = findFor(SpgNotificationService.NotificationEvents.AMEND_EVENT.getNotificationCode()).get(0);
        assertThat(notification.getUniqueId()).isEqualTo(99L);
        assertThat(notification.getOffenderId()).isEqualTo(77L);
    }

    @Test
    public void keyInformationAboutTheUpdateCustodyIsSaved() {
        when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of(ProbationArea
                .builder()
                .code("AA")
                .build()));

        final var event = aCustodyEvent(99L, ImmutableList.of());
        final var offender = anOffender().toBuilder().offenderId(77L).build();
        spgNotificationService.notifyUpdateOfCustody(offender, event);

        verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

        val notification = findFor(SpgNotificationService.NotificationEvents.UPDATE_CUSTODY.getNotificationCode()).get(0);
        assertThat(notification.getUniqueId()).isEqualTo(99L);
        assertThat(notification.getOffenderId()).isEqualTo(77L);
    }

    @Nested
    class UpdateOffender {
        @Test
        public void withOneInterestedCRCOffenderUpdateNotificationIsSaved() {
            when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of(ProbationArea
                    .builder()
                    .code("AA")
                    .build()));

            spgNotificationService.notifyUpdateOfOffender(Offender
                    .builder()
                    .offenderId(99L)
                    .build());

            verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

            assertThat(findFor("OIBI027")).hasSize(1);
        }

        @Test
        public void withMultipleInterestedCRCOffenderUpdateNotificationIsSavedForAllOfThem() {
            when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of(ProbationArea
                            .builder()
                            .code("AA")
                            .build(),
                    ProbationArea
                            .builder()
                            .code("BB")
                            .build(),
                    ProbationArea
                            .builder()
                            .code("CC")
                            .build()));

            spgNotificationService.notifyUpdateOfOffender(Offender
                    .builder()
                    .offenderId(99L)
                    .build());

            verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

            assertThat(findFor("OIBI027")).hasSize(3);
        }

        @Test
        @MockitoSettings(strictness = Strictness.LENIENT)
        public void witNoInterestedCRCOffenderUpdateNotificationIsNotSaved() {
            when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of());

            spgNotificationService.notifyUpdateOfOffender(Offender
                    .builder()
                    .offenderId(99L)
                    .build());

            verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

            assertThat(findFor("OIBI027")).hasSize(0);
        }

        @Test
        public void offenderIdIsSavedWithNotification() {
            when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of(ProbationArea
                    .builder()
                    .code("AA")
                    .build()));

            spgNotificationService.notifyUpdateOfOffender(Offender
                    .builder()
                    .offenderId(99L)
                    .build());

            verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

            var notification = findFor("OIBI027").get(0);

            assertThat(notification.getOffenderId()).isEqualTo(99L);
            assertThat(notification.getUniqueId()).isEqualTo(99L);
            assertThat(notification.getParentEntityId()).isNull();
        }

    }
    @Nested
    class InsertAdditionalOffenderIdentifier {
        @Test
        public void withOneInterestedCRCNotificationIsSaved() {
            when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of(ProbationArea
                    .builder()
                    .code("AA")
                    .build()));

            spgNotificationService.notifyInsertOfOffenderAdditionalIdentifier(
                    Offender
                            .builder()
                            .offenderId(99L)
                            .build(),
                    AdditionalIdentifier
                            .builder()
                            .additionalIdentifierId(88L)
                            .build());

            verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

            assertThat(findFor("OIBI070")).hasSize(1);
        }

        @Test
        public void withMultipleInterestedCRCNotificationIsSavedForAllOfThem() {
            when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of(ProbationArea
                            .builder()
                            .code("AA")
                            .build(),
                    ProbationArea
                            .builder()
                            .code("BB")
                            .build(),
                    ProbationArea
                            .builder()
                            .code("CC")
                            .build()));

            spgNotificationService.notifyInsertOfOffenderAdditionalIdentifier(
                    Offender
                            .builder()
                            .offenderId(99L)
                            .build(),
                    AdditionalIdentifier
                            .builder()
                            .additionalIdentifierId(88L)
                            .build());

            verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

            assertThat(findFor("OIBI070")).hasSize(3);
        }

        @Test
        @MockitoSettings(strictness = Strictness.LENIENT)
        public void witNoInterestedCRCNotificationIsNotSaved() {
            when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of());

            spgNotificationService.notifyInsertOfOffenderAdditionalIdentifier(
                    Offender
                            .builder()
                            .offenderId(99L)
                            .build(),
                    AdditionalIdentifier
                            .builder()
                            .additionalIdentifierId(88L)
                            .build());

            verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

            assertThat(findFor("OIBI070")).hasSize(0);
        }

        @Test
        public void offenderIdIsSavedWithNotification() {
            when(spgNotificationHelperRepository.getInterestedCRCs(any())).thenReturn(ImmutableList.of(ProbationArea
                    .builder()
                    .code("AA")
                    .build()));

            spgNotificationService.notifyInsertOfOffenderAdditionalIdentifier(
                    Offender
                            .builder()
                            .offenderId(99L)
                            .build(),
                    AdditionalIdentifier
                            .builder()
                            .additionalIdentifierId(88L)
                            .build());

            verify(spgNotificationRepository, atLeastOnce()).saveAll(spgNotificationsCaptor.capture());

            var notification = findFor("OIBI070").get(0);

            assertThat(notification.getOffenderId()).isEqualTo(99L);
            assertThat(notification.getUniqueId()).isEqualTo(88L);
            assertThat(notification.getParentEntityId()).isNull();
        }

    }

    private List<SpgNotification> findFor(String businessInteraction) {
        return spgNotificationsCaptor
                .getAllValues()
                .stream()
                .flatMap(notificationSet -> notificationSet.stream().filter(notification -> notification.getBusinessInteraction().getBusinessInteractionCode().equals(businessInteraction)))
                .collect(toList());

    }
}
