package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.CustodyKeyDate;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jpa.standard.entity.KeyDate;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

public class CustodyKeyDateTransformer {
    private static KeyValue keyValueOf(StandardReference outcome) {
        return KeyValue
                .builder()
                .description(outcome.getCodeDescription())
                .code(outcome.getCodeValue())
                .build();
    }

    public static CustodyKeyDate custodyKeyDateOf(KeyDate keyDate) {
        return CustodyKeyDate
                .builder()
                .date(keyDate.getKeyDate())
                .type(keyValueOf(keyDate.getKeyDateType()))
                .build();
    }

}
