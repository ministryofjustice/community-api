package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.Offence;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalOffence;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.justice.digital.delius.transformers.OffenceDetailTransformer.detailOf;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.zeroOneToBoolean;

@Component
public class AdditionalOffenceTransformer {

    public List<Offence> offencesOf(List<AdditionalOffence> additionalOffences) {
        return additionalOffences.stream()
            .map(additionalOffence ->
                Offence.builder()
                    .id(additionalOffence.getAdditionalOffenceId())
                    .detail(detailOf(additionalOffence.getOffence()))
                    .createdDatetime(additionalOffence.getCreatedDatetime())
                    .eventId(additionalOffence.getEventId())
                    .lastUpdatedDatetime(additionalOffence.getLastUpdatedDatetime())
                    .mainOffence(false)
                    .offenceCount(additionalOffence.getOffenceCount())
                    .offenceDate(additionalOffence.getOffenceDate())
                    .softDeleted(zeroOneToBoolean(additionalOffence.getSoftDeleted()))
                    .build()
            )
            .collect(Collectors.toList());
    }

}
