package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.ConvictionRequirements;
import uk.gov.justice.digital.delius.data.api.LicenceConditions;
import uk.gov.justice.digital.delius.data.api.PssRequirements;
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.ContactTransformer;
import uk.gov.justice.digital.delius.transformers.RequirementTransformer;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@NoArgsConstructor
@AllArgsConstructor
public class RequirementService {
    @Autowired
    private OffenderRepository offenderRepository;
    @Autowired
    private EventRepository eventRepository;

    public ConvictionRequirements getRequirementsByConvictionId(String crn, Long convictionId) {
        var requirements = Optional.of(getEvent(crn, convictionId))
                .map(Event::getDisposal)
                .map(Disposal::getRequirements)

                .stream()
                .flatMap(Collection::stream)
                .map(RequirementTransformer::requirementOf)
                .collect(Collectors.toList());

        return new ConvictionRequirements(requirements);
    }

    public PssRequirements getPssRequirementsByConvictionId(String crn, Long convictionId) {
        var pssRequirements = Optional.of(getEvent(crn, convictionId))
                .map(Event::getDisposal)
                .map(Disposal::getCustody)
                .map(Custody::getPssRequirements)

                .stream()
                .flatMap(Collection::stream)
                .map(RequirementTransformer::pssRequirementOf)
                .collect(Collectors.toList());

        return new PssRequirements(pssRequirements);
    }

    private Event getEvent(String crn, Long convictionId) {
        var offender = getOffender(crn);
        return eventRepository.findByOffenderId(offender.getOffenderId())
                .stream()
                .filter(event -> convictionId.equals(event.getEventId()))
                .findAny()
                .orElseThrow(() ->  new NotFoundException(String.format("Conviction with convictionId '%s' not found", convictionId)));
    }

    private Offender getOffender(String crn) {
        return offenderRepository.findByCrn(crn)
                .orElseThrow(() -> new NotFoundException(String.format("Offender with CRN '%s' not found", crn)));
    }

    public LicenceConditions getLicenceConditionsByConvictionId(String crn, Long convictionId) {
        var conditionsList = Optional.of(getEvent(crn, convictionId))
                .map(Event::getDisposal)
                .map(Disposal::getLicenceConditions)
                .stream()
                .flatMap(Collection::stream)
                .map(ContactTransformer::licenceConditionOf)
                .collect(Collectors.toList());

        return new LicenceConditions(conditionsList);
    }
}