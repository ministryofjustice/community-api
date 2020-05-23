package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.ConvictionRequirements;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.Requirement;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.RequirementTransformer;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@NoArgsConstructor
@AllArgsConstructor
public class RequirementService {
    @Autowired
    private OffenderRepository offenderRepository;
    @Autowired
    private EventRepository eventRepository;

    public ConvictionRequirements getRequirementsByConvictionId(String crn, Long convictionId) {
        var offender = offenderRepository.findByCrn(crn)
                .orElseThrow(() -> new NotFoundException(String.format("Offender with CRN '%s' not found", crn)));

        var requirements = eventRepository.findByOffenderId(offender.getOffenderId())
                .stream()
                .filter(event -> convictionId.equals(event.getEventId()))
                .map(Event::getDisposal)
                .flatMap(this::getRequirementStreamFromDisposal)
                .map(RequirementTransformer::requirementOf)
                .collect(Collectors.toList());

        return new ConvictionRequirements(requirements);
    }

    private Stream<Requirement> getRequirementStreamFromDisposal(Disposal disposal) {
        return disposal != null && disposal.getRequirements() != null ? disposal.getRequirements().stream() : Stream.empty();
    }
}
