package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.Offence;
import uk.gov.justice.digital.delius.jpa.standard.entity.MainOffence;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.justice.digital.delius.transformers.OffenceDetailTransformer.detailOf;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.zeroOneToBoolean;

@Component
public class MainOffenceTransformer {
    public List<Offence> offencesOf(List<MainOffence> mainOffences) {
        return mainOffences.stream()
            .map(mainOffence ->
                Offence.builder()
                    .id(mainOffence.getMainOffenceId())
                    .detail(detailOf(mainOffence.getOffence()))
                    .createdDatetime(mainOffence.getCreatedDatetime())
                    .eventId(mainOffence.getEventId())
                    .lastUpdatedDatetime(mainOffence.getLastUpdatedDatetime())
                    .mainOffence(true)
                    .offenceCount(mainOffence.getOffenceCount())
                    .offenceDate(mainOffence.getOffenceDate())
                    .offenderId(mainOffence.getOffenderId())
                    .softDeleted(zeroOneToBoolean(mainOffence.getSoftDeleted()))
                    .tics(mainOffence.getTics())
                    .verdict(mainOffence.getVerdict())
                    .build()
            )
            .collect(Collectors.toList());
    }

}
