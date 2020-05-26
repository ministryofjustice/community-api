package uk.gov.justice.digital.delius.entitybuilders;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.Offence;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.Event;
import uk.gov.justice.digital.delius.service.LookupSupplier;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class AdditionalOffenceEntityBuilder {
    private final LookupSupplier lookupSupplier;

    public AdditionalOffenceEntityBuilder(LookupSupplier lookupSupplier) {
        this.lookupSupplier = lookupSupplier;
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
