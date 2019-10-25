package uk.gov.justice.digital.delius.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;
import uk.gov.justice.digital.delius.jpa.standard.repository.*;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.digital.delius.service.SpgNotificationService.NotificationEvents.*;

@Service
@Slf4j
public class SpgNotificationService {
    private final BusinessInteractionRepository businessInteractionRepository;
    private final BusinessInteractionXmlMapRepository businessInteractionXmlMapRepository;
    private final StandardReferenceRepository standardReferenceRepository;
    private final ProbationAreaRepository probationAreaRepository;
    private final SpgNotificationRepository spgNotificationRepository;
    private final SpgNotificationHelperRepository spgNotificationHelperRepository;


    public SpgNotificationService(BusinessInteractionRepository businessInteractionRepository, BusinessInteractionXmlMapRepository businessInteractionXmlMapRepository, StandardReferenceRepository standardReferenceRepository, ProbationAreaRepository probationAreaRepository, SpgNotificationRepository spgNotificationRepository, SpgNotificationHelperRepository spgNotificationHelperRepository) {
        this.businessInteractionRepository = businessInteractionRepository;
        this.businessInteractionXmlMapRepository = businessInteractionXmlMapRepository;
        this.standardReferenceRepository = standardReferenceRepository;
        this.probationAreaRepository = probationAreaRepository;
        this.spgNotificationRepository = spgNotificationRepository;
        this.spgNotificationHelperRepository = spgNotificationHelperRepository;
    }

    enum NotificationEvents {
        INSERT_EVENT("CWBI006"),
        INSERT_COURT_APPEARANCE("CWBI007"),
        UPDATE_OFFENDER("OIBI027"),
        INSERT_CUSTODY_KEY_DATE("SPOBI010"),
        UPDATE_CUSTODY_KEY_DATE("SPOBI011"),
        DELETE_CUSTODY_KEY_DATE("TCBI037");


        private final String notificationCode;

        NotificationEvents(String notificationCode) {
            this.notificationCode = notificationCode;
        }

        public String getNotificationCode() {
            return notificationCode;
        }
    }
    public void notifyNewCourtCaseCreated(Event event) {

        // these events represent what the Delius code indicate what is inserted for this scenario and match what we see in test for Delius
        // It would be preferable to know what the actual requirement is here to get this correct. In test we have also seen "SPGALF01" business interactions
        // sent as well but not sure how or why they were sent
        createNotificationsFor(INSERT_EVENT, event.getOffenderId(), event.getEventId());
        event.getCourtAppearances().forEach(courtAppearance -> createNotificationsFor(INSERT_COURT_APPEARANCE, event.getOffenderId(), courtAppearance.getCourtAppearanceId()));
        createNotificationsFor(UPDATE_OFFENDER, event.getOffenderId());
    }

    public void notifyNewCustodyKeyDate(String custodyKeyTypeCode, Event event) {
        createNotificationsFor(INSERT_CUSTODY_KEY_DATE, event.getOffenderId(), keyDateIdOf(custodyKeyTypeCode, event), event.getEventId());
    }


    public void notifyUpdateOfCustodyKeyDate(String custodyKeyTypeCode, Event event) {
        createNotificationsFor(UPDATE_CUSTODY_KEY_DATE, event.getOffenderId(), keyDateIdOf(custodyKeyTypeCode, event), event.getEventId());
    }


    public void notifyDeletedCustodyKeyDate(KeyDate deletedKeyDate, Event event) {
        createNotificationsFor(DELETE_CUSTODY_KEY_DATE, event.getOffenderId(), deletedKeyDate.getKeyDateId(), event.getEventId());
    }

    private void createNotificationsFor(NotificationEvents notificationEvent, Long offenderId) {
        createNotificationsFor(notificationEvent, offenderId, offenderId);
    }

    private void createNotificationsFor(NotificationEvents notificationEvent, Long offenderId, Long uniqueId) {
        createNotificationsFor(notificationEvent, offenderId, uniqueId, null);
    }

    private void createNotificationsFor(NotificationEvents notificationEvent, Long offenderId, Long uniqueId, Long parentId ) {
        spgNotificationRepository.saveAll(
            areasThatHaveAnInterestInOffender(offenderId)
                    .stream()
                    .map(probationArea ->
                            {
                                val sendingProbationArea = probationAreaRepository.findByCode("N00").orElseThrow(() -> new RuntimeException("No probation area for send SPG"));
                                val businessInteraction = businessInteractionRepository
                                        .findByBusinessInteractionCode(notificationEvent.getNotificationCode())
                                        .orElseThrow(() -> new RuntimeException(String.format("No SPG business interaction code found for %s", notificationEvent.getNotificationCode())));

                                log.info("Sending SPG notification {} to {}", notificationEvent.getNotificationCode(), probationArea.getCode());

                                // for these values I have looked at the legacy Delius code and looked at what the Delius application
                                // does in test when writing these records. For now I have to assumed the values are correct however we
                                // need to be aware that the values are not derived from any acceptance criteria but is "as is" Delius code
                                return SpgNotification
                                        .builder()
                                        .offenderId(offenderId)
                                        .businessInteraction(businessInteraction)
                                        .receiverIdentity(probationArea)
                                        .senderIdentity(sendingProbationArea)
                                        .uniqueId(uniqueId)
                                        .parentEntityId(parentId)
                                        .dateCreated(LocalDateTime.now())
                                        .spgMessageContextId(messageContextIdFor(businessInteraction))
                                        .controlReference(nextControlReferenceFor(sendingProbationArea))
                                        .processedFlag(0L)
                                        .errorFlag(0L)
                                        .messageDirection("O")
                                        .rowVersion(0L)
                                        .exportToFileFlag(0L)
                                        .build();
                        }).collect(toList()));
    }

    private Long nextControlReferenceFor(ProbationArea sendingProbationArea) {
        return spgNotificationHelperRepository.getNextControlSequence(sendingProbationArea.getCode());
    }

    private Long messageContextIdFor(BusinessInteraction businessInteraction) {
        val contextCode = businessInteractionXmlMapRepository
                .findByBusinessInteractionId(businessInteraction.getBusinessInteractionId())
                .map(businessInteractionMap -> {
                        switch (businessInteractionMap.getDataUpdateMode().toUpperCase())
                        {
                            case "I":
                                return "INS";
                            case "U":
                                return "UPD";
                            case "D":
                                return "DEL";
                            default:
                                return null;
                        }
                })
                .orElse("UNK");

        return standardReferenceRepository
                .findByCodeAndCodeSetName(contextCode, "SPG MESSAGE CONTEXT").map(StandardReference::getStandardReferenceListId)
                .orElseThrow(() -> new RuntimeException(String.format("Unable to find reference data for contextCode %s", contextCode)));
    }

    private List<ProbationArea> areasThatHaveAnInterestInOffender(Long offenderId) {
        return spgNotificationHelperRepository.getInterestedCRCs(String.valueOf(offenderId));
    }

    private Long keyDateIdOf(String custodyKeyTypeCode, Event event) {
        return event.getDisposal().getCustody().getKeyDates()
                .stream()
                .filter(keyDate -> keyDate.getKeyDateType().getCodeValue().equals(custodyKeyTypeCode))
                .findAny()
                .map(KeyDate::getKeyDateId)
                .orElseThrow(() -> new RuntimeException("Can not find key date event though it has just changed"));
    }
}
