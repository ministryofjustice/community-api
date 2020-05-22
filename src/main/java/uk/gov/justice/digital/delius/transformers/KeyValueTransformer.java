package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.util.Optional;

@Component
public class KeyValueTransformer {
    public static KeyValue keyValueOf(StandardReference standardReference) {
        return Optional.ofNullable(standardReference).map(reason -> KeyValue.builder()
                    .description(reason.getCodeDescription())
                    .code(reason.getCodeValue()).build())
        .orElse(null);
    }
}