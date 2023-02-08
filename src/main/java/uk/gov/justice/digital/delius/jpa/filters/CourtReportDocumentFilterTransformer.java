package uk.gov.justice.digital.delius.jpa.filters;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.digital.delius.data.filters.DocumentFilter.SubType;
import uk.gov.justice.digital.delius.jpa.standard.entity.CourtReportDocument;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Optional;

import static uk.gov.justice.digital.delius.jpa.standard.entity.RCourtReportType.PRE_SENTENCE_REPORT_TYPES;

public class CourtReportDocumentFilterTransformer implements Specification<CourtReportDocument> {
    private final Long offenderId;
    private final Optional<SubType> subType;

    private CourtReportDocumentFilterTransformer(Long offenderId, Optional<SubType> subType) {
        this.offenderId = offenderId;
        this.subType = subType;
    }

    public static Specification<CourtReportDocument> of(Long offenderId, SubType type) {
        return new CourtReportDocumentFilterTransformer(offenderId, Optional.ofNullable(type));
    }

    @Override
    public Predicate toPredicate(@NotNull Root<CourtReportDocument> documentRoot, @NotNull CriteriaQuery<?> query, @NotNull CriteriaBuilder criteriaBuilder) {
        final var predicateBuilder = new ArrayList<Predicate>();

        predicateBuilder.add(criteriaBuilder.equal(documentRoot.get("offenderId"), offenderId));
        predicateBuilder.add(criteriaBuilder.equal(documentRoot.get("softDeleted"), false));

        subType.filter(type -> type == SubType.PSR).ifPresent(type -> {
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
