package uk.gov.justice.digital.delius.service;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.ProbationArea;
import uk.gov.justice.digital.delius.jpa.filters.ProbationAreaFilter;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.ProbationAreaRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StandardReferenceRepository;
import uk.gov.justice.digital.delius.transformers.ProbationAreaTransformer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

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

    public StandardReference pomAllocationAutoTransferReason() {
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

    public Page<KeyValue> getProbationAreasCodes(boolean restrictActive) {
        final var filter = ProbationAreaFilter.builder().restrictActive(restrictActive).build();

        return probationAreaRepository.findAll(filter).stream()
                .map(area -> new KeyValue(area.getCode(), area.getDescription()))
                .collect(collectingAndThen(toList(), PageImpl::new));
    }

    public Page<KeyValue> getLocalDeliveryUnitsForProbationArea(String code) {
        return probationAreaRepository.findByCode(code).stream()
                .flatMap(probationArea -> probationArea.getBoroughs().stream())
                .flatMap(borough -> borough.getDistricts().stream())
                .map(unit -> new KeyValue(unit.getCode(), unit.getDescription()))
                .collect(collectingAndThen(toList(), PageImpl::new));
    }

    public Page<KeyValue> getTeamsForLocalDeliveryUnit(String code, String lduCode) {
        var localDeliveryUnit = probationAreaRepository.findByCode(code).stream()
                .flatMap(probationArea -> probationArea.getBoroughs().stream())
                .flatMap(borough -> borough.getDistricts().stream())
                .filter(district -> district.getCode().equals(lduCode))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        String.format("Could not find local delivery unit in probation area: '%s', with code: '%s'", code, lduCode)));

        return localDeliveryUnit.getTeams().stream()
                .map(team -> new KeyValue(team.getCode(), team.getDescription()))
                .collect(collectingAndThen(toList(), PageImpl::new));
    }
}
