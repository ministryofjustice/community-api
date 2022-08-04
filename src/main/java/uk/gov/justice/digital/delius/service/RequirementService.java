package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
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

    public ConvictionRequirements getActiveRequirementsByConvictionId(
        String crn,
        Long convictionId,
        boolean includeDeleted
    ) {
        return getConvictionRequirements(getActiveEvent(crn, convictionId), false, includeDeleted);
    }

    private ConvictionRequirements getConvictionRequirements(
        Event conviction, boolean includeInactive, boolean includeDeleted
    ) {
        var requirements = Optional.of(conviction)
            .map(Event::getDisposal)
            .map(Disposal::getRequirements)
            .stream()
            .flatMap(Collection::stream)
            .map(RequirementTransformer::requirementOf)
            .filter(r -> (includeInactive || r.getActive()) && (includeDeleted || !r.getSoftDeleted()))
            .toList();

        return new ConvictionRequirements(requirements);
    }

    public ConvictionRequirements getRequirementsByConvictionId(String crn, Long convictionId, boolean includeInactive, boolean includeDeleted) {
        return getConvictionRequirements(getEvent(crn, convictionId), includeInactive, includeDeleted);
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
    // that has a main category of F - rehab activity requirement. If multiple active ones exist
    // the one with the latest start date is chosen.
    // NB. The called method getRequirementsByConvictionId accepts an event id (conviction or sentence)
    // and perhaps should have been named getRequirementsByEventId
    public Optional<Requirement> getActiveRequirement(String crn, Long eventId, String requirementTypeCode) {

        return getRequirementsByConvictionId(crn, eventId, false, false)
            .getRequirements().stream()
            .filter(requirement ->
                ofNullable(requirement.getRequirementTypeMainCategory())
                    .map(cat -> requirementTypeCode.equals(cat.getCode()))
                    .orElse(false))
            .sorted(comparing(Requirement::getStartDate, reverseOrder()).thenComparing(Requirement::getCreatedDatetime, reverseOrder()))
            .findFirst();
    }
}
