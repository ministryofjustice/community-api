package uk.gov.justice.digital.delius.service;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.ProbationArea;
import uk.gov.justice.digital.delius.jpa.filters.ProbationAreaFilter;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.ProbationAreaRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StandardReferenceRepository;
import uk.gov.justice.digital.delius.transformers.ProbationAreaTransformer;

import java.util.List;
import java.util.Optional;

@Service
public class ReferenceDataService {

    private static final String POM_ALLOCATION_REASON_DATASET = "POM ALLOCATION REASON";
    public static final String POM_AUTO_TRANSFER_ALLOCATION_REASON_CODE = "AUT";
    public static final String POM_INTERNAL_TRANSFER_ALLOCATION_REASON_CODE = "INA";
    public static final String POM_EXTERNAL_TRANSFER_ALLOCATION_REASON_CODE = "EXT";


    private final ProbationAreaTransformer probationAreaTransformer;
    private final ProbationAreaRepository probationAreaRepository;
    private final StandardReferenceRepository standardReferenceRepository;


    @Autowired
    public ReferenceDataService(ProbationAreaTransformer probationAreaTransformer, ProbationAreaRepository probationAreaRepository, StandardReferenceRepository standardReferenceRepository) {
        this.probationAreaTransformer = probationAreaTransformer;
        this.probationAreaRepository = probationAreaRepository;
        this.standardReferenceRepository = standardReferenceRepository;
    }

    public List<ProbationArea> getProbationAreas(Optional<List<String>> maybeCodes, boolean restrictActive) {
        ProbationAreaFilter probationAreaFilter = ProbationAreaFilter.builder().probationAreaCodes(maybeCodes).restrictActive(restrictActive).build();

        return probationAreaTransformer.probationAreasOf(probationAreaRepository.findAll(probationAreaFilter));
    }

    public List<ProbationArea> getProbationAreasForCode(String code, boolean restrictActive) {
        ProbationAreaFilter probationAreaFilter = ProbationAreaFilter.builder().probationAreaCodes(Optional.of(Lists.newArrayList(code))).restrictActive(restrictActive).build();

        return probationAreaTransformer.probationAreasOf(probationAreaRepository.findAll(probationAreaFilter));
    }

    public  StandardReference pomAllocationAutoTransferReason() {
        return pomAllocationTransferReason(POM_AUTO_TRANSFER_ALLOCATION_REASON_CODE);
    }

    public StandardReference pomAllocationInternalTransferReason() {
        return pomAllocationTransferReason(POM_INTERNAL_TRANSFER_ALLOCATION_REASON_CODE);
    }

    public StandardReference pomAllocationExternalTransferReason() {
        return pomAllocationTransferReason(POM_EXTERNAL_TRANSFER_ALLOCATION_REASON_CODE);
    }

    private StandardReference pomAllocationTransferReason(String reason) {
        return standardReferenceRepository.findByCodeAndCodeSetName(reason, POM_ALLOCATION_REASON_DATASET)
                .orElseThrow(() -> new RuntimeException(String.format("No pom allocation reason found for %s", reason)));
    }
}
