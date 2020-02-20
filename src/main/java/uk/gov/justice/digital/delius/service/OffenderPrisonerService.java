package uk.gov.justice.digital.delius.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.delius.jpa.standard.entity.*;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderPrisonerRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OffenderPrisonerService {
    private final EventRepository eventRepository;
    private final OffenderPrisonerRepository offenderPrisonerRepository;

    public OffenderPrisonerService(EventRepository eventRepository, OffenderPrisonerRepository offenderPrisonerRepository) {
        this.eventRepository = eventRepository;
        this.offenderPrisonerRepository = offenderPrisonerRepository;
    }

    @Transactional
    public Offender refreshOffenderPrisonersFor(Offender offender) {
        final var offenderId = offender.getOffenderId();
        final var events = eventRepository.findByOffenderId(offenderId);
        offenderPrisonerRepository.deleteAllByOffenderId(offenderId);
        offenderPrisonerRepository.saveAll(createOffenderPrisonersFromEvents(offenderId, events));
        offender.setMostRecentPrisonerNumber(getPrisonNumberFromLatestEvent(events));
        return offender;
    }

    public String getPrisonNumberFromLatestEvent(List<Event> events) {
        return events
                .stream()
                .map(Event::getDisposal)
                .filter(Objects::nonNull)
                .filter(disposal -> Objects.nonNull(disposal.getCustody()))
                .filter(disposal -> StringUtils.isNotEmpty(disposal.getCustody().getPrisonerNumber()))
                .max(Comparator.comparing(Disposal::getStartDate))
                .map(Disposal::getCustody)
                .map(Custody::getPrisonerNumber)
                .orElse("");
    }

    public Set<OffenderPrisoner> createOffenderPrisonersFromEvents(Long offenderId, List<Event> events) {
        return events
                .stream()
                .map(Event::getDisposal)
                .filter(Objects::nonNull)
                .map(Disposal::getCustody)
                .filter(Objects::nonNull)
                .map(Custody::getPrisonerNumber)
                .filter(StringUtils::isNotEmpty)
                .map(prisonerNumber -> OffenderPrisoner
                        .builder()
                        .offenderId(offenderId)
                        .prisonerNumber(prisonerNumber)
                        .build())
                .collect(Collectors.toSet());
    }
}
