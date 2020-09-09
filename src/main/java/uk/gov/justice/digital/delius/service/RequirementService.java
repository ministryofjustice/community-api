package uk.gov.justice.digital.delius.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.ConvictionRequirements;
import uk.gov.justice.digital.delius.data.api.PssRequirements;
import uk.gov.justice.digital.delius.jpa.standard.entity.Custody;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.Offender;
import uk.gov.justice.digital.delius.jpa.standard.entity.PssRequirement;
import uk.gov.justice.digital.delius.jpa.standard.entity.Requirement;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.RequirementTransformer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

        var requirements = getDisposalStream(convictionId, offender)
                .flatMap(this::getRequirementStream)
                .map(RequirementTransformer::requirementOf)
                .collect(Collectors.toList());

        return new ConvictionRequirements(requirements);
    }

    public PssRequirements getPssRequirementsByConvictionId(String crn, Long convictionId) {
        var offender = offenderRepository.findByCrn(crn)
                .orElseThrow(() -> new NotFoundException(String.format("Offender with CRN '%s' not found", crn)));

        var requirements = getDisposalStream(convictionId, offender)
                .map(Disposal::getCustody)
                .flatMap(this::getPssRequirementStream)
                .map(RequirementTransformer::pssRequirementOf)
                .collect(Collectors.toList());
        return new PssRequirements(requirements);
    }

    private Stream<Disposal> getDisposalStream(Long convictionId, Offender offender) {
        List<Event> events = eventRepository.findByOffenderId(offender.getOffenderId())
                .stream()
                .filter(event -> convictionId.equals(event.getEventId()))
                .collect(Collectors.toList());

        if (events.size() == 0) {
                throw new NotFoundException(String.format("Conviction with convictionId '%s' not found", convictionId));
        }

        return events.stream()
                .flatMap(this::getDisposalStream);
    }

    @NotNull
    private Stream<Disposal> getDisposalStream(Event event) {
        return Optional.ofNullable(event.getDisposal())
                .stream();
    }

    private Stream<Requirement> getRequirementStream(Disposal disposal) {
        return Optional.ofNullable(disposal.getRequirements())
                .orElse(Collections.emptyList())
                .stream();
    }

    private Stream<PssRequirement> getPssRequirementStream(Custody custody) {
        return Optional.ofNullable(custody.getPssRequirements())
                .orElse(Collections.emptyList())
                .stream();
    }
}
