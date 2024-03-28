package uk.gov.justice.digital.delius.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.config.FeatureSwitches;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.CourtCase;
import uk.gov.justice.digital.delius.entitybuilders.EventEntityBuilder;
import uk.gov.justice.digital.delius.entitybuilders.KeyDateEntityBuilder;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.ConvictionTransformer;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
@Slf4j
public class ConvictionService {
    public static final int SENTENCE_START_DATE_LENIENT_DAYS = 7;
    private final EventRepository eventRepository;
    private final EventEntityBuilder eventEntityBuilder;

    public static class SingleActiveCustodyConvictionNotFoundException extends BadRequestException {
        public SingleActiveCustodyConvictionNotFoundException(Long offenderId, int activeCustodyConvictionCount) {
            super(String.format("Expected offender %d to have a single custody related event but found %d events", offenderId, activeCustodyConvictionCount));
        }
    }

    public static class DuplicateActiveCustodialConvictionsException extends Exception {
        private final int convictionCount;

        public DuplicateActiveCustodialConvictionsException(int convictionCount) {
            super(String.format("duplicate active custody conviction count was %d, should be 1", convictionCount));
            this.convictionCount = convictionCount;
        }

        public int getConvictionCount() {
            return convictionCount;
        }
    }

    public static class DuplicateConvictionsForSentenceDateException extends Exception {
        private final int convictionCount;

        DuplicateConvictionsForSentenceDateException(int convictionCount) {
            super(String.format("duplicate active custody conviction count was %d, should be 1", convictionCount));
            this.convictionCount = convictionCount;
        }

        public int getConvictionCount() {
            return convictionCount;
        }
    }

    public static class CustodyTypeCodeIsNotValidException extends Exception {
        CustodyTypeCodeIsNotValidException(String message) {
            super(message);
        }
    }

    @Autowired
    public ConvictionService(
        EventRepository eventRepository,
        EventEntityBuilder eventEntityBuilder
    ) {
        this.eventRepository = eventRepository;
        this.eventEntityBuilder = eventEntityBuilder;
    }

    @Transactional(readOnly = true)
    public List<Conviction> convictionsFor(Long offenderId, boolean activeOnly) {
        List<uk.gov.justice.digital.delius.jpa.standard.entity.Event> events;
        if (activeOnly) {
            events = eventRepository.findByOffenderIdAndActiveFlagTrue(offenderId);
        } else {
            events = eventRepository.findByOffenderId(offenderId);
        }
        return events
            .stream()
            .filter(event -> !event.isSoftDeleted())
            .sorted(Comparator.comparing(uk.gov.justice.digital.delius.jpa.standard.entity.Event::getReferralDate).reversed())
            .map(ConvictionTransformer::convictionOf)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<Conviction> convictionFor(Long offenderId, Long eventId) {
        final var event = eventRepository.findById(eventId);
        return event
            .filter(e -> offenderId.equals(e.getOffenderId()))
            .filter(e -> !e.isSoftDeleted())
            .map(ConvictionTransformer::convictionOf);
    }

    @Transactional(readOnly = true)
    public Optional<Event> eventFor(Long offenderId, Long eventId) {
        return eventRepository.findByEventIdAndOffenderIdAndSoftDeletedFalse(eventId, offenderId);
    }

    @Transactional
    public Conviction addCourtCaseFor(Long offenderId, CourtCase courtCase) {
        final var event = eventEntityBuilder.eventOf(
            offenderId,
            courtCase,
            calculateNextEventNumber(offenderId));

        return ConvictionTransformer.convictionOf(eventRepository.save(event));
    }

    public Optional<Event> getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(Long offenderId, String prisonBookingNumber) throws DuplicateActiveCustodialConvictionsException {
        final var events = activeCustodyEvents(offenderId);

        return switch (events.size()) {
            case 0 -> Optional.empty();
            case 1 -> events.stream()
                    .filter(event -> prisonBookingNumber.equals(event.getDisposal().getCustody().getPrisonerNumber()))
                    .findFirst();
            default -> throw new DuplicateActiveCustodialConvictionsException(events.size());
        };
    }

    @Transactional(readOnly = true)
    public Optional<Long> getSingleActiveConvictionIdByOffenderIdAndPrisonBookingNumber(Long offenderId, String prisonBookingNumber) throws DuplicateActiveCustodialConvictionsException {
        return getSingleActiveConvictionByOffenderIdAndPrisonBookingNumber(offenderId, prisonBookingNumber).map(Event::getEventId);
    }

    public Result<Optional<Event>, DuplicateConvictionsForSentenceDateException> getSingleActiveConvictionIdByOffenderIdAndCloseToSentenceDate(Long offenderId, LocalDate sentenceStartDate) {
        final var events = activeCustodyEvents(offenderId)
            .stream()
            .filter(event -> didSentenceStartAroundDate(event, sentenceStartDate)).toList();

        switch (events.size()) {
            case 0:
                return Result.of(Optional.empty());
            case 1:
                return Result.of(events.stream().findFirst());
            default:
                return Result.ofError(new DuplicateConvictionsForSentenceDateException(events.size()));
        }
    }

    private boolean didSentenceStartAroundDate(Event event, LocalDate sentenceStartDate) {
        // typically used to match start dates in NOMIS and Delius which may be out by a few days
        return Math.abs(DAYS.between(event.getDisposal().getStartDate(), sentenceStartDate)) <= SENTENCE_START_DATE_LENIENT_DAYS;
    }

    @Transactional(readOnly = true)
    public Event getActiveCustodialEvent(Long offenderId) {
        final var activeCustodyConvictions = activeCustodyEvents(offenderId);

        if (activeCustodyConvictions.size() != 1) {
            throw new SingleActiveCustodyConvictionNotFoundException(offenderId, activeCustodyConvictions.size());
        }
        return activeCustodyConvictions.getFirst();
    }

    @Transactional(readOnly = true)
    public List<Event> getAllActiveCustodialEvents(Long offenderId) {
        return activeCustodyEvents(offenderId);
    }

    @Transactional(readOnly = true)
    public List<Event> getAllActiveCustodialEventsWithBookingNumber(Long offenderId, String bookingNumber) {
        return activeCustodyEvents(offenderId)
            .stream()
            .filter(event -> bookingNumber.equals(event.getDisposal().getCustody().getPrisonerNumber()))
            .toList();
    }
    private String calculateNextEventNumber(Long offenderId) {
        return String.valueOf(eventRepository.findByOffenderId(offenderId).size() + 1);
    }

    private List<Event> activeCustodyEvents(Long offenderId) {
        return eventRepository.findActiveByOffenderIdWithCustody(offenderId)
            .stream()
            .filter(event -> event.getDisposal().getTerminationDate() == null)
            .filter(event -> !event.getDisposal().getCustody().isPostSentenceSupervision())
            .toList();
    }
}
