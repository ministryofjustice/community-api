package uk.gov.justice.digital.delius.jpa.filters;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtReportDocument;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Optional;

import static uk.gov.justice.digital.delius.jpa.standard.entity.RCourtReportType.PRE_SENTENCE_REPORT_TYPES;

public class CourtReportDocumentFilterTransformer implements Specification<CourtReportDocument> {
    private final Long offenderId;
    private final Optional<String> type;

    private CourtReportDocumentFilterTransformer(Long offenderId, Optional<String> type) {
        this.offenderId = offenderId;
        this.type = type;
    }

    public static Specification<CourtReportDocument> of(Long offenderId, String type) {
        return new CourtReportDocumentFilterTransformer(offenderId, Optional.ofNullable(type));
    }

    @Override
    public Predicate toPredicate(@NotNull Root<CourtReportDocument> documentRoot, @NotNull CriteriaQuery<?> query, @NotNull CriteriaBuilder criteriaBuilder) {
        final var predicateBuilder = new ArrayList<Predicate>();

        predicateBuilder.add(criteriaBuilder.equal(documentRoot.get("offenderId"), offenderId));

        type.filter(type -> type.equals("PSR")).ifPresent(type -> {
            final var courtReportTypeCodeIn = criteriaBuilder.in(documentRoot
                .join("courtReport")
                .join("courtReportType")
                .get("code"));
            PRE_SENTENCE_REPORT_TYPES.forEach(courtReportTypeCodeIn::value);
            predicateBuilder.add(courtReportTypeCodeIn);
        });
        return criteriaBuilder.and(predicateBuilder.toArray(new Predicate[]{}));
    }
}
