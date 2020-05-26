package uk.gov.justice.digital.delius.transformers;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.delius.data.api.CustodyKeyDate;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jpa.standard.entity.KeyDate;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
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