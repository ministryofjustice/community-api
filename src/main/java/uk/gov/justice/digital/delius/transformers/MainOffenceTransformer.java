package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.Offence;
import uk.gov.justice.digital.delius.jpa.standard.entity.MainOffence;

import static uk.gov.justice.digital.delius.transformers.OffenceDetailTransformer.detailOf;

@Component
public class MainOffenceTransformer {
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

}
