package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.CourtAppearance;
import uk.gov.justice.digital.delius.jpa.standard.repository.CourtAppearanceRepository;
import uk.gov.justice.digital.delius.transformers.CourtAppearanceTransformer;

import java.util.List;

@Service
public class CourtAppearanceService {

    private final CourtAppearanceRepository courtAppearanceRepository;
    private final CourtAppearanceTransformer courtAppearanceTransformer;

    @Autowired
    public CourtAppearanceService(CourtAppearanceRepository courtAppearanceRepository, CourtAppearanceTransformer courtAppearanceTransformer) {
        this.courtAppearanceRepository = courtAppearanceRepository;
        this.courtAppearanceTransformer = courtAppearanceTransformer;
    }

    public List<CourtAppearance> courtAppearancesFor(Long offenderId) {
        return courtAppearanceTransformer.courtAppearancesOf(courtAppearanceRepository.findByOffenderId(offenderId));
    }

}
