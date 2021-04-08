package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.Registration;
import uk.gov.justice.digital.delius.jpa.standard.repository.RegistrationRepository;
import uk.gov.justice.digital.delius.transformers.RegistrationTransformer;

import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.convertToBoolean;

@Service
public class RegistrationService {
    private final RegistrationRepository registrationRepository;

    @Autowired
    public RegistrationService(RegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    public List<Registration> registrationsFor(Long offenderId) {
        return transform(registrationRepository.findByOffenderId(offenderId));
    }

    public List<Registration> activeRegistrationsFor(Long offenderId) {
        return transform(registrationRepository.findActiveByOffenderId(offenderId));
    }

    private List<Registration> transform(List<uk.gov.justice.digital.delius.jpa.standard.entity.Registration> registrations) {
        return registrations
            .stream()
            .filter(registration -> !convertToBoolean(registration.getSoftDeleted()))
            .sorted(Comparator.comparing(uk.gov.justice.digital.delius.jpa.standard.entity.Registration::getRegistrationDate).reversed())
            .map(RegistrationTransformer::registrationOf)
            .collect(toList());
    }
}
