package uk.gov.justice.digital.delius.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.MappaDetails;
import uk.gov.justice.digital.delius.jpa.standard.repository.RegistrationRepository;
import uk.gov.justice.digital.delius.transformers.MappaDetailsTransformer;

@Service
public class RiskService {
    private final RegistrationRepository registrationRepository;

    public RiskService(RegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    public MappaDetails getMappaDetails(Long offenderId) {
        final var reg = registrationRepository.findActiveMappaRegistrationByOffenderId(offenderId, PageRequest.of(0, 1));
        return reg.stream()
            .findFirst()
            .map(MappaDetailsTransformer::mappaDetailsOf)
            .orElseThrow(() -> new NotFoundException("MAPPA details for offender not found"));
    }
}
