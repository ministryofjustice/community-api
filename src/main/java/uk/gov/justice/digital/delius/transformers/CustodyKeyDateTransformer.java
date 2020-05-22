package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.CustodyKeyDate;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jpa.standard.entity.KeyDate;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.service.LookupSupplier;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class CustodyKeyDateTransformer {
    private LookupSupplier lookupSupplier;

    public CustodyKeyDateTransformer(LookupSupplier lookupSupplier) {
        this.lookupSupplier = lookupSupplier;
    }

    private static KeyValue keyValueOf(StandardReference outcome) {
        return KeyValue
                .builder()
                .description(outcome.getCodeDescription())
                .code(outcome.getCodeValue())
                .build();
    }

    public KeyDate keyDateOf(uk.gov.justice.digital.delius.jpa.standard.entity.Custody custody, StandardReference keyDateType, LocalDate date) {
        return KeyDate
                .builder()
                .createdByUserId(lookupSupplier.userSupplier().get().getUserId())
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedUserId(lookupSupplier.userSupplier().get().getUserId())
                .lastUpdatedDatetime(LocalDateTime.now())
                .custody(custody)
                .keyDate(date)
                .keyDateType(keyDateType)
                .partitionAreaId(0L)
                .softDeleted(0L)
                .rowVersion(1L)
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
