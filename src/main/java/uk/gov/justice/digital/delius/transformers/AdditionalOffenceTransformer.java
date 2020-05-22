package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.Offence;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.service.LookupSupplier;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.digital.delius.transformers.OffenceDetailTransformer.detailOf;
import static uk.gov.justice.digital.delius.transformers.OffenceIdTransformer.additionalOffenceIdOf;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.convertToBoolean;

@Component
public class AdditionalOffenceTransformer {
    private final LookupSupplier lookupSupplier;

    public AdditionalOffenceTransformer(LookupSupplier lookupSupplier) {
        this.lookupSupplier = lookupSupplier;
    }


    public static List<Offence> offencesOf(List<AdditionalOffence> additionalOffences) {
        return additionalOffences.stream()
            .filter(offence -> !convertToBoolean(offence.getSoftDeleted()))
            .map(additionalOffence ->
                Offence.builder()
                    .offenceId(additionalOffenceIdOf(additionalOffence.getAdditionalOffenceId()))
                    .detail(detailOf(additionalOffence.getOffence()))
                    .createdDatetime(additionalOffence.getCreatedDatetime())
                    .lastUpdatedDatetime(additionalOffence.getLastUpdatedDatetime())
                    .mainOffence(false)
                    .offenceCount(additionalOffence.getOffenceCount())
                    .offenceDate(additionalOffence.getOffenceDate())
                    .build()
            )
            .collect(toList());
    }

    public List<AdditionalOffence> additionalOffencesOf(
            List<Offence> additionalOffences,
            Event event) {
        return additionalOffences
                .stream()
                .map(offence -> AdditionalOffence
                        .builder()
                        .offence(lookupSupplier.offenceSupplier().apply(offence.getDetail().getCode()))
                        .softDeleted(0L)
                        .createdByUserId(lookupSupplier.userSupplier().get().getUserId())
                        .createdDatetime(LocalDateTime.now())
                        .lastUpdatedUserId(lookupSupplier.userSupplier().get().getUserId())
                        .lastUpdatedDatetime(LocalDateTime.now())
                        .offenceCount(offence.getOffenceCount())
                        .offenceDate(offence.getOffenceDate())
                        .rowVersion(1L)
                        .partitionAreaId(0L)
                        .event(event)
                        .build())
                .collect(toList());
    }
}
