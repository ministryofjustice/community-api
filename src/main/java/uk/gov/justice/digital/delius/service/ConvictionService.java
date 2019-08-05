package uk.gov.justice.digital.delius.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.Conviction;
import uk.gov.justice.digital.delius.data.api.CourtCase;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.transformers.ConvictionTransformer;

import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.convertToBoolean;

@Service
@Slf4j
public class ConvictionService {
    private final EventRepository eventRepository;
    private final ConvictionTransformer convictionTransformer;
    private final SpgNotificationService spgNotificationService;

    @Autowired
    public ConvictionService(EventRepository eventRepository, ConvictionTransformer convictionTransformer, SpgNotificationService spgNotificationService) {
        this.eventRepository = eventRepository;
        this.convictionTransformer = convictionTransformer;
        this.spgNotificationService = spgNotificationService;
    }

    public List<Conviction> convictionsFor(Long offenderId) {
        List<uk.gov.justice.digital.delius.jpa.standard.entity.Event> events = eventRepository.findByOffenderId(offenderId);
        return events
                .stream()
                .filter(event -> !convertToBoolean(event.getSoftDeleted()))
                .sorted(Comparator.comparing(uk.gov.justice.digital.delius.jpa.standard.entity.Event::getReferralDate).reversed())
                .map(convictionTransformer::convictionOf)
                .collect(toList());
    }

    public Conviction addCourtCaseFor(Long offenderId, CourtCase courtCase) {
        val event = convictionTransformer.eventOf(
                offenderId,
                courtCase,
                String.valueOf(eventRepository.findByOffenderId(offenderId).size() + 1));

        val conviction = convictionTransformer.convictionOf(eventRepository.save(event));
        spgNotificationService.notifyNewCourtCaseCreated(event);
        return conviction;
    }
}
