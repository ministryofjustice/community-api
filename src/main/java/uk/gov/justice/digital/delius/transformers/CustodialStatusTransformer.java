package uk.gov.justice.digital.delius.transformers;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.delius.data.api.CustodialStatus;
import uk.gov.justice.digital.delius.data.api.KeyValue;
import uk.gov.justice.digital.delius.jpa.standard.entity.Disposal;
import uk.gov.justice.digital.delius.jpa.standard.entity.Release;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class CustodialStatusTransformer {
    public CustodialStatus custodialStatusOf(Disposal disposal) {
        return CustodialStatus.builder()
                .sentenceId(disposal.getDisposalId())
                .custodialType(custodialTypeOf(disposal))
                .sentence(sentenceOf(disposal))
                .mainOffence(mainOffenceOf(disposal))
                .sentenceDate(disposal.getStartDate())
                .actualReleaseDate(actualReleaseDateOf(disposal))
                .licenceExpiryDate(pssStartDateOf(disposal))
                .length(disposal.getLength())
                .lengthUnit("Months")
        .build();
    }

    private LocalDate pssStartDateOf(Disposal disposal) {
        return disposal.getCustody().getPssStartDate();
    }

    private LocalDate actualReleaseDateOf(Disposal disposal) {
        return disposal.getCustody()
                .getReleases()
                .stream()
                .map(Release::getActualReleaseDate)
                .max(LocalDateTime::compareTo)
                .map(LocalDateTime::toLocalDate)
                .orElse(null);
    }

    private KeyValue mainOffenceOf(Disposal disposal) {
        return KeyValue.builder()
                .description(disposal.getEvent().getMainOffence().getOffence().getDescription()).build();
    }

    private KeyValue sentenceOf(Disposal disposal) {
        return KeyValue.builder()
                .description(disposal.getDisposalType().getDescription())
                .build();
    }

    private KeyValue custodialTypeOf(Disposal disposal) {
        return KeyValue.builder()
                .code(disposal.getCustody().getCustodialStatus().getCodeValue())
                .description(disposal.getCustody().getCustodialStatus().getCodeDescription())
                .build();
    }
}
