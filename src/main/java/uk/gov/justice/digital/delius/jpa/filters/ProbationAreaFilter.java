package uk.gov.justice.digital.delius.jpa.filters;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.digital.delius.jpa.standard.entity.ProbationArea;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Builder(toBuilder = true)
@Getter
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
        List<Predicate> predicates = new ArrayList<>();

        probationAreaCodes.ifPresent(strings -> predicates.add(root.get("code").in(strings)));

        if (restrictActive) {
            predicates.add(cb.equal(root.get("selectable"), "Y"));
        }

        if (excludeEstablishments) {
            predicates.add(cb.isNull(root.get("establishment")));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
