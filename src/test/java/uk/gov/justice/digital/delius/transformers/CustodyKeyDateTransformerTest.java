package uk.gov.justice.digital.delius.transformers;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.digital.delius.data.api.CustodyKeyDate;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jpa.standard.entity.KeyDate;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class CustodyKeyDateTransformerTest {
    @Test
    public void custodyKeyDateOfCopiesReferenceData() {
        val keyDate = LocalDate.now();
        assertThat(CustodyKeyDateTransformer.custodyKeyDateOf(
                KeyDate
                        .builder()
                        .keyDateType(
                                StandardReference
                                        .builder()
                                        .codeValue("XX")
                                        .codeDescription("Description")
                                        .build()
                        )
                        .keyDate(keyDate)
                        .build()))
                .isEqualTo(
                        CustodyKeyDate
                                .builder()
                                .type(KeyValue
                                        .builder()
                                        .code("XX")
                                        .description("Description")
                                        .build())
                                .date(keyDate)
                                .build());
    }
}