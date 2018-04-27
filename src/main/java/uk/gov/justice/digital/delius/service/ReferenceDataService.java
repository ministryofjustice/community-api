package uk.gov.justice.digital.delius.service;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.ProbationArea;
import uk.gov.justice.digital.delius.jpa.filters.ProbationAreaFilter;
import uk.gov.justice.digital.delius.jpa.standard.repository.ProbationAreaRepository;
import uk.gov.justice.digital.delius.transformers.ProbationAreaTransformer;

import java.util.List;
import java.util.Optional;

@Service
public class ReferenceDataService {

    private final ProbationAreaTransformer probationAreaTransformer;
    private final ProbationAreaRepository probationAreaRepository;

    @Autowired
    public ReferenceDataService(ProbationAreaTransformer probationAreaTransformer, ProbationAreaRepository probationAreaRepository) {
        this.probationAreaTransformer = probationAreaTransformer;
        this.probationAreaRepository = probationAreaRepository;
    }

    public List<ProbationArea> getProbationAreas(Optional<List<String>> maybeCodes, boolean restrictActive) {
        ProbationAreaFilter probationAreaFilter = ProbationAreaFilter.builder().probationAreaCodes(maybeCodes).restrictActive(restrictActive).build();

        return probationAreaTransformer.probationAreasOf(probationAreaRepository.findAll(probationAreaFilter));
    }

    public List<ProbationArea> getProbationAreasForCode(String code, boolean restrictActive) {
        ProbationAreaFilter probationAreaFilter = ProbationAreaFilter.builder().probationAreaCodes(Optional.of(Lists.newArrayList(code))).restrictActive(restrictActive).build();

        return probationAreaTransformer.probationAreasOf(probationAreaRepository.findAll(probationAreaFilter));
    }

}
