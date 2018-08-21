package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.Offence;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalOffence;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.digital.delius.transformers.OffenceDetailTransformer.detailOf;
import static uk.gov.justice.digital.delius.transformers.OffenceIdTransformer.additionalOffenceIdOf;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.convertToBoolean;

@Component
public class AdditionalOffenceTransformer {

    public List<Offence> offencesOf(List<AdditionalOffence> additionalOffences) {
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

}
