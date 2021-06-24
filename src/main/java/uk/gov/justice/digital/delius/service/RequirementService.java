package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.ConvictionRequirements;
import uk.gov.justice.digital.delius.data.api.LicenceConditions;
import uk.gov.justice.digital.delius.data.api.PssRequirements;
import uk.gov.justice.digital.delius.data.api.Requirement;
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.ContactTransformer;
import uk.gov.justice.digital.delius.transformers.RequirementTransformer;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Service
@NoArgsConstructor
@AllArgsConstructor
public class RequirementService {

    @Autowired
    private OffenderRepository offenderRepository;
    @Autowired
    private EventRepository eventRepository;

    public ConvictionRequirements getActiveRequirementsByConvictionId(String crn, Long convictionId) {
        return getConvictionRequirements(getActiveEvent(crn, convictionId), true);
    }

    private ConvictionRequirements getConvictionRequirements(Event conviction, boolean activeOnly) {
        Predicate<Requirement> activeFilter = activeFilter(activeOnly);

        var requirements = Optional.of(conviction)
            .map(Event::getDisposal)
            .map(Disposal::getRequirements)
            .stream()
            .flatMap(Collection::stream)
            .map(RequirementTransformer::requirementOf)
            .filter(activeFilter)
            .collect(toList());

        return new ConvictionRequirements(requirements);
    }


    private Predicate<Requirement> activeFilter(boolean activeOnly) {
        if(activeOnly) {
            return Requirement::getActive;
        }
        return r -> true;
    }

    public ConvictionRequirements getRequirementsByConvictionId(String crn, Long convictionId) {
        return getConvictionRequirements(getEvent(crn, convictionId), false);
    }

    public PssRequirements getPssRequirementsByConvictionId(String crn, Long convictionId) {
        var pssRequirements = Optional.of(getEvent(crn, convictionId))
                .map(Event::getDisposal)
                .map(Disposal::getCustody)
                .map(Custody::getPssRequirements)

                .stream()
                .flatMap(Collection::stream)
                .map(RequirementTransformer::pssRequirementOf)
                .collect(toList());

        return new PssRequirements(pssRequirements);
    }

    private Event getEvent(String crn, Long convictionId) {
        var offenderId = getOffenderId(crn);
        return eventRepository.findByOffenderId(offenderId)
                .stream()
                .filter(event -> convictionId.equals(event.getEventId()))
                .findAny()
                .orElseThrow(() ->  new NotFoundException(format("Conviction with convictionId '%s' not found", convictionId)));
    }

    private Event getActiveEvent(String crn, Long convictionId) {
        var offenderId = getOffenderId(crn);
        return eventRepository.findByOffenderIdAndEventIdAndActiveFlagTrue(offenderId, convictionId)
            .orElseThrow(() ->  new NotFoundException(format("Active conviction with convictionId '%s' not found", convictionId)));
    }

    private Long getOffenderId(String crn) {
        return offenderRepository.getOffenderIdFrom(crn)
                .orElseThrow(() -> new NotFoundException(format("Offender with CRN '%s' not found", crn)));
    }

    public LicenceConditions getLicenceConditionsByConvictionId(String crn, Long convictionId) {
        var conditionsList = Optional.of(getEvent(crn, convictionId))
                .map(Event::getDisposal)
                .map(Disposal::getLicenceConditions)
                .stream()
                .flatMap(Collection::stream)
                .map(ContactTransformer::licenceConditionOf)
                .collect(toList());

        return new LicenceConditions(conditionsList);
    }

    // In the context of NSI's for interventions, the offender's sentence is the event against which
    // the NSI is created. This sentence is supplied by interventions as part of the "referral sent
    // request". This method takes the sentence and finds any associated requirements and returns one
    // that has a main category of F - rehab activity requirement. It is invalid for multiple to exist.
    // NB. The called method getRequirementsByConvictionId accepts an event id (conviction or sentence)
    // and perhaps should have been named getRequirementsByEventId
    public Optional<Requirement> getRequirement(String crn, Long eventId, String requirementTypeCode) {

        var requirements = getRequirementsByConvictionId(crn, eventId)
            .getRequirements().stream()
            .filter(requirement ->
                ofNullable(requirement.getRequirementTypeMainCategory())
                    .map(cat -> requirementTypeCode.equals(cat.getCode()))
                    .orElse(false))
            .collect(toList());

        if ( requirements.size() > 1 ) {
            throw new BadRequestException(format("CRN: %s EventId: %d has multiple referral requirements", crn, eventId));
        }

        return requirements.stream().findFirst();
    }


}
