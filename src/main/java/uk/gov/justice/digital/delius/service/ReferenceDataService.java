package uk.gov.justice.digital.delius.service;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.controller.NotFoundException;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.ProbationArea;
import uk.gov.justice.digital.delius.data.api.ReferenceData;
import uk.gov.justice.digital.delius.jpa.filters.ProbationAreaFilter;
import uk.gov.justice.digital.delius.jpa.standard.entity.District;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.ProbationAreaRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ReferenceDataMasterRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StandardReferenceRepository;
import uk.gov.justice.digital.delius.transformers.ProbationAreaTransformer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.*;
import static java.util.stream.Collectors.*;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.ynToBoolean;

@Service
public class ReferenceDataService {

    public static final String POM_AUTO_TRANSFER_ALLOCATION_REASON_CODE = "AUT";
    public static final String POM_INTERNAL_TRANSFER_ALLOCATION_REASON_CODE = "INA";
    public static final String POM_EXTERNAL_TRANSFER_ALLOCATION_REASON_CODE = "EXT";
    private static final String POM_ALLOCATION_REASON_DATASET = "POM ALLOCATION REASON";
    private static final String CUSTODY_EVENT_DATASET = "CUSTODY EVENT TYPE";
    private static final String CUSTODY_EVENT_PRISON_LOCATION_CHANGE_CODE = "CPL";
    private static final String CUSTODY_EVENT_CUSTODY_STATUS_CHANGE_CODE = "TSC";
    private static final String CUSTODY_STATUS_DATASET = "THROUGHCARE STATUS";
    private static final String CUSTODY_STATUS_IN_CUSTODY_CODE = "D";
    private static final String ADDITIONAL_IDENTIFIER_DATASET = "ADDITIONAL IDENTIFIER TYPE";
    private static final String DUPLICATE_NOMS_NUMBER_CODE = "DNOMS";
    private static final String FORMER_NOMS_NUMBER_CODE = "XNOMS";
    private final ProbationAreaTransformer probationAreaTransformer;
    private final ProbationAreaRepository probationAreaRepository;
    private final StandardReferenceRepository standardReferenceRepository;
    private final ReferenceDataMasterRepository referenceDataMasterRepository;


    @Autowired
    public ReferenceDataService(ProbationAreaTransformer probationAreaTransformer, ProbationAreaRepository probationAreaRepository, StandardReferenceRepository standardReferenceRepository, ReferenceDataMasterRepository referenceDataMasterRepository) {
        this.probationAreaTransformer = probationAreaTransformer;
        this.probationAreaRepository = probationAreaRepository;
        this.standardReferenceRepository = standardReferenceRepository;
        this.referenceDataMasterRepository = referenceDataMasterRepository;
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
                .orElseThrow(() -> new RuntimeException(format("No pom allocation reason found for %s", reason)));
    }

    public Page<KeyValue> getProbationAreasCodes(boolean restrictActive, boolean excludeEstablishments) {
        final var filter = ProbationAreaFilter
                .builder()
                .restrictActive(restrictActive)
                .excludeEstablishments(excludeEstablishments).build();

        return probationAreaRepository.findAll(filter).stream()
                .map(area -> new KeyValue(area.getCode(), area.getDescription()))
                .collect(collectingAndThen(toList(), PageImpl::new));
    }

    public Page<KeyValue> getLocalDeliveryUnitsForProbationArea(String code) {
        return getSelectableLdusForProbationArea(code)
                .map(ldu -> new KeyValue(ldu.getCode(), ldu.getDescription()))
                .collect(collectingAndThen(toList(), PageImpl::new));
    }

    public Page<KeyValue> getTeamsForLocalDeliveryUnit(String code, String lduCode) {
        var ldus = getSelectableLdusForProbationArea(code)
                // ldu code is not primary key so duplicates can exist - this returns all teams that are linked to LDUs with the provided code
                .filter(ldu -> ldu.getCode().equals(lduCode))
                .collect(toList());

        if (ldus.isEmpty()) {
            throw new NotFoundException(format("Could not find local delivery unit in probation area: '%s', with code: '%s'", code, lduCode));
        }

        return ldus.stream()
                .flatMap(ldu -> ldu.getTeams().stream())
                .map(team -> new KeyValue(team.getCode(), team.getDescription()))
                .collect(collectingAndThen(toList(), PageImpl::new));
    }

    public StandardReference getPrisonLocationChangeCustodyEvent() {
        return getCustodyEventTypeFor(CUSTODY_EVENT_PRISON_LOCATION_CHANGE_CODE);
    }

    public StandardReference getCustodyStatusChangeCustodyEvent() {
        return getCustodyEventTypeFor(CUSTODY_EVENT_CUSTODY_STATUS_CHANGE_CODE);
    }


    public StandardReference getInCustodyCustodyStatus() {
        return standardReferenceRepository.findByCodeAndCodeSetName(CUSTODY_STATUS_IN_CUSTODY_CODE, CUSTODY_STATUS_DATASET).orElseThrow();
    }


    public StandardReference duplicateNomsNumberAdditionalIdentifier() {
        return standardReferenceRepository.findByCodeAndCodeSetName(DUPLICATE_NOMS_NUMBER_CODE, ADDITIONAL_IDENTIFIER_DATASET).orElseThrow();
    }

    public StandardReference formerNomsNumberAdditionalIdentifier() {
        return standardReferenceRepository.findByCodeAndCodeSetName(FORMER_NOMS_NUMBER_CODE, ADDITIONAL_IDENTIFIER_DATASET).orElseThrow();
    }

    private StandardReference getCustodyEventTypeFor(String code) {
        return standardReferenceRepository.findByCodeAndCodeSetName(code, CUSTODY_EVENT_DATASET).orElseThrow();
    }

    private Stream<District> getSelectableLdusForProbationArea(String code) {
        var probationArea = probationAreaRepository.findByCode(code).orElseThrow(() ->
                new NotFoundException(format("Could not find probation area with code: '%s'", code)));

        return probationArea.getBoroughs().stream()
                // LDUs are represented as districts in the delius schema
                .flatMap(borough -> borough.getDistricts().stream())
                .filter(district -> ynToBoolean(district.getSelectable()));
    }

    public Optional<List<ReferenceData>> getReferenceDataForSet(String set) {
        return referenceDataMasterRepository
                .findByCodeSetName(set)
                .map(referenceDataMaster -> referenceDataMaster.getStandardReferences()
                        .stream()
                        .map(data -> ReferenceData
                                .builder()
                                .id(data.getStandardReferenceListId().toString())
                                .code(data.getCodeValue())
                                .description(data.getCodeDescription())
                                .active(data.isActive())
                                .build())
                        .collect(toList()));
    }

    public List<KeyValue> getReferenceDataSets() {
        return referenceDataMasterRepository
                .findAll()
                .stream()
                .map(sets -> KeyValue
                        .builder()
                        .code(sets.getCodeSetName())
                        .description(sets.getDescription())
                        .build())
                .collect(toList());
    }
}
