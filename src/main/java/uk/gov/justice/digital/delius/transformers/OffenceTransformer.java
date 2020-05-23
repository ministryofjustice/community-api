package uk.gov.justice.digital.delius.transformers;

import uk.gov.justice.digital.delius.data.api.Offence;
import uk.gov.justice.digital.delius.jpa.standard.entity.AdditionalOffence;
import uk.gov.justice.digital.delius.jpa.standard.entity.MainOffence;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.digital.delius.transformers.OffenceDetailTransformer.detailOf;
import static uk.gov.justice.digital.delius.transformers.OffenceIdTransformer.additionalOffenceIdOf;
import static uk.gov.justice.digital.delius.transformers.TypesTransformer.convertToBoolean;

public class OffenceTransformer {
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

    public static Offence offenceOf(MainOffence mainOffence) {
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
