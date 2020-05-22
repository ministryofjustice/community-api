package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.PersonalCircumstance;
import uk.gov.justice.digital.delius.jpa.standard.repository.PersonalCircumstanceRepository;
import uk.gov.justice.digital.delius.transformers.PersonalCircumstanceTransformer;

import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.convertToBoolean;

@Service
public class PersonalCircumstanceService {
    private final PersonalCircumstanceRepository personalCircumstanceRepository;
    private final PersonalCircumstanceTransformer personalCircumstanceTransformer;

    @Autowired
    public PersonalCircumstanceService(PersonalCircumstanceRepository personalCircumstanceRepository, PersonalCircumstanceTransformer personalCircumstanceTransformer) {
        this.personalCircumstanceRepository = personalCircumstanceRepository;
        this.personalCircumstanceTransformer = personalCircumstanceTransformer;
    }

    public List<PersonalCircumstance> personalCircumstancesFor(Long offenderId) {
        List<uk.gov.justice.digital.delius.jpa.standard.entity.PersonalCircumstance> personalCircumstances = personalCircumstanceRepository.findByOffenderId(offenderId);
        return personalCircumstances
                .stream()
                .filter(personalCircumstance -> !convertToBoolean(personalCircumstance.getSoftDeleted()))
                .sorted(Comparator.comparing(uk.gov.justice.digital.delius.jpa.standard.entity.PersonalCircumstance::getPersonalCircumstanceId).reversed())
                .map(PersonalCircumstanceTransformer::personalCircumstanceOf)
                .collect(toList());
    }
}
