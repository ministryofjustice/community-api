package uk.gov.justice.digital.delius.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.LocalDeliveryUnit;
import uk.gov.justice.digital.delius.data.api.ProbationArea;
import uk.gov.justice.digital.delius.data.api.ProbationAreaWithLocalDeliveryUnits;
import uk.gov.justice.digital.delius.jpa.filters.ProbationAreaFilter;
import uk.gov.justice.digital.delius.jpa.standard.entity.District;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.jpa.standard.repository.ProbationAreaRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.ReferenceDataMasterRepository;
import uk.gov.justice.digital.delius.jpa.standard.repository.StandardReferenceRepository;
import uk.gov.justice.digital.delius.transformers.ProbationAreaTransformer;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.ynToBoolean;

@Service
public class ReferenceDataService {

    public static final String POM_AUTO_TRANSFER_ALLOCATION_REASON_CODE = "AUT";
    public static final String POM_INTERNAL_TRANSFER_ALLOCATION_REASON_CODE = "INA";
    public static final String POM_EXTERNAL_TRANSFER_ALLOCATION_REASON_CODE = "EXT";
    public static final String REFERENCE_DATA_PSR_ADJOURNED_CODE = "101";
    private static final String ADDITIONAL_IDENTIFIER_DATASET = "ADDITIONAL IDENTIFIER TYPE";
    private static final String DUPLICATE_NOMS_NUMBER_CODE = "DNOMS";
    private static final String FORMER_NOMS_NUMBER_CODE = "XNOMS";
    private final ProbationAreaRepository probationAreaRepository;
    private final StandardReferenceRepository standardReferenceRepository;


    @Autowired
    public ReferenceDataService(ProbationAreaRepository probationAreaRepository, StandardReferenceRepository standardReferenceRepository, ReferenceDataMasterRepository referenceDataMasterRepository) {
        this.probationAreaRepository = probationAreaRepository;
        this.standardReferenceRepository = standardReferenceRepository;
    }

    public List<ProbationArea> getProbationAreasForCode(String code, boolean restrictActive) {
        ProbationAreaFilter probationAreaFilter = ProbationAreaFilter.builder().probationAreaCodes(Optional.of(List.of(code))).restrictActive(restrictActive).build();

        return ProbationAreaTransformer.probationAreasOf(probationAreaRepository.findAll(probationAreaFilter));
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


    public StandardReference duplicateNomsNumberAdditionalIdentifier() {
        return standardReferenceRepository.findByCodeAndCodeSetName(DUPLICATE_NOMS_NUMBER_CODE, ADDITIONAL_IDENTIFIER_DATASET).orElseThrow();
    }

    public StandardReference formerNomsNumberAdditionalIdentifier() {
        return standardReferenceRepository.findByCodeAndCodeSetName(FORMER_NOMS_NUMBER_CODE, ADDITIONAL_IDENTIFIER_DATASET).orElseThrow();
    }

    public List<ProbationAreaWithLocalDeliveryUnits> getProbationAreasAndLocalDeliveryUnits(boolean restrictActive) {
        final var filter = ProbationAreaFilter
                .builder()
                .restrictActive(restrictActive)
                .excludeEstablishments(true).build();
        final var probationAreas =  probationAreaRepository.findAll(filter);

        return probationAreas.stream().map(
                pa -> {
                    final var ldus = pa.getBoroughs().stream()
                            // LDUs are represented as districts in the delius schema
                            .flatMap(borough -> borough.getDistricts().stream())
                            .filter(this::getPossibleActiveLdus) // current (non-historic) only
                            .map(ldu -> LocalDeliveryUnit.builder().localDeliveryUnitId(ldu.getDistrictId()).code(ldu.getCode()).description(ldu.getDescription()).build())
                            .toList();

                    return ProbationAreaWithLocalDeliveryUnits.builder().code(pa.getCode()).description(pa.getDescription()).localDeliveryUnits(ldus).build();


                }
        ).toList();
    }

    private boolean getPossibleActiveLdus(District district) {
        return ynToBoolean(district.getSelectable()) || district.getCode().endsWith("UAT") || district.getCode().endsWith("UNA") || district.getCode().endsWith("IAV");
    }


}
