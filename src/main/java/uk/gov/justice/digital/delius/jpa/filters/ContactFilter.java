package uk.gov.justice.digital.delius.jpa.filters;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.digital.delius.controller.BadRequestException;
import uk.gov.justice.digital.delius.jpa.standard.entity.Contact;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.justice.digital.delius.jpa.standard.entity.RequirementTypeMainCategory.REHABILITATION_ACTIVITY_REQUIREMENT_CODE;

@Builder(toBuilder = true)
@EqualsAndHashCode
public class ContactFilter implements Specification<Contact> {

    public static final String INCLUDE_TYPE_PREFIX = "TYPE_";
    public static final String INCLUDE_APPOINTMENTS = "APPOINTMENTS";
    @Builder.Default
    private Optional<List<String>> contactTypes = Optional.empty();

    @Builder.Default
    private Optional<LocalDateTime> from = Optional.empty();

    @Builder.Default
    private Optional<LocalDateTime> to = Optional.empty();

    @Builder.Default
    private Optional<LocalDate> contactDateFrom = Optional.empty();

    @Builder.Default
    private Optional<LocalDate> contactDateTo = Optional.empty();

    private Long offenderId;
    @Builder.Default
    private Optional<Boolean> appointmentsOnly = Optional.empty();

    @Builder.Default
    private Optional<Long> convictionId = Optional.empty();

    @Builder.Default
    private Optional<Boolean> attended = Optional.empty();

    @Builder.Default
    private Optional<Boolean> complied = Optional.empty();
    
    @Builder.Default
    private Optional<Boolean> nationalStandard = Optional.empty();

    @Builder.Default
    private Optional<Boolean> outcome = Optional.empty();

    @Builder.Default
    private Optional<List<String>> include = Optional.empty();

    @Builder.Default
    private Optional<Boolean> rarActivity = Optional.empty();

    @Override
    public Predicate toPredicate(Root<Contact> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        ImmutableList.Builder<Predicate> predicateBuilder = ImmutableList.builder();

        predicateBuilder.add(cb.equal(root.get("offenderId"), offenderId));

        contactTypes.ifPresent(strings -> predicateBuilder.add(root.get("contactType").get("code").in(strings)));

        from.ifPresent(localDateTime -> predicateBuilder.add(cb.greaterThanOrEqualTo(root.get("createdDateTime"), localDateTime)));

        to.ifPresent(localDateTime -> predicateBuilder.add(cb.lessThanOrEqualTo(root.get("createdDateTime"), localDateTime)));

        contactDateFrom.ifPresent(localDate -> predicateBuilder.add(cb.greaterThanOrEqualTo(root.get("contactDate"), localDate)));

        contactDateTo.ifPresent(localDate -> predicateBuilder.add(cb.lessThanOrEqualTo(root.get("contactDate"), localDate)));

        appointmentsOnly.ifPresent(value -> predicateBuilder.add(cb.equal(root.get("contactType").get("attendanceContact"), value)));

        convictionId.ifPresent(value -> predicateBuilder.add(cb.equal(root.get("event").get("eventId"), value)));

        attended.ifPresent(value -> predicateBuilder.add(cb.equal(root.get("attended"), value ? "Y" : "N")));

        complied.ifPresent(value -> predicateBuilder.add(cb.equal(root.get("complied"), value ? "Y" : "N")));

        nationalStandard.ifPresent(value -> predicateBuilder.add(cb.equal(root.get("contactType").get("nationalStandardsContact"), value)));

        outcome.ifPresent(value -> predicateBuilder.add(value ? cb.isNotNull(root.get("contactOutcomeType")) : cb.isNull(root.get("contactOutcomeType"))));

        rarActivity.filter(value -> value).ifPresent(value -> rarActivityOnlyFilter(root, cb, predicateBuilder));
        List<Predicate> includePredicates = getIncludePredicates(root, query, cb);

        if (includePredicates.size() > 0 ) {
            predicateBuilder.add(cb.or(includePredicates.toArray(new Predicate[0])));
        }

        ImmutableList<Predicate> predicates = predicateBuilder.build();

        return cb.and(predicates.toArray(new Predicate[0]));
    }

    private void rarActivityOnlyFilter(Root<Contact> root, CriteriaBuilder cb, ImmutableList.Builder<Predicate> predicateBuilder) {
        predicateBuilder.add(cb.equal(root.get("requirement").get("softDeleted"), false));
        predicateBuilder.add(cb.equal(root.get("rarActivity"), "Y"));
        predicateBuilder.add(cb.equal(root.get("requirement").get("requirementTypeMainCategory")
            .get("code"), REHABILITATION_ACTIVITY_REQUIREMENT_CODE));
    }

    private List<Predicate> getIncludePredicates(Root<Contact> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return include.map(value -> value.stream()
            .map(inc -> inc.trim().toUpperCase())
            .distinct().map(inc -> {
                if(inc.startsWith(INCLUDE_TYPE_PREFIX)) {
                    return cb.equal(root.get("contactType").get("code"), getIncludeValue(inc));
                } else if(inc.equals(INCLUDE_APPOINTMENTS)) {
                    return cb.equal(root.get("contactType").get("attendanceContact"), true);
                } else {
                    throw new BadRequestException("Unknown include parameter");
                }
        }).collect(Collectors.toList())).orElse(Collections.emptyList());
    }

    private String getIncludeValue(final String includeParameter) {
        var values = includeParameter.split("_",2);

        if (values.length > 1) {
            return values[1];
        }
        return "";
    }
}