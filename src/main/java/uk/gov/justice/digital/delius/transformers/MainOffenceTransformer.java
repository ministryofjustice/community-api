package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.Offence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.jpa.standard.entity.MainOffence;
import uk.gov.justice.digital.delius.service.LookupSupplier;

import java.time.LocalDateTime;

import static uk.gov.justice.digital.delius.transformers.OffenceDetailTransformer.detailOf;

@Component
public class MainOffenceTransformer {
    private final LookupSupplier lookupSupplier;

    public MainOffenceTransformer(LookupSupplier lookupSupplier) {
        this.lookupSupplier = lookupSupplier;
    }

    public Offence offenceOf(MainOffence mainOffence) {
        return Offence.builder()
            .offenceId(OffenceIdTransformer.mainOffenceIdOf(mainOffence.getMainOffenceId()))
            .detail(detailOf(mainOffence.getOffence()))
            .createdDatetime(mainOffence.getCreatedDatetime())
            .lastUpdatedDatetime(mainOffence.getLastUpdatedDatetime())
            .mainOffence(true)
            .offenceCount(mainOffence.getOffenceCount())
            .offenceDate(mainOffence.getOffenceDate())
            .offenderId(mainOffence.getOffenderId())
            .tics(mainOffence.getTics())
            .verdict(mainOffence.getVerdict())
            .build();
    }

    public MainOffence mainOffenceOf(Long offenderId, Offence mainOffence, Event event) {
        return MainOffence.builder()
                .offence(lookupSupplier.offenceSupplier().apply(mainOffence.getDetail().getCode()))
                .event(event)
                .softDeleted(0L)
                .createdByUserId(lookupSupplier.userSupplier().get().getUserId())
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedUserId(lookupSupplier.userSupplier().get().getUserId())
                .lastUpdatedDatetime(LocalDateTime.now())
                .offenceCount(mainOffence.getOffenceCount())
                .offenceDate(mainOffence.getOffenceDate())
                .offenderId(offenderId)
                .partitionAreaId(0L)
                .rowVersion(1L)
                .tics(mainOffence.getTics())
                .verdict(mainOffence.getVerdict())
                .build();
    }
}
