package uk.gov.justice.digital.delius.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.MappaDetails;
import uk.gov.justice.digital.delius.data.api.RiskResourcingDetails;
import uk.gov.justice.digital.delius.jpa.standard.repository.CaseAllocationRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.RegistrationRepository;
import uk.gov.justice.digital.delius.transformers.CaseAllocationTransformer;
import uk.gov.justice.digital.delius.transformers.MappaDetailsTransformer;

import java.util.Optional;

@Service
public class RiskService {
    private final RegistrationRepository registrationRepository;
    private final CaseAllocationRepository caseAllocationRepository;

    public RiskService(RegistrationRepository registrationRepository,
                       CaseAllocationRepository caseAllocationRepository) {
        this.registrationRepository = registrationRepository;
        this.caseAllocationRepository = caseAllocationRepository;
    }

    public MappaDetails getMappaDetails(Long offenderId) {
        final var reg = registrationRepository.findActiveMappaRegistrationByOffenderId(offenderId, PageRequest.of(0, 1));
        return reg.stream()
            .findFirst()
            .map(MappaDetailsTransformer::mappaDetailsOf)
            .orElseThrow(() -> new NotFoundException("MAPPA details for offender not found"));
    }

    public Optional<RiskResourcingDetails> getResourcingDetails(Long offenderId) {
        return caseAllocationRepository
            .findFirstByOffenderIdOrderByAllocationDecisionDateDesc(offenderId)
            .map(CaseAllocationTransformer::riskResourcingDetailsOf);
    }
}
