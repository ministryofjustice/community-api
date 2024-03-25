package uk.gov.justice.digital.delius.jpa.filters;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.digital.delius.data.filters.OffenderFilter;
import uk.gov.justice.digital.delius.jpa.standard.entity.OffenderPrimaryIdentifiers;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OffenderFilterTransformer implements Specification<OffenderPrimaryIdentifiers> {
    private final OffenderFilter filter;

    private OffenderFilterTransformer(OffenderFilter filter) {
        this.filter = filter;
    }

    public static Specification<OffenderPrimaryIdentifiers> fromFilter(OffenderFilter filter) {
        return new OffenderFilterTransformer(filter);
    }

    @Override
    public Predicate toPredicate(@NotNull Root<OffenderPrimaryIdentifiers> offenderRoot, @NotNull CriteriaQuery<?> query, @NotNull CriteriaBuilder criteriaBuilder) {
        final List<Predicate> predicates = new ArrayList<>();

        if (!filter.isIncludeDeleted()) {
            predicates.add(criteriaBuilder.equal(offenderRoot.get("softDeleted"), 0));
        }

        if (filter.isIncludeActiveOnly()) {
            predicates.add(criteriaBuilder.equal(offenderRoot.get("currentDisposal"), 1));
        }

        Optional.ofNullable(filter.getActiveDate()).ifPresent(activeDate -> {
                /*
                Predicate below should generate SQL like this:
                    select distinct o.* from OFFENDER o
                        inner join EVENT e on o.OFFENDER_ID=events1_.OFFENDER_ID
                        inner join DISPOSAL d on e.EVENT_ID= d.EVENT_ID
                    where
                    and e.SOFT_DELETED=0
                    and d.DISPOSAL_DATE<=?
                    and ( d.TERMINATION_DATE>=? or  d.TERMINATION_DATE is null)
                 */
                    final var eventJoin = offenderRoot.join("events");
                    predicates.add(criteriaBuilder.equal(eventJoin.get("softDeleted"), 0));
                    final var disposalJoin = eventJoin.join("disposal");
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(disposalJoin.get("startDate"), activeDate));
                    predicates.add(criteriaBuilder.or(
                            criteriaBuilder.greaterThanOrEqualTo(disposalJoin.get("terminationDate"), activeDate),
                            criteriaBuilder.isNull(disposalJoin.get("terminationDate"))
                    ));
                    query.distinct(true);
                }
        );
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
