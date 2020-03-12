package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.ConvictionRequirements;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.repository.EventRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.OffenderRepository;
import uk.gov.justice.digital.delius.transformers.RequirementTransformer;

import java.util.stream.Collectors;

@Service
public class RequirementService {
    @Autowired
    private RequirementTransformer requirementTransformer;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private OffenderRepository offenderRepository;

    public ConvictionRequirements getRequirementsByConvictionId(String crn, String convictionId) {
        var requirements = offenderRepository.findByCrn(crn)
                .map(o -> eventRepository.findByOffenderId(o.getOffenderId()))
                .map(events -> events.stream()
                        .map(Event::getDisposal)
                        .collect(Collectors.toList())
                )
                .map(disposals -> disposals.stream()
                        .flatMap(disposal -> disposal.getRequirements().stream())
                        .map(requirement -> requirementTransformer.requirementOf(requirement))
                        .collect(Collectors.toList()));
        return new ConvictionRequirements(requirements.orElseThrow(() -> new RuntimeException("Offender not found")));
    }
}
