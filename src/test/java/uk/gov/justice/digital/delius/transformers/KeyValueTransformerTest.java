package uk.gov.justice.digital.delius.transformers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import static org.assertj.core.api.Assertions.assertThat;

class KeyValueTransformerTest {

    @DisplayName("Given null input, ensure no NPE and return null")
    @Test
    void whenStandardReferenceIsNull_thenMapToNull() {
        assertThat(KeyValueTransformer.keyValueOf(null)).isNull();
    }

    @DisplayName("Given populated StandardReference input then map to KeyValue")
    @Test
    void whenStandardReferenceIsSet_thenMapCodeAndDescription() {
        var standardReference = StandardReference.builder()
                                                        .codeValue("CODE")
                                                        .codeDescription("Description")
                                                        .build();
        var keyValue = KeyValueTransformer.keyValueOf(standardReference);

        assertThat(keyValue.getCode()).isEqualTo("CODE");
        assertThat(keyValue.getDescription()).isEqualTo("Description");
    }

}
