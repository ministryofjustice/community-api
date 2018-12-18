package uk.gov.justice.digital.delius.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.Conviction;
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

    @Autowired
    public ConvictionService(EventRepository eventRepository, ConvictionTransformer convictionTransformer) {
        this.eventRepository = eventRepository;
        this.convictionTransformer = convictionTransformer;
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
}
