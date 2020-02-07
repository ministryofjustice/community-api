package uk.gov.justice.digital.delius.jpa.filters;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

@Builder(toBuilder = true)
@EqualsAndHashCode
public class ProbationAreaFilter implements Specification<ProbationArea> {

    @Builder.Default
    private Optional<List<String>> probationAreaCodes = Optional.empty();

    @Builder.Default
    private Boolean restrictActive = false;

    @Builder.Default
    private Boolean excludeEstablishments = false;

    @Override
    public Predicate toPredicate(Root<ProbationArea> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        ImmutableList.Builder<Predicate> predicateBuilder = ImmutableList.builder();

        probationAreaCodes.ifPresent(strings -> predicateBuilder.add(root.get("code").in(strings)));

        if (restrictActive) {
            predicateBuilder.add(cb.isNull(root.get("endDate")));
        }

        if (excludeEstablishments) {
            predicateBuilder.add(cb.isNull(root.get("establishment")));
        }

        ImmutableList<Predicate> predicates = predicateBuilder.build();

        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
    }
}