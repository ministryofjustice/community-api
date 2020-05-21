package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.data.api.ReferenceData;
import uk.gov.justice.digital.delius.jpa.standard.entity.ReferenceDataMaster;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ReferenceDataTransformer {
    private ReferenceDataTransformer() {
        // static helpers only
    }

    public static List<KeyValue> referenceDataSetsOf(List<ReferenceDataMaster> referenceDataMasters) {
        return referenceDataMasters.stream()
                .map(sets -> KeyValue
                        .builder()
                        .code(sets.getCodeSetName())
                        .description(sets.getDescription())
                        .build())
                .sorted(Comparator.comparing(KeyValue::getCode))
                .collect(toList());
    }

    public static List<ReferenceData> referenceDataOf(List<StandardReference> standardReferences) {
        return standardReferences.stream()
                .map(data -> ReferenceData
                        .builder()
                        .code(data.getCodeValue())
                        .description(data.getCodeDescription())
                        .active(data.isActive())
                        .build())
                .sorted(Comparator.comparing(ReferenceData::getCode))
                .collect(toList());
    }


}
