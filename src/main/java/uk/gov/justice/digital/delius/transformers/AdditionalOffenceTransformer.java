package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.Offence;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalOffence;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.justice.digital.delius.transformers.OffenceDetailTransformer.detailOf;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.convertToBoolean;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.zeroOneToBoolean;

@Component
public class AdditionalOffenceTransformer {

    public List<Offence> offencesOf(List<AdditionalOffence> additionalOffences) {
        return additionalOffences.stream()
            .filter(offence -> !convertToBoolean(offence.getSoftDeleted()))
            .map(additionalOffence ->
                Offence.builder()
                    .offenceId(String.format("%s%d", "A", additionalOffence.getAdditionalOffenceId()))
                    .detail(detailOf(additionalOffence.getOffence()))
                    .createdDatetime(additionalOffence.getCreatedDatetime())
                    .lastUpdatedDatetime(additionalOffence.getLastUpdatedDatetime())
                    .mainOffence(false)
                    .offenceCount(additionalOffence.getOffenceCount())
                    .offenceDate(additionalOffence.getOffenceDate())
                    .build()
            )
            .collect(Collectors.toList());
    }

}
