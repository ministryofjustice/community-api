package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.Offence;
import uk.gov.justice.digital.delius.jpa.standard.entity.MainOffence;
import uk.gov.justice.digital.delius.jpa.standard.repository.MainOffenceRepository;
import uk.gov.justice.digital.delius.transformers.OffenceTransformer;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.convertToBoolean;

@Service
public class OffenceService {

    private final MainOffenceRepository mainOffenceRepository;

    @Autowired
    public OffenceService(MainOffenceRepository mainOffenceRepository) {
        this.mainOffenceRepository = mainOffenceRepository;
    }

    public List<Offence> offencesFor(Long offenderId) {
        List<MainOffence> mainOffences = mainOffenceRepository.findByOffenderId(offenderId);
        return mainOffences
            .stream()
            .filter(mainOffence -> !convertToBoolean(mainOffence.getSoftDeleted()))
            .map(this::combineMainAndAdditionalOffences)
            .flatMap(List::stream)
            .collect(toList());
    }

    private List<Offence> combineMainAndAdditionalOffences(MainOffence mainOffence) {
        List<Offence> offences = OffenceTransformer.offencesOf(mainOffence.getEvent().getAdditionalOffences());
        if (mainOffence != null) {
            offences.addFirst(OffenceTransformer.offenceOf(mainOffence));
        }
        return offences;
    }

}
