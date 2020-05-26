package uk.gov.justice.digital.delius.entitybuilders;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.jpa.standard.entity.KeyDate;
import uk.gov.justice.digital.delius.jpa.standard.entity.StandardReference;
import uk.gov.justice.digital.delius.service.LookupSupplier;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class KeyDateEntityBuilder {
    private LookupSupplier lookupSupplier;

    public KeyDateEntityBuilder(LookupSupplier lookupSupplier) {
        this.lookupSupplier = lookupSupplier;
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
}
